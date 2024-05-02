package com.max.test.controller;

import com.max.test.dto.*;
import com.max.test.exception.InvalidTokenException;
import com.max.test.service.AuthService;
import com.max.test.service.JwtProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProviderService jwtProvider;

    @PostMapping("/authenticate")
    public ResponseEntity<JwtLoginResponse> login(@RequestBody JwtRequest authRequest) {
        log.info("Received authentication request with email: {}", authRequest.getEmail());

        final JwtLoginResponse token = authService.login(authRequest);
        String accessToken = token.getAccessToken();
        String maskedAccessToken = jwtProvider.maskToken(accessToken);

        log.info("Authentication successful, access token issued: {}", maskedAccessToken);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh-access-token")
    public ResponseEntity<JwtAccessResponse> getNewAccessToken(@RequestBody JwtRefreshRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("Received access token refresh request with ID: {}", requestId);

        final JwtAccessResponse token = authService.getAccessToken(request.getRefreshToken());

        log.info("Access token refreshed for request with ID: {}", requestId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/renew-refresh-token")
    public ResponseEntity<JwtRefreshResponse> getNewRefreshToken(@RequestBody JwtRefreshRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("Received refresh token refresh request with ID: {}", requestId);

        final JwtRefreshResponse token = authService.getRefreshToken(request.getRefreshToken());

        log.info("Refresh token refreshed for request with ID: {}", requestId);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/validateToken")
    public ResponseEntity<Void> validateAccessToken(@RequestBody JwtAccessRequest request) {
        String requestId = UUID.randomUUID().toString();
        log.info("Received access token validation request with ID: {}", requestId);

        if (request.getAccessToken() != null && jwtProvider.isValidAccessToken(request.getAccessToken())) {
            log.info("Access token is valid for ID: {}", requestId);
            return ResponseEntity.ok().build();
        }

        throw new InvalidTokenException("Invalid access token");
    }
}