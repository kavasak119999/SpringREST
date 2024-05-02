package com.max.rest.utils;

import com.max.rest.dto.UserRequest;
import com.max.rest.entity.UserEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class DtoToEntity {

    public static UserEntity userDtoToEntity(UserRequest user) {
        String hash = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

        return UserEntity.builder()
                .email(user.getEmail())
                .password(hash)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .birthDate(user.getBirthDate())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}