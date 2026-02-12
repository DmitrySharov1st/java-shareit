package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserServiceImpl userService;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void createUserShouldReturnCreatedUserWhenValidUser() {
        User createdUser = userService.create(user);

        assertNotNull(createdUser);
        assertEquals("Test User", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertNotNull(createdUser.getId());
    }

    @Test
    void updateUserShouldReturnUpdatedUserWhenDuplicateId() {
        User createdUser = userService.create(user);
        User duplicateUser = User.builder()
                .id(createdUser.getId())
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        User updatedUser = userService.update(duplicateUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void updateUserShouldReturnUpdatedUserWhenPartialUpdate() {
        userService.create(user);

        User partialUpdate = User.builder()
                .id(1L)
                .name("New Name")
                .build();

        User result = userService.update(partialUpdate);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void updateUserShouldThrowNotFoundExceptionWhenNonExistentUser() {
        User nonExistentUser = User.builder()
                .id(999L)
                .name("Non Existent")
                .email("nonexistent@example.com")
                .build();

        assertThrows(NotFoundException.class, () -> userService.update(nonExistentUser));
    }

    @Test
    void getUserByIdShouldReturnUserWhenValidId() {
        userService.create(user);

        User foundUser = userService.getById(1L);

        assertNotNull(foundUser);
        assertEquals(1L, foundUser.getId());
        assertEquals("Test User", foundUser.getName());
    }

    @Test
    void getUserByIdShouldThrowNotFoundExceptionWhenNonExistentId() {
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
    }

    @Test
    void getAllUsersShouldReturnUserList() {
        userService.create(user);

        List<User> users = userService.getAll();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("Test User", users.get(0).getName());
    }

    @Test
    void deleteUserShouldDeleteUserWhenValidId() {
        userService.create(user);

        userService.delete(1L);

        assertThrows(NotFoundException.class, () -> userService.getById(1L));
    }

    @Test
    void deleteUserShouldThrowNotFoundExceptionWhenNonExistentId() {
        assertThrows(NotFoundException.class, () -> userService.delete(999L));
    }

    @Test
    void createUserShouldThrowConflictExceptionWhenDuplicateEmail() {
        userService.create(user);

        User duplicateUser = User.builder()
                .name("Another User")
                .email("test@example.com") // Тот же email
                .build();

        assertThrows(ConflictException.class, () -> userService.create(duplicateUser));
    }

    @Test
    void updateUserShouldThrowConflictExceptionWhenDuplicateEmail() {
        User user1 = User.builder()
                .name("User One")
                .email("user1@example.com")
                .build();
        User createdUser1 = userService.create(user1);

        User user2 = User.builder()
                .name("User Two")
                .email("user2@example.com")
                .build();
        User createdUser2 = userService.create(user2);

        // Пытаемся обновить второго пользователя, установив email первого
        User updateUser2 = User.builder()
                .id(createdUser2.getId())
                .email("user1@example.com") // Email первого пользователя
                .build();

        assertThrows(ConflictException.class, () -> userService.update(updateUser2));
    }
}