package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @Size(min = 1, max = 200, message = "Имя не может быть пустым")
    private String name;

    @Size(max = 200, message = "Email должен быть не длиннее 200 символов")
    @Email(message = "Некорректный формат электронной почты")
    private String email;
}
