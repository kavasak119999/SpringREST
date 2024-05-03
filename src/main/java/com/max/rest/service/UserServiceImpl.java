package com.max.rest.service;

import com.max.rest.dto.User;
import com.max.rest.dto.UserCredentials;
import com.max.rest.dto.UserRequest;
import com.max.rest.dto.UserUpdateRequest;
import com.max.rest.entity.UserEntity;
import com.max.rest.exception.NotFoundException;
import com.max.rest.exception.RegistrationException;
import com.max.rest.exception.ValidationException;
import com.max.rest.repository.UserRepository;
import com.max.rest.utils.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.function.Consumer;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User updateUser(Long id, UserRequest userRequest) {
        UserEntity userEntity = userRepository
                .findById(id).orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        String email = userRequest.getEmail();
        if (email != null && userRepository.existsByEmail(email) && !email.equals(userEntity.getEmail())) {
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation failed",
                    "User with email '" + email + " already registered")));
        }

        // Update data for user from DTO
        BeanUtils.copyProperties(userRequest, userEntity);

        userEntity.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        userEntity = userRepository.save(userEntity);

        return userMapper.userEntityToUser(userEntity);
    }

    @Override
    public User saveUser(UserRequest userRequest) {
        String email = userRequest.getEmail();
        if (userRepository.existsByEmail(email))
            throw new RegistrationException(
                    "User with email '" + email + "' already registered");

        UserEntity userEntity = userRepository.save(userMapper.userRequestToUserEntity(userRequest));

        return userMapper.userEntityToUser(userEntity);
    }

    @Override
    public Page<User> getAllUsers(PageRequest pageRequest) {
        Page<UserEntity> entities = userRepository.findAll(pageRequest);
        return entities.map(userMapper::userEntityToUser);
    }

    @Override
    public void deleteUserById(Long id) {
        if (userRepository.existsById(id))
            userRepository.deleteById(id);
        else
            throw new NotFoundException("User with id '" + id + "' not found");
    }

    @Override
    public User partialUpdateUser(Long id, UserUpdateRequest userRequest) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        String email = userRequest.getEmail();
        if (email != null && !email.equals(existingUser.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation failed",
                    "User with email '" + email + " already registered")));
        }

        if (userRequest.getPassword() != null) {
            updateField(passwordEncoder.encode(userRequest.getPassword()), existingUser::setPassword);
        }

        updateField(userRequest.getEmail(), existingUser::setEmail);
        updateField(userRequest.getFirstName(), existingUser::setFirstName);
        updateField(userRequest.getLastName(), existingUser::setLastName);
        updateField(userRequest.getBirthDate(), existingUser::setBirthDate);
        updateField(userRequest.getAddress(), existingUser::setAddress);
        updateField(userRequest.getPhoneNumber(), existingUser::setPhoneNumber);

        return userMapper.userEntityToUser(existingUser);
    }

    @Override
    public Page<User> searchUsers(LocalDate fromDate, LocalDate toDate, PageRequest pageRequest) {
        return userRepository.findAllByBirthDateBetween(fromDate, toDate, pageRequest)
                .map(userMapper::userEntityToUser);
    }

    @Override
    public UserCredentials getUserCredentialsByEmail(String email) {
        UserEntity entity = userRepository
                .findByEmail(email).orElseThrow(() -> new NotFoundException(
                        "User with email '" + email + "' not found"));

        return UserCredentials.builder()
                .email(email)
                .password(entity.getPassword())
                .build();
    }

    @Override
    public User getUserById(Long id) {
        UserEntity userEntity = userRepository
                .findById(id).orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        return userMapper.userEntityToUser(userEntity);
    }

    @Override
    public boolean isValidBirthDate(LocalDate birthDate, int minAge) {
        LocalDate minBirthDate = LocalDate.now().minus(Period.ofYears(minAge));
        return minBirthDate.isAfter(birthDate);
    }

    private <T> void updateField(T field, Consumer<T> setter) {
        if (field instanceof String && StringUtils.hasText((String) field) || field != null) {
            setter.accept(field);
        }
    }

}