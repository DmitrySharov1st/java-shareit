package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.lang.reflect.Field;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerEmailTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {
        // Очищаем базу пользователей перед каждым тестом
        clearUserDatabase();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Очищаем базу пользователей после каждого теста
        clearUserDatabase();
    }

    private void clearUserDatabase() throws Exception {
        // Используем Reflection для очистки внутренней Map
        Field usersField = UserServiceImpl.class.getDeclaredField("users");
        usersField.setAccessible(true);
        Map<?, ?> users = (Map<?, ?>) usersField.get(userService);
        users.clear();

        Field nextIdField = UserServiceImpl.class.getDeclaredField("nextId");
        nextIdField.setAccessible(true);
        nextIdField.set(userService, 1L);
    }

    @Test
    void shouldReturnConflictWhenDuplicateEmail() throws Exception {
        UserDto userDto1 = UserDto.builder()
                .name("User One")
                .email("test1@example.com") // Уникальный email для этого теста
                .build();

        UserDto userDto2 = UserDto.builder()
                .name("User Two")
                .email("test1@example.com") // Тот же email
                .build();

        // Создаем первого пользователя
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk());

        // Пытаемся создать второго пользователя с тем же email
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email test1@example.com is already in use"));
    }

    @Test
    void shouldReturnConflictWhenUpdateWithDuplicateEmail() throws Exception {
        // Создаем первого пользователя
        UserDto userDto1 = UserDto.builder()
                .name("User One")
                .email("user1@example.com") // Уникальный email
                .build();

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser1 = objectMapper.readValue(response1, UserDto.class);

        // Создаем второго пользователя с другим email
        UserDto userDto2 = UserDto.builder()
                .name("User Two")
                .email("user2@example.com") // Другой email
                .build();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser2 = objectMapper.readValue(response2, UserDto.class);

        // Пытаемся обновить второго пользователя, установив email первого
        UserDto updateDto = UserDto.builder()
                .email("user1@example.com") // Email первого пользователя
                .build();

        mockMvc.perform(patch("/users/{userId}", createdUser2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("Email user1@example.com is already in use"));
    }

    @Test
    void shouldReturnOkWhenUpdateSameUserWithSameEmail() throws Exception {
        // Создаем пользователя
        UserDto userDto = UserDto.builder()
                .name("User One")
                .email("user@example.com") // Уникальный email
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(response, UserDto.class);

        // Обновляем того же пользователя с тем же email (должно быть разрешено)
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("user@example.com") // Тот же самый email
                .build();

        mockMvc.perform(patch("/users/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void shouldReturnOkWhenUpdateUserNameOnly() throws Exception {
        // Создаем пользователя
        UserDto userDto = UserDto.builder()
                .name("User One")
                .email("user@example.com")
                .build();

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser = objectMapper.readValue(response, UserDto.class);

        // Обновляем только имя, email остается тем же
        UserDto updateDto = UserDto.builder()
                .name("Updated Name Only")
                .build();

        mockMvc.perform(patch("/users/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void shouldReturnOkWhenCreatingUsersWithDifferentEmails() throws Exception {
        // Создаем первого пользователя
        UserDto userDto1 = UserDto.builder()
                .name("User One")
                .email("user1@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user1@example.com"));

        // Создаем второго пользователя с другим email
        UserDto userDto2 = UserDto.builder()
                .name("User Two")
                .email("user2@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user2@example.com"));
    }
}