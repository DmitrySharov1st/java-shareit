package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional          // каждои тест будет откатывать изменения в БД
@ActiveProfiles("test") // используем application-test.properties с H2
class UserControllerEmailTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnConflictWhenDuplicateEmail() throws Exception {
        // Создаём первого пользователя
        UserDto userDto1 = UserDto.builder()
                .name("User One")
                .email("duplicate@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk());

        // Пытаемся создать второго с тем же email
        UserDto userDto2 = UserDto.builder()
                .name("User Two")
                .email("duplicate@example.com")
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("Email duplicate@example.com is already in use"));
    }

    @Test
    void shouldReturnConflictWhenUpdateWithDuplicateEmail() throws Exception {
        // Создаём первого пользователя
        UserDto userDto1 = UserDto.builder()
                .name("User One")
                .email("user1@example.com")
                .build();

        String response1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser1 = objectMapper.readValue(response1, UserDto.class);

        // Создаём второго пользователя
        UserDto userDto2 = UserDto.builder()
                .name("User Two")
                .email("user2@example.com")
                .build();

        String response2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto2)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserDto createdUser2 = objectMapper.readValue(response2, UserDto.class);

        // Пытаемся обновить второго пользователя, установив email первого
        UserDto updateDto = UserDto.builder()
                .email("user1@example.com")
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
        // Создаём пользователя
        UserDto userDto = UserDto.builder()
                .name("User One")
                .email("same@example.com")
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
                .email("same@example.com")
                .build();

        mockMvc.perform(patch("/users/{userId}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("same@example.com"));
    }
}