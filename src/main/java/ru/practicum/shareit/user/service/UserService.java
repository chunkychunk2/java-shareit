package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserDto create(UserDto userDto);

    UserDto update(UserDto userDto);

    Optional<UserDto> getById(Long id);

    List<UserDto> getAll();

    void delete(Long id);
}
