package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.ItemValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class ItemValidatorTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemValidator validator;

    private User owner;
    private Item item;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        item = new Item();
        item.setName("Book");
        item.setDescription("For IT");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);
    }

    @Test
    void validateItemExistsWhenFound() {
        Item found = validator.validateItemExists(item.getId());
        assertThat(found.getId()).isEqualTo(item.getId());
    }

    @Test
    void validateItemExistsWhenNotFound() {
        long missingId = item.getId() + 999;
        ResponseStatusException ex =
                catchThrowableOfType(() -> validator.validateItemExists(missingId), ResponseStatusException.class);
        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Вещь не найдена");
    }

    @Test
    void validateOwnershipWhenNoOwner() {
        Item noOwner = new Item();
        noOwner.setId(5L);
        noOwner.setName("Book");
        noOwner.setDescription("For IT");
        noOwner.setAvailable(false);

        ResponseStatusException ex =
                catchThrowableOfType(
                        () -> validator.validateOwnership(noOwner, owner.getId()),
                        ResponseStatusException.class
                );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getReason()).contains("отсутствует владелец");
    }

    @Test
    void validateOwnershipWhenNotOwner() {
        User unsaved = new User();
        unsaved.setName("Other");
        unsaved.setEmail("other@example.com");
        User savedOther = userRepository.save(unsaved);

        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateOwnership(item, savedOther.getId()),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Только владелец может редактировать");
    }

    @Test
    void validateOwnershipWhenOwner() {
        assertThatNoException().isThrownBy(() -> validator.validateOwnership(item, owner.getId()));
    }
}
