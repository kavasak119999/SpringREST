package com.max.test.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequest {
    private String email;
    private String password;
}
