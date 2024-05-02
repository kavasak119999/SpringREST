package com.max.test.service;

import com.max.test.dto.*;
import com.max.test.exception.AuthException;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProviderService jwtProvider;

    @Override
    public JwtLoginResponse login(@NonNull JwtRequest authRequest) {
        if (authRequest.getEmail() == null || authRequest.getPassword() == null)
            throw new IllegalArgumentException("Email and password cannot be empty");

        final UserCredentials user = userService.getUserCredentialsByEmail(authRequest.getEmail());

        if (user == null || !BCrypt.checkpw(authRequest.getPassword(), user.getPassword()))
            throw new AuthException("Invalid email or password");

        final String accessToken = jwtProvider.generateAccessToken(user);
        final String refreshToken = jwtProvider.generateRefreshToken(user);
        refreshStorage.put(user.getEmail(), refreshToken);

        return new JwtLoginResponse(accessToken, refreshToken);
    }

    @Override
    public JwtAccessResponse getAccessToken(@NonNull String refreshToken) {
        if (!jwtProvider.isValidRefreshToken(refreshToken))
            throw new AuthException("Invalid refresh token");

        Claims claims = jwtProvider.getRefreshClaims(refreshToken);
        String email = claims.getSubject();
        String saveRefreshToken = refreshStorage.get(email);

        if (saveRefreshToken == null || !saveRefreshToken.equals(refreshToken))
            throw new AuthException("Mismatched or expired refresh token");

        UserCredentials user = userService.getUserCredentialsByEmail(email);
        String accessToken = jwtProvider.generateAccessToken(user);
        return new JwtAccessResponse(accessToken);
    }

    @Override
    public JwtRefreshResponse getRefreshToken(@NonNull String refreshToken) {
        if (!jwtProvider.isValidRefreshToken(refreshToken))
            throw new AuthException("Invalid refresh token");

        Claims claims = jwtProvider.getRefreshClaims(refreshToken);
        String email = claims.getSubject();
        String saveRefreshToken = refreshStorage.get(email);

        if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
            UserCredentials user = userService.getUserCredentialsByEmail(email);
            String newRefreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(user.getEmail(), newRefreshToken);

            return new JwtRefreshResponse(newRefreshToken);
        } else
            throw new AuthException("Mismatched or expired refresh token");

    }

}