package com.max.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 255)
    private String email;

    @Pattern(regexp = "^[a-zA-Z0-9А-Яа-я]{8,20}$")
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Past
    private LocalDate birthDate;

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^\\+?\\d{10,13}$")
    private String phoneNumber;
}
