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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserServiceImplTest {

    @Value("${app.minimum-age}")
    private int minAge;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<UserEntity> userEntityCaptor;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void testUpdateUser() {
        // Arrange
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("test@example.com", "password", "John", "Doe",
                LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        UserEntity existingUser = new UserEntity(userId, "test@example.com",
                BCrypt.hashpw(userRequest.getPassword(), BCrypt.gensalt()), "John", "Doe",
                LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(userId);
            entity.setEmail(userRequest.getEmail());
            entity.setPassword(generateHash(userRequest.getPassword()));
            entity.setFirstName(userRequest.getFirstName());
            entity.setLastName(userRequest.getLastName());
            entity.setBirthDate(userRequest.getBirthDate());
            entity.setAddress(userRequest.getAddress());
            entity.setPhoneNumber(userRequest.getPhoneNumber());
            return entity;
        });

        // Act
        User user = userService.updateUser(userId, userRequest);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
        verify(userRepository, times(1)).save(userEntityCaptor.capture());
        UserEntity capturedUserEntity = userEntityCaptor.getValue();
        assertTrue(BCrypt.checkpw(userRequest.getPassword(), capturedUserEntity.getPassword()));
        assertEquals(user.getEmail(), capturedUserEntity.getEmail());
        assertEquals(user.getFirstName(), capturedUserEntity.getFirstName());
        assertEquals(user.getLastName(), capturedUserEntity.getLastName());
        assertEquals(user.getBirthDate(), capturedUserEntity.getBirthDate());
        assertEquals(user.getAddress(), capturedUserEntity.getAddress());
        assertEquals(user.getPhoneNumber(), capturedUserEntity.getPhoneNumber());
    }

    @Test
    void testUpdateUser_EmailAlreadyExists() {
        // Arrange
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("test@example.com", "password", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        UserEntity existingUser = new UserEntity(userId, "old@example.com", "hashedPassword", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.updateUser(userId, userRequest));
    }

    @Test
    void testSaveUser() {
        // Arrange
        Long userId = 1L;
        UserRequest userRequest = new UserRequest("test@example.com", "password", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            entity.setId(userId);
            entity.setEmail(userRequest.getEmail());
            entity.setPassword(generateHash(userRequest.getPassword()));
            entity.setFirstName(userRequest.getFirstName());
            entity.setLastName(userRequest.getLastName());
            entity.setBirthDate(userRequest.getBirthDate());
            entity.setAddress(userRequest.getAddress());
            entity.setPhoneNumber(userRequest.getPhoneNumber());
            return entity;
        });

        // Act
        User user = userService.saveUser(userRequest);

        // Assert
        verify(userRepository, times(1)).existsByEmail(userRequest.getEmail());
        verify(userRepository, times(1)).save(userEntityCaptor.capture());
        UserEntity capturedUserEntity = userEntityCaptor.getValue();
        assertNotNull(capturedUserEntity.getId());
        assertEquals(user.getId(), capturedUserEntity.getId());
        assertEquals(user.getEmail(), capturedUserEntity.getEmail());
        assertTrue(BCrypt.checkpw(userRequest.getPassword(), capturedUserEntity.getPassword()));
        assertEquals(user.getFirstName(), capturedUserEntity.getFirstName());
        assertEquals(user.getLastName(), capturedUserEntity.getLastName());
        assertEquals(user.getBirthDate(), capturedUserEntity.getBirthDate());
        assertEquals(user.getAddress(), capturedUserEntity.getAddress());
        assertEquals(user.getPhoneNumber(), capturedUserEntity.getPhoneNumber());
    }

    @Test
    void testSaveUser_EmailAlreadyExists() {
        // Arrange
        UserRequest userRequest = new UserRequest("test@example.com", "password", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(RegistrationException.class, () -> userService.saveUser(userRequest));
    }

    @Test
    void testGetAllUsers() {
        // Arrange
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(new UserEntity(1L, "test1@example.com", "hashedPassword1", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890"));
        userEntities.add(new UserEntity(2L, "test2@example.com", "hashedPassword2", "Jane", "Smith", LocalDate.of(1995, 5, 15), "456 Oak Ave", "0987654321"));
        Page<UserEntity> userEntitiesPage = new PageImpl<>(userEntities);
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userEntitiesPage);

        // Act
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> userPage = userService.getAllUsers(pageRequest);

        // Assert
        verify(userRepository, times(1)).findAll(pageRequest);
        assertEquals(2, userPage.getTotalElements());
        List<User> users = userPage.getContent();
        assertEquals(userEntities.get(0).getId(), users.get(0).getId());
        assertEquals(userEntities.get(0).getEmail(), users.get(0).getEmail());
        assertEquals(userEntities.get(1).getId(), users.get(1).getId());
        assertEquals(userEntities.get(1).getEmail(), users.get(1).getEmail());
    }

    @Test
    void testDeleteUserById() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        // Act
        userService.deleteUserById(userId);

        // Assert
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUserById_UserNotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.deleteUserById(userId));
    }

    @Test
    void testPartialUpdateUser() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest("new@example.com", "newPassword",
                "NewFirst", "NewLast", LocalDate.of(1995, 10, 20), "789 Elm St", "5678901234");
        UserEntity existingUser = new UserEntity(userId, "old@example.com", "oldPassword",
                "OldFirst", "OldLast", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);

        // Act
        User updatedUser = userService.partialUpdateUser(userId, updateRequest);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).existsByEmail(updateRequest.getEmail());
        assertEquals(updateRequest.getEmail(), updatedUser.getEmail());
        assertNotEquals(updateRequest.getPassword(), existingUser.getPassword());
        assertEquals(updateRequest.getFirstName(), updatedUser.getFirstName());
        assertEquals(updateRequest.getLastName(), updatedUser.getLastName());
        assertEquals(updateRequest.getBirthDate(), updatedUser.getBirthDate());
        assertEquals(updateRequest.getAddress(), updatedUser.getAddress());
        assertEquals(updateRequest.getPhoneNumber(), updatedUser.getPhoneNumber());
    }

    @Test
    void testPartialUpdateUser_EmailAlreadyExists() {
        // Arrange
        Long userId = 1L;
        UserUpdateRequest updateRequest = new UserUpdateRequest("new@example.com", "newPassword",
                "NewFirst", "NewLast", LocalDate.of(1995, 10, 20), "789 Elm St", "5678901234");
        UserEntity existingUser = new UserEntity(userId, "old@example.com", "oldPassword",
                "OldFirst", "OldLast", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.partialUpdateUser(userId, updateRequest));
    }

    @Test
    void testSearchUsers() {
        // Arrange
        LocalDate fromDate = LocalDate.of(1990, 1, 1);
        LocalDate toDate = LocalDate.of(2000, 12, 31);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<UserEntity> userEntities = new ArrayList<>();
        userEntities.add(new UserEntity(1L, "test1@example.com", "hashedPassword1",
                "John", "Doe", LocalDate.of(1992, 5, 10), "123 Main St", "1234567890"));
        userEntities.add(new UserEntity(2L, "test2@example.com", "hashedPassword2",
                "Jane", "Smith", LocalDate.of(1995, 8, 15), "456 Oak Ave", "0987654321"));
        Page<UserEntity> userEntitiesPage = new PageImpl<>(userEntities);
        when(userRepository.findAllByBirthDateBetween(fromDate, toDate, pageRequest)).thenReturn(userEntitiesPage);

        // Act
        Page<User> userPage = userService.searchUsers(fromDate, toDate, pageRequest);

        // Assert
        verify(userRepository, times(1)).findAllByBirthDateBetween(fromDate, toDate, pageRequest);
        assertEquals(2, userPage.getTotalElements());
        List<User> users = userPage.getContent();
        System.out.println();
        assertEquals(userEntities.get(0).getId(), users.get(0).getId());
        assertEquals(userEntities.get(0).getEmail(), users.get(0).getEmail());
        assertEquals(userEntities.get(1).getId(), users.get(1).getId());
        assertEquals(userEntities.get(1).getEmail(), users.get(1).getEmail());
    }

    @Test
    void testGetUserCredentialsByEmail() {
        // Arrange
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity(1L, email, "hashedPassword", "John", "Doe",
                LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // Act
        UserCredentials userCredentials = userService.getUserCredentialsByEmail(email);

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
        assertEquals(userEntity.getId(), userCredentials.getId());
        assertEquals(email, userCredentials.getEmail());
        assertEquals(userEntity.getPassword(), userCredentials.getPassword());
    }

    @Test
    void testGetUserCredentialsByEmail_UserNotFound() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.getUserCredentialsByEmail(email));
    }

    @Test
    void testGetUserById() {
        // Arrange
        Long userId = 1L;
        UserEntity userEntity = new UserEntity(userId, "test@example.com",
                "hashedPassword", "John", "Doe", LocalDate.of(1990, 1, 1), "123 Main St", "1234567890");
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // Act
        User user = userService.getUserById(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        assertEquals(userEntity.getId(), user.getId());
        assertEquals(userEntity.getEmail(), user.getEmail());
        assertEquals(userEntity.getFirstName(), user.getFirstName());
        assertEquals(userEntity.getLastName(), user.getLastName());
        assertEquals(userEntity.getBirthDate(), user.getBirthDate());
        assertEquals(userEntity.getAddress(), user.getAddress());
        assertEquals(userEntity.getPhoneNumber(), user.getPhoneNumber());
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void testIsValidBirthDate() {
        // Arrange
        LocalDate validBirthDate = LocalDate.now().minusYears(20);
        LocalDate invalidBirthDate = LocalDate.now().minusYears(1);

        // Act & Assert
        assertTrue(userService.isValidBirthDate(validBirthDate, minAge));
        assertFalse(userService.isValidBirthDate(invalidBirthDate, minAge));
    }

    private String generateHash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}