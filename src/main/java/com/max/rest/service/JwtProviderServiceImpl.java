package com.max.rest.service;

import com.max.rest.dto.UserCredentials;
import com.max.rest.exception.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class JwtProviderServiceImpl implements JwtProviderService{

    private final SecretKey JWT_ACCESS_SECRET;
    private final SecretKey JWT_REFRESH_SECRET;
    private final Integer MINUTES;
    private final Integer DAYS;

    public JwtProviderServiceImpl(
            @Value("${app.jwt.secret.access}") String jwtAccessSecret,
            @Value("${app.jwt.secret.refresh}") String jwtRefreshSecret,
            @Value("${app.jwt.minutes}") Integer minutes,
            @Value("${app.jwt.days}") Integer days
    ) {
        this.JWT_ACCESS_SECRET = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.JWT_REFRESH_SECRET = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.MINUTES = minutes;
        this.DAYS = days;
    }

    @Override
    public String generateAccessToken(@NonNull UserCredentials user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant accessExpirationInstant = now.plusMinutes(MINUTES).atZone(ZoneId.systemDefault()).toInstant();
        final Date accessExpiration = Date.from(accessExpirationInstant);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(accessExpiration)
                .signWith(JWT_ACCESS_SECRET)
                .compact();
    }

    @Override
    public String generateRefreshToken(@NonNull UserCredentials user) {
        final LocalDateTime now = LocalDateTime.now();
        final Instant refreshExpirationInstant = now.plusDays(DAYS).atZone(ZoneId.systemDefault()).toInstant();
        final Date refreshExpiration = Date.from(refreshExpirationInstant);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setExpiration(refreshExpiration)
                .signWith(JWT_REFRESH_SECRET)
                .compact();
    }

    @Override
    public boolean isValidAccessToken(String accessToken) {
        return isValidToken(accessToken, JWT_ACCESS_SECRET);
    }

    @Override
    public boolean isValidRefreshToken(String refreshToken) {
        return isValidToken(refreshToken, JWT_REFRESH_SECRET);
    }

    private boolean isValidToken(String token, @NonNull Key secret) {
        String maskedToken = maskToken(token);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException expEx) {
            log.warn("Token is expired: {}", maskedToken);
        } catch (UnsupportedJwtException unsEx) {
            log.error("Unsupported jwt: {}", maskedToken, unsEx);
        } catch (MalformedJwtException mjEx) {
            log.warn("Malformed jwt: {}", maskedToken);
        } catch (SignatureException sEx) {
            log.error("Invalid signature", sEx);
        } catch (IndexOutOfBoundsException e) {
            log.warn("Invalid token: {}", maskedToken);
        }
        return false;
    }

    @Override
    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, JWT_ACCESS_SECRET);
    }

    @Override
    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, JWT_REFRESH_SECRET);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        String maskedToken = maskToken(token);
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Error while parsing claims from token: {}", maskedToken, e);
            throw new AuthException("Error while parsing claims from token");
        }
    }

    @Override
    public String maskToken(String token) {
        if (token.length() > 8) {
            return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
        }
        return token;
    }
}