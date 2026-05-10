package com.demo.utils;

import com.demo.exceptions.custom.UnauthorizedException;
import com.demo.security.UserPrincipal;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityUtils {

    public static UserPrincipal getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated())
            throw new UnauthorizedException("No authenticated user found");

        return (UserPrincipal) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId(){
        return getCurrentUser().getId();
    }

    public static boolean isUserLoggedIn(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

}
