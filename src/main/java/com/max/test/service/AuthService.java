package com.max.test.service;

import com.max.test.dto.*;

public interface AuthService {
    JwtLoginResponse login(JwtRequest authRequest);

    JwtAccessResponse getAccessToken(String refreshToken);

    JwtRefreshResponse getRefreshToken(String refreshToken);
}
