package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.UserValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserValidator userValidator;

    @Override
    public UserDto create(UserDto dto) {
        userValidator.validateEmailIsUnique(dto.getEmail(), null);
        User user = UserMapper.toModel(dto);
        return UserMapper.toDto(repository.save(user));
    }

    @Override
    public UserDto update(UserDto dto) {
        User existing = userValidator.validateUserExists(dto.getId());

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            userValidator.validateEmailIsUnique(dto.getEmail(), dto.getId());
            existing.setEmail(dto.getEmail());
        }
        return UserMapper.toDto(repository.save(existing));
    }

    @Override
    public Optional<UserDto> getById(Long id) {
        return repository.findById(id)
                .map(UserMapper::toDto);
    }

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}