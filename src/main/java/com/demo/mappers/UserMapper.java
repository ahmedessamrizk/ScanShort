package com.demo.mappers;

import com.demo.dtos.request.SignupRequest;
import com.demo.dtos.response.UserDetailsResponse;
import com.demo.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(SignupRequest request);
    UserDetailsResponse toDetailsDto(User user);

}
