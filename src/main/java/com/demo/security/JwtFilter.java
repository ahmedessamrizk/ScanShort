package com.demo.security;

import com.demo.utils.ApiErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${security.bearer-prefix}")
    private String bearerPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try{
            String token = extractToken(request);

            if(token != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = jwtService.validateToken(token);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);
        }catch(IllegalAccessException ex){
            buildResponse(response, HttpServletResponse.SC_FORBIDDEN, ex.getMessage(), "FORBIDDEN");
        }catch(JwtException ex){
            buildResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token", "INVALID_TOKEN");
        }catch(Exception ex){
            log.error("JWT filter error: " + ex.getMessage());
            buildResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Something went wrong", "SYSTEM_INTERNAL_ERROR");
        }
    }

    private String extractToken(HttpServletRequest request){
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && authHeader.startsWith(bearerPrefix)){
            return authHeader.split(bearerPrefix + " ")[1];
        }
        return null;
    }

    private void buildResponse(HttpServletResponse response, int statusCode , String message, String errorCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(ApiErrorResponse.of(message, errorCode)));
    }
}
