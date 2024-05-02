package com.max.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtAccessResponse {
    private final String type = "Bearer";
    private String accessToken;
}
