package com.max.rest.service;

import com.max.rest.dto.UserCredentials;
import io.jsonwebtoken.Claims;

public interface JwtProviderService {
    String generateAccessToken(UserCredentials user);
    String generateRefreshToken(UserCredentials user);
    boolean isValidAccessToken(String accessToken);
    boolean isValidRefreshToken(String refreshToken);
    Claims getAccessClaims(String token);
    Claims getRefreshClaims(String token);
    String maskToken(String token);
}
