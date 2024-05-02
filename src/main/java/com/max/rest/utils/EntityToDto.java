package com.max.rest.utils;

import com.max.rest.dto.User;
import com.max.rest.entity.UserEntity;

public class EntityToDto {

    public static User userEntityToDto(UserEntity userEntity) {
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .birthDate(userEntity.getBirthDate())
                .address(userEntity.getAddress())
                .phoneNumber(userEntity.getPhoneNumber())
                .build();
    }
}
