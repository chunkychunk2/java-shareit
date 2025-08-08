package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.validation.UserValidator;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class UserValidatorTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserValidator validator;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = new User();
        user.setName("Alex");
        user.setEmail("alex@example.com");
        user = userRepository.save(user);
    }

    @Test
    void validateUserExistsWhenExists() {
        User found = validator.validateUserExists(user.getId());
        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getEmail()).isEqualTo("alex@example.com");
    }

    @Test
    void validateUserExistsWhenNotExists() {
        long missingId = user.getId() + 100;
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateUserExists(missingId),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Пользователь не найден");
    }

    @Test
    void validateEmailIsUnique() {
        validator.validateEmailIsUnique("bob@example.com", null);
    }

    @Test
    void validateEmailIsUniqueWhenDuplicateEmail() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateEmailIsUnique("Alex@EXAMPLE.com", null),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
        assertThat(ex.getReason()).contains("Email уже используется");
    }

    @Test
    void validateEmailIsUniqueWhenDuplicateButExcludedById() {
        validator.validateEmailIsUnique("alex@example.com", user.getId());
    }
}
