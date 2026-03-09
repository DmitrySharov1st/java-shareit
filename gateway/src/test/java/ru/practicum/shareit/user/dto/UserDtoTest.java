package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeUserDto() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@test.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.name", "John Doe");
        assertThat(result).hasJsonPathStringValue("$.email", "john@test.com");
    }

    @Test
    void deserializeUserDto() throws Exception {
        String content = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@test.com\"}";

        UserDto userDto = objectMapper.readValue(content, UserDto.class);

        assertThat(userDto.getId()).isEqualTo(1L);
        assertThat(userDto.getName()).isEqualTo("John Doe");
        assertThat(userDto.getEmail()).isEqualTo("john@test.com");
    }
}