package ru.practicum.shareit.user.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    public User validateUserExists(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + userId));
    }

    public void validateEmailIsUnique(String email, Long excludeId) {
        boolean exists = userRepository.findAll().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email) && (excludeId == null || !user.getId().equals(excludeId)));
        if (exists) {
            throw new EntityNotFoundException("Email уже используется: " + email);
        }
    }
}
