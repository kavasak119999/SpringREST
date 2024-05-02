package com.max.test.utils;

import com.max.test.dto.UserRequest;
import com.max.test.entity.UserEntity;
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