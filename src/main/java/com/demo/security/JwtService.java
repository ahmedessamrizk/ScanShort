package com.demo.security;

import com.demo.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserDetailsService userDetailsService;

    @Value("${security.secret-key}")
    private String secretKet;

    @Value("${security.expiration-token-time}")
    private long expirationTokenTime;

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal)  authentication.getPrincipal();

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .issuedAt(new Date())
                .expiration(getExpirationDate())
                .signWith(getKey())
                .compact();
    }

    public Date getExpirationDate(){
        return new Date(System.currentTimeMillis() + expirationTokenTime);
    }

    @PostConstruct
    private Key getKey(){
        return Keys.hmacShaKeyFor(secretKet.getBytes());
    }

    public UserDetails validateToken(String token) throws IllegalAccessException {
        Claims claims = extractAllClaims(token);

        String username = claims.getSubject();
        if (username == null || username.isBlank()) {
            throw new JwtException("Invalid token: missing subject");
        }

        UserDetails userDetails = UserPrincipal.builder()
                .user(User.builder()
                        .id(UUID.fromString(claims.get("userId").toString()))
                        .email(claims.getSubject())
                        .password(null).build())
                .build();

//        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return userDetails;
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
