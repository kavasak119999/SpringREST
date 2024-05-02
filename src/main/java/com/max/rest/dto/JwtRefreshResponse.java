package com.max.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtRefreshResponse {
    private String refreshToken;
}
