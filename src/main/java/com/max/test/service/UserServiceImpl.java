package com.max.test.service;

import com.max.test.dto.User;
import com.max.test.dto.UserCredentials;
import com.max.test.dto.UserRequest;
import com.max.test.dto.UserUpdateRequest;
import com.max.test.entity.UserEntity;
import com.max.test.exception.NotFoundException;
import com.max.test.exception.RegistrationException;
import com.max.test.exception.ValidationException;
import com.max.test.repository.UserRepository;
import com.max.test.utils.DtoToEntity;
import com.max.test.utils.EntityToDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;
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

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User updateUser(Long id, UserRequest request) {
        UserEntity userEntity = userRepository
                .findById(id).orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        String email = request.getEmail();
        if (email != null && userRepository.existsByEmail(email) && !email.equals(userEntity.getEmail())) {
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation failed",
                    "User with email '" + email + " already registered")));
        }

        // Update data for user from DTO
        BeanUtils.copyProperties(request, userEntity);

        userEntity.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));

        userEntity = userRepository.save(userEntity);

        return EntityToDto.userEntityToDto(userEntity);
    }

    @Override
    public User saveUser(UserRequest userRequest) {
        String email = userRequest.getEmail();
        if (userRepository.existsByEmail(email))
            throw new RegistrationException(
                    "User with email '" + email + "' already registered");

        UserEntity userEntity = userRepository.save(DtoToEntity.userDtoToEntity(userRequest));

        return EntityToDto.userEntityToDto(userEntity);
    }

    @Override
    public Page<User> getAllUsers(PageRequest pageRequest) {
        Page<UserEntity> entities = userRepository.findAll(pageRequest);
        return entities.map(EntityToDto::userEntityToDto);
    }

    @Override
    public void deleteUserById(Long id) {
        if (userRepository.existsById(id))
            userRepository.deleteById(id);
        else
            throw new NotFoundException("User with id '" + id + "' not found");
    }

    @Override
    public User partialUpdateUser(Long id, UserUpdateRequest request) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        String email = request.getEmail();
        if (email != null && !email.equals(existingUser.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ValidationException(Collections.singletonList(new ObjectError("Validation failed",
                    "User with email '" + email + " already registered")));
        }

        if (request.getPassword() != null) {
            updateField(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()), existingUser::setPassword);
        }

        updateField(request.getEmail(), existingUser::setEmail);
        updateField(request.getFirstName(), existingUser::setFirstName);
        updateField(request.getLastName(), existingUser::setLastName);
        updateField(request.getBirthDate(), existingUser::setBirthDate);
        updateField(request.getAddress(), existingUser::setAddress);
        updateField(request.getPhoneNumber(), existingUser::setPhoneNumber);

        return EntityToDto.userEntityToDto(existingUser);
    }

    @Override
    public Page<User> searchUsers(LocalDate fromDate, LocalDate toDate, PageRequest pageRequest) {
        return userRepository.findAllByBirthDateBetween(fromDate, toDate, pageRequest)
                .map(EntityToDto::userEntityToDto);
    }

    @Override
    public UserCredentials getUserCredentialsByEmail(String email) {
        UserEntity entity = userRepository
                .findByEmail(email).orElseThrow(() -> new NotFoundException(
                        "User with email '" + email + "' not found"));

        return UserCredentials.builder()
                .id(entity.getId())
                .email(email)
                .password(entity.getPassword())
                .build();
    }

    @Override
    public User getUserById(Long id) {
        UserEntity entity = userRepository
                .findById(id).orElseThrow(() -> new NotFoundException(
                        "User with id '" + id + "' not found"));

        return EntityToDto.userEntityToDto(entity);
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