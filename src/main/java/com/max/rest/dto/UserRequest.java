package com.max.rest.dto;

import lombok.*;

import javax.validation.constraints.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9А-Яа-я]{8,20}$")
    private String password;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotNull
    @Past
    private LocalDate birthDate;

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^\\+?\\d{10,13}$")
    private String phoneNumber;
}
