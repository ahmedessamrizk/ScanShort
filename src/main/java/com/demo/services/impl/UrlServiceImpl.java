package com.demo.services.impl;

import com.demo.dtos.request.CreateUrlRequest;
import com.demo.dtos.request.UpdateUrlRequest;
import com.demo.dtos.response.UrlCreationResult;
import com.demo.dtos.response.UrlDetailsResponse;
import com.demo.dtos.response.UrlListResponse;
import com.demo.entities.Url;
import com.demo.entities.User;
import com.demo.entities.enums.UrlStatus;
import com.demo.exceptions.custom.ConflictException;
import com.demo.exceptions.custom.ForbiddenException;
import com.demo.exceptions.custom.NotFoundException;
import com.demo.exceptions.custom.ResourceGoneException;
import com.demo.mappers.UrlMapper;
import com.demo.repositories.UrlRepository;
import com.demo.repositories.specifications.UrlSpecification;
import com.demo.services.CacheService;
import com.demo.services.CounterService;
import com.demo.services.UrlService;
import com.demo.utils.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {
    private final UrlMapper urlMapper;
    private final UrlRepository urlRepository;
    private final CounterService counterService;
    private final CacheService cacheService;

    @Value("${app.base-url}")
    private String BASE_URL;

    private final String codeCounter = AppConstants.COUNTER_KEY;
    private final Duration guestUrlExpiration = AppConstants.GUEST_URL_EXPIRATION;

    @Override
    @Transactional
    public UrlCreationResult createUrl(CreateUrlRequest request) {
        boolean isUser = SecurityUtils.isUserLoggedIn();

        //Handle guest case
        if(!isUser){
           return createUrlForGuest(request);
        } else { //Handle user case
           return createUrlForUser(request);
        }
    }

    @Override
    public String redirectUrl(String shortCode){
        //Check in cache, if exists -> update views, redirect to the baseUrl.
        String cached = cacheService.getCachedUrl(shortCode);
        if(cached != null){
            cacheService.incrementViewsAndResetTTL(shortCode, AppConstants.URL_CACHE_EXPIRATION);
            return cached;
        }
        //If not exist -> check in database.
        Url url = getUrlOrThrow(shortCode);
        //Exist in db -> check status not expired or disabled
        if(url.getStatus() == UrlStatus.DISABLED)
            throw new ForbiddenException("This url is disabled");
        if(url.getStatus() == UrlStatus.EXPIRED || url.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ResourceGoneException("This url isn't available anymore");

        //If yes -> update viewCount, cache the baseUrl in db and redirect.
        cacheService.incrementViews(url.getShortCode());
        cacheService.cacheUrl(url.getShortCode(), url.getBaseUrl(), DateTimeUtils.getOptimalCacheTtl(url.getExpiresAt()));

        return url.getBaseUrl();
    }

    @Override
    @Transactional
    public UrlDetailsResponse getUrl(UUID id){
        Url fetchUrl = getOwnedUrlOrThrow(id);

        //flush any views exist in cache for this url.
        Long views = cacheService.getAndResetViews(fetchUrl.getShortCode());
        if(views != null && views > 0){
            urlRepository.incrementViewCount(fetchUrl.getShortCode(), views);
            fetchUrl.setViewCount(fetchUrl.getViewCount() + views);
        }

        return urlMapper.toDetailsDto(fetchUrl, BASE_URL);
    }

    @Override
    public PaginatedResponse<UrlListResponse> getUrls(UrlStatus status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Specification<Url> specification = Specification.where(UrlSpecification.hasStatus(status))
                .and(UrlSpecification.hasUserId(SecurityUtils.getCurrentUserId()));

        Page<Url> pageResult = urlRepository.findAll(specification, pageable);

        return PaginatedResponse.from(pageResult, url -> urlMapper.toListDto(url, BASE_URL));
    }

    @Override
    @Transactional
    public void disableUrl(UUID id) {
        //Check url exists
        Url url = getOwnedUrlOrThrow(id);

        if(!url.getStatus().equals(UrlStatus.ACTIVE))
            throw new IllegalArgumentException("You can only disable active urls");

        //Update in db and remove from cache
        url.setStatus(UrlStatus.DISABLED);
        Url updatedUrl = urlRepository.save(url);

        //Update views in db and evict the cache.
        Long views = cacheService.getAndResetViews(updatedUrl.getShortCode());
        if(views != null && views != 0L)
            urlRepository.incrementViewCount(updatedUrl.getShortCode(), views);

        cacheService.evictUrl(updatedUrl.getShortCode());
    }

    @Override
    @Transactional
    public UrlDetailsResponse expireUrl(UUID id, UpdateUrlRequest request) {
        //Check url exists + ownership
        Url url = getOwnedUrlOrThrow(id);

        //Update in db, also update status if it was expired
        url.setExpiresAt(request.expiresAt());
        if(url.getStatus().equals(UrlStatus.EXPIRED))
            url.setStatus(UrlStatus.ACTIVE);

        Url updatedUrl = urlRepository.save(url);

        //Update cache with new expiration date.
        cacheService.evictUrl(updatedUrl.getShortCode());

        return urlMapper.toDetailsDto(updatedUrl, BASE_URL);
    }

    @Override
    @Transactional
    public void enableUrl(UUID id) {
        //Check url exists
        Url url = getOwnedUrlOrThrow(id);

        if(!url.getStatus().equals(UrlStatus.DISABLED))
            throw new IllegalArgumentException("You can only activate disabled urls");

        //If scheduler doesn't work yet.
        if(url.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("You can't activate expired url");
        }

        //Update in db and remove from cache
        url.setStatus(UrlStatus.ACTIVE);
        urlRepository.save(url);
    }

    @Override
    public byte[] generateQr(String shortCode) {
        try{
            Url url = getUrlOrThrow(shortCode);

            String fullUrl = BASE_URL + url.getShortCode();
            int size = 300;

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    fullUrl,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code", e);  // ✅ only catch unexpected errors
        }
    }

    //------------------------------ Helper methods -------------------------------
    //For redirect — no ownership check
    private Url getUrlOrThrow(String shortCode){
        return urlRepository.findByShortCode(shortCode).orElseThrow(() -> new NotFoundException("Invalid shorten url"));
    }

    //For user operations — ownership included in query
    private Url getOwnedUrlOrThrow(UUID id){
        UUID userId = SecurityUtils.getCurrentUserId();
        return urlRepository.findByIdAndUser_Id(id, userId).orElseThrow(() -> new NotFoundException("Invalid shorten url"));
    }

    @Transactional
    private UrlCreationResult createUrlForGuest(CreateUrlRequest request){
        //Increment counter present in cache, update cache and update DB with new value.
        Long counter = counterService.incrementCounter(codeCounter);
        //Generate shorten url
        String code = CodeGenerator.generateCode(counter);
        //Set expiresAt as 30 days
        Url url = Url.builder()
                .shortCode(code)
                .baseUrl(request.baseUrl())
                .baseUrlHash(CodeGenerator.hashValue(request.baseUrl()))
                .expiresAt(DateTimeUtils.toLocalDateTime(guestUrlExpiration))
                .status(UrlStatus.ACTIVE)
                .build();
        //Save in db
        Url createdUrl = urlRepository.save(url);
        return new UrlCreationResult(urlMapper.toDetailsDto(createdUrl, BASE_URL), true);
    }

    @Transactional
    private UrlCreationResult createUrlForUser(CreateUrlRequest request){
        UUID userId = SecurityUtils.getCurrentUserId();
        String hashedBaseUrl = CodeGenerator.hashValue(request.baseUrl());
        String code;

        if(request.customCode() != null){
            //Validate not reserved
            if(AppConstants.RESERVED_WORDS.contains(request.customCode().toLowerCase()))
                throw new ConflictException("This custom code is reserved!");

            //Validate not taken
            if(urlRepository.existsByShortCode(request.customCode()))
                throw new ConflictException("This custom code is already taken");

            // use custom code — skip duplicate check, user explicitly wants this code
            code = request.customCode();
        } else{
            Optional<Url> existUrl  = urlRepository.findFirstByUser_IdAndBaseUrlHash(userId, hashedBaseUrl);
            //If exists? check allow duplicity. if no -> just return it.
            if(existUrl.isPresent() && !request.allowDuplicate()){
                UrlDetailsResponse response = urlMapper.toDetailsDto(existUrl.get(), BASE_URL);
                return new UrlCreationResult(response, false);
            }
            //If allow duplicity is yes or if this is new baseurl go on creation.
            Long counter = counterService.incrementCounter(codeCounter);
            //Generate code
            code = CodeGenerator.generateCode(counter);
        }

        //If user provide expiresAt -> use it, Else -> set Default time as guest
        LocalDateTime expiresAt = request.expiresAt() == null? DateTimeUtils.toLocalDateTime(guestUrlExpiration) : request.expiresAt();

        //Save in db
        User currentUser =  User.builder().id(userId).build();
        Url url = Url.builder()
                .baseUrl(request.baseUrl())
                .baseUrlHash(hashedBaseUrl)
                .shortCode(code)
                .user(currentUser)
                .expiresAt(expiresAt)
                .status(UrlStatus.ACTIVE)
                .viewCount(0L)
                .build();
        Url createdUrl = urlRepository.save(url);
        return new UrlCreationResult(urlMapper.toDetailsDto(createdUrl, BASE_URL), true);
    }
}
