package com.max.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class JwtLoginResponse {
    private final String type = "Bearer";
    private String accessToken;
    private String refreshToken;
}
