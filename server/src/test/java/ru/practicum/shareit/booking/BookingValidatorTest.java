package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.validation.BookingValidator;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class BookingValidatorTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BookingValidator validator;

    private User owner;
    private User booker;
    private Item item;
    private Booking waitingBooking;
    private Booking approvedBooking;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        item = new Item();
        item.setName("Book");
        item.setDescription("For IT");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepository.save(item);

        waitingBooking = new Booking();
        waitingBooking.setBooker(booker);
        waitingBooking.setItem(item);
        waitingBooking.setStart(LocalDateTime.now().plusDays(1));
        waitingBooking.setEnd(LocalDateTime.now().plusDays(2));
        waitingBooking.setStatus(BookingStatus.WAITING);
        waitingBooking = bookingRepository.save(waitingBooking);

        approvedBooking = new Booking();
        approvedBooking.setBooker(booker);
        approvedBooking.setItem(item);
        approvedBooking.setStart(LocalDateTime.now().plusDays(3));
        approvedBooking.setEnd(LocalDateTime.now().plusDays(4));
        approvedBooking.setStatus(BookingStatus.APPROVED);
        approvedBooking = bookingRepository.save(approvedBooking);
    }

    @Test
    void validateBookingExistsWhenFound() {
        Booking found = validator.validateBookingExists(waitingBooking.getId());
        assertThat(found.getId()).isEqualTo(waitingBooking.getId());
    }

    @Test
    void validateBookingExistsWhenNotFound() {
        long missingId = waitingBooking.getId() + approvedBooking.getId() + 100;
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateBookingExists(missingId),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).contains("Бронирование не найдено");
    }

    @Test
    void validateOwnerForOwner() {
        assertThatNoException().isThrownBy(() ->
                validator.validateOwner(waitingBooking, owner.getId())
        );
    }

    @Test
    void validateOwnerForNonOwner() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateOwner(waitingBooking, booker.getId()),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Только владелец вещи может менять статус");
    }

    @Test
    void validateAccessForBookerOrOwner() {
        assertThatNoException().isThrownBy(() ->
                validator.validateAccess(waitingBooking, booker.getId())
        );

        assertThatNoException().isThrownBy(() ->
                validator.validateAccess(waitingBooking, owner.getId())
        );
    }

    @Test
    void validateAccessForOthers() {
        final Booking waitingBooking = new Booking();
        waitingBooking.setItem(item);
        waitingBooking.setBooker(booker);
        waitingBooking.setStart(LocalDateTime.now().minusDays(2));
        waitingBooking.setEnd(LocalDateTime.now().minusDays(1));
        waitingBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(waitingBooking);

        final User other = new User();
        other.setName("Other");
        other.setEmail("other@example.com");
        userRepository.save(other);

        ResponseStatusException ex = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> validator.validateAccess(waitingBooking, other.getId())
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getReason()).contains("Доступ запрещён");
    }

    @Test
    void validateStatusIsWaitingForWaiting() {
        assertThatNoException().isThrownBy(() ->
                validator.validateStatusIsWaiting(waitingBooking)
        );
    }

    @Test
    void validateStatusIsWaitingForNonWaiting() {
        ResponseStatusException ex = catchThrowableOfType(
                () -> validator.validateStatusIsWaiting(approvedBooking),
                ResponseStatusException.class
        );
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getReason()).contains("Нельзя изменить статус бронирования повторно");
    }
}
