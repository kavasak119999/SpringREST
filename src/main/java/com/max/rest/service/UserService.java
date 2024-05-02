package com.max.rest.service;

import com.max.rest.dto.User;
import com.max.rest.dto.UserCredentials;
import com.max.rest.dto.UserRequest;
import com.max.rest.dto.UserUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

public interface UserService {
    UserCredentials getUserCredentialsByEmail(String email);

    Page<User> getAllUsers(PageRequest pageRequest);

    Page<User> searchUsers(LocalDate fromDate, LocalDate toDate, PageRequest pageRequest);

    User updateUser(Long id, UserRequest userRequest);

    User saveUser(UserRequest userRequest);

    User partialUpdateUser(Long id, UserUpdateRequest request);

    User getUserById(Long id);

    void deleteUserById(Long id);

    boolean isValidBirthDate(LocalDate birthDate, int minAge);
}
