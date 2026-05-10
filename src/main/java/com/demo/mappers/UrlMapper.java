package com.demo.mappers;

import com.demo.dtos.request.CreateUrlRequest;
import com.demo.dtos.response.UrlDetailsResponse;
import com.demo.dtos.response.UrlListResponse;
import com.demo.entities.Url;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UrlMapper {
    Url toEntity(CreateUrlRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortUrl", expression = "java(baseUrl + url.getShortCode())")
    @Mapping(target = "numberOfViews", source = "viewCount")
    UrlDetailsResponse toDetailsDto(Url url, @Context String baseUrl);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "shortUrl", expression = "java(baseUrl + url.getShortCode())")
    @Mapping(target = "numberOfViews", source = "viewCount")
    UrlListResponse toListDto(Url url, @Context String baseUrl);
}
