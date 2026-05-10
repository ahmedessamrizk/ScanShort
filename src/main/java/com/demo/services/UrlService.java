package com.demo.services;

import com.demo.dtos.request.CreateUrlRequest;
import com.demo.dtos.request.UpdateUrlRequest;
import com.demo.dtos.response.UrlCreationResult;
import com.demo.dtos.response.UrlDetailsResponse;
import com.demo.dtos.response.UrlListResponse;
import com.demo.entities.enums.UrlStatus;
import com.demo.utils.PaginatedResponse;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public interface UrlService {
    UrlCreationResult createUrl(CreateUrlRequest request);
    String redirectUrl(String shortCode);
    UrlDetailsResponse getUrl(UUID id);
    PaginatedResponse<UrlListResponse> getUrls(UrlStatus status, @Positive Integer page, @Positive Integer size);
    void disableUrl(UUID id);
    UrlDetailsResponse expireUrl(UUID id, UpdateUrlRequest request);
    void enableUrl(UUID id);
    byte[] generateQr(String shortCode);
}
