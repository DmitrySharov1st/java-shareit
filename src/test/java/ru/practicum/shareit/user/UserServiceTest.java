package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUserShouldReturnCreatedUserWhenValidUser() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.create(user);

        assertNotNull(createdUser);
        assertEquals("Test User", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals(1L, createdUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserShouldThrowConflictExceptionWhenDuplicateEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.create(user));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserShouldReturnUpdatedUserWhenValidUpdate() {
        User updatedData = User.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updatedData.getEmail())).thenReturn(false);

        User result = userService.update(updatedData);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        // Проверяем, что исходный объект (user) тоже обновился (опционально)
        assertEquals("Updated Name", user.getName());
        assertEquals("updated@example.com", user.getEmail());
    }

    @Test
    void updateUserShouldThrowNotFoundExceptionWhenNonExistentUser() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        User nonExistentUser = User.builder().id(999L).name("Any").build();

        assertThrows(NotFoundException.class, () -> userService.update(nonExistentUser));
    }

    @Test
    void getUserByIdShouldReturnUserWhenValidId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
    }

    @Test
    void getUserByIdShouldThrowNotFoundExceptionWhenNonExistentId() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAllUsersShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void deleteUserShouldDeleteUserWhenValidId() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUserShouldThrowNotFoundExceptionWhenNonExistentId() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }
}