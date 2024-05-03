package com.max.rest.service;

import com.max.rest.dto.*;

public interface AuthService {
    JwtLoginResponse login(UserCredentials authRequest);

    JwtAccessResponse getAccessToken(String refreshToken);

    JwtRefreshResponse getRefreshToken(String refreshToken);
}
