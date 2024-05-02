package com.max.rest.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCredentials {
    private Long id;
    private String email;
    private String password;
}
