package com.max.rest.utils;

import com.max.rest.dto.User;
import com.max.rest.dto.UserRequest;
import com.max.rest.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(source = "password", target = "password", qualifiedByName = "encodePassword")
    public abstract UserEntity userRequestToUserEntity(UserRequest userRequest);

    public abstract User userEntityToUser(UserEntity userEntity);

    @Named("encodePassword")
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}