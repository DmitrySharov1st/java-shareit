package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class User {
    Long id;

    @NotBlank(message = "Имя должно быть заполнено")
    String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    String email;
}