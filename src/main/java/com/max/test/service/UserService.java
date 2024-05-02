package com.max.test.service;

import com.max.test.dto.User;
import com.max.test.dto.UserCredentials;
import com.max.test.dto.UserRequest;
import com.max.test.dto.UserUpdateRequest;
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
