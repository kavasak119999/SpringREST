package com.max.test.controller;

import com.max.test.dto.User;
import com.max.test.dto.UserRequest;
import com.max.test.dto.UserUpdateRequest;
import com.max.test.exception.ValidationException;
import com.max.test.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Value("${app.minimum-age}")
    private int minAge;

    @Operation(summary = "Create an user",
            description = "Update an existing user. The response is updated User object.")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserRequest request,
                                           BindingResult bindingResult) {
        log.info("Creating new user with email: {}", request.getEmail());
        if (bindingResult.hasErrors()) {
            log.info("Validation failed for user creation request: {}", bindingResult.getAllErrors());
            throw new ValidationException("Validation failed", bindingResult.getAllErrors());
        }

        if (!userService.isValidBirthDate(request.getBirthDate(), minAge)) {
            log.info("Validation failed. Minimum age is {}", minAge);
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation birthdate fail",
                    "Validation failed. Minimum age is " + minAge)));
        }

        User createdUser = userService.saveUser(request);

        log.info("User: {} - created", createdUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Get all users",
            description = "Retrieve all users with pagination.")
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching users, page: {}, size: {}", page, size);

        PageRequest pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(pageable);

        log.info("Users fetched: {}", users.getTotalElements());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID",
            description = "Retrieve a user by their unique ID.")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userService.getUserById(id);

        log.info("User fetched with id: {}", id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update an user",
            description = "Update an existing user. The response is updated User object.")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id,
                                           @RequestBody @Valid UserRequest request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("Validation failed for user update request: {}", bindingResult.getAllErrors());
            throw new ValidationException("Validation failed", bindingResult.getAllErrors());
        }

        if (!userService.isValidBirthDate(request.getBirthDate(), minAge)) {
            log.info("Validation failed. Minimum age is {}", minAge);
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation birthdate fail",
                    "Validation failed. Minimum age is " + minAge)));
        }

        log.info("Updating user with id: {}", id);

        User updatedUser = userService.updateUser(id, request);

        log.info("Data updated for user: {}", id);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Partial update an user",
            description = "Update an existing user. The response is updated User object.")
    @PatchMapping("/{id}")
    public ResponseEntity<User> partialUpdateUser(@PathVariable Long id,
                                                  @RequestBody @Valid UserUpdateRequest request,
                                                  BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation failed for partial user update request: {}", bindingResult.getAllErrors());
            throw new ValidationException("Validation failed", bindingResult.getAllErrors());
        }

        if (request.getBirthDate() != null && !userService.isValidBirthDate(request.getBirthDate(), minAge)) {
            log.info("Validation failed. Minimum age is {}", minAge);
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation birthdate fail",
                    "Validation failed. Minimum age is " + minAge)));
        }

        log.info("Partially updating user with id: {}", id);

        User partiallyUpdatedUser = userService.partialUpdateUser(id, request);

        log.info("Data partially updated for user: {}", partiallyUpdatedUser.getEmail());
        return ResponseEntity.ok(partiallyUpdatedUser);
    }

    @Operation(summary = "Delete an user",
            description = "Delete an existing user.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);

        userService.deleteUserById(id);

        log.info("User deleted with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search users",
            description = "Search users based on from date and to date.")
    @GetMapping("/search")
    public ResponseEntity<Page<User>> searchUsers(
            @DateTimeFormat(pattern = "yyyy.MM.dd") @RequestParam LocalDate fromDate,
            @DateTimeFormat(pattern = "yyyy.MM.dd") @RequestParam LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Searching users from: {}, to: {}, page: {}, size: {}", fromDate, toDate, page, size);

        PageRequest pageable = PageRequest.of(page, size);
        Page<User> users = userService.searchUsers(fromDate, toDate, pageable);

        log.info("Users found: {}", users.getTotalElements());
        return ResponseEntity.ok(users);
    }
}