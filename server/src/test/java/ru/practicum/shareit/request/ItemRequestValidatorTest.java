package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.validation.ItemRequestValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class ItemRequestValidatorTest {

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private ItemRequestValidator validator;

    @Autowired
    private UserRepository userRepository;

    private ItemRequest savedRequest;
    private User requester;

    @BeforeEach
    void setup() {
        requestRepository.deleteAll();
        userRepository.deleteAll();

        requester = new User();
        requester.setName("Alex");
        requester.setEmail("alex@example.com");
        requester = userRepository.save(requester);

        ItemRequest r = new ItemRequest();
        r.setDescription("Need a book");
        r.setRequester(requester);
        r.setCreated(LocalDateTime.now());
        savedRequest = requestRepository.save(r);
    }

    @Test
    void validateRequestExistsWhenFound() {
        ItemRequest found = validator.validateRequestExists(savedRequest.getId());
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(savedRequest.getId());
        assertThat(found.getDescription()).isEqualTo("Need a book");
    }

    @Test
    void validateRequestExistsWhenNotFound() {
        long missingId = savedRequest.getId() + 100;
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateRequestExists(missingId),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Запрос не найден");
    }
}
