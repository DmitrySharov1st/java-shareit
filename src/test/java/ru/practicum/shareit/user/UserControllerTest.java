package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;

    private static final String USERS_PATH = "/users";
    private static final String USER_ID_PATH = "/users/{userId}";

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void shouldCreateUserWhenValidUser() throws Exception {
        when(userService.create(any())).thenReturn(UserMapper.toUser(userDto));

        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenUserNameEmpty() throws Exception {
        userDto.setName("");

        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidEmail() throws Exception {
        userDto.setEmail("invalid-email");

        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateUserWhenValidData() throws Exception {
        UserDto updatedUser = UserDto.builder()
                .id(1L)
                .name("John Updated")
                .email("john.updated@example.com")
                .build();

        when(userService.update(any())).thenReturn(UserMapper.toUser(updatedUser));

        mockMvc.perform(patch(USER_ID_PATH, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    void shouldReturnUserWhenValidId() throws Exception {
        when(userService.getById(1L)).thenReturn(UserMapper.toUser(userDto));

        mockMvc.perform(get(USER_ID_PATH, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenNonExistentUserId() throws Exception {
        when(userService.getById(999L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get(USER_ID_PATH, 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllUsers() throws Exception {
        mockMvc.perform(get(USERS_PATH))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteUserWhenValidId() throws Exception {
        mockMvc.perform(delete(USER_ID_PATH, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnConflictWhenDuplicateEmail() throws Exception {
        // Создаем первого пользователя
        when(userService.create(any())).thenReturn(UserMapper.toUser(userDto));

        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());

        // Пытаемся создать второго пользователя с тем же email
        when(userService.create(any())).thenThrow(new ConflictException("Email already exists"));

        mockMvc.perform(post(USERS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }
}