package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;

    @Size(min = 1, max = 200, message = "Имя не может быть пустым")
    String name;

    @Size(max = 200, message = "Email должен быть не длиннее 200 символов")
    @Email(message = "Некорректный формат электронной почты")
    String email;
}
