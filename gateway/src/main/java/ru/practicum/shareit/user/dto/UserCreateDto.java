package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @Size(max = 200, message = "Email должен быть не длиннее 200 символов")
    @Email(message = "Некорректный формат электронной почты")
    private String email;
}
