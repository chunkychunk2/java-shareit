package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.validation.BookingValidator;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingValidator validator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking waitingBooking;
    private LocalDateTime now;

    @BeforeEach
    void setup() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

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

        now = LocalDateTime.now();
    }

    @Test
    void createShouldSaveAndReturnBooking() {
        BookingRequestDto req = new BookingRequestDto(item.getId(), now.plusDays(1), now.plusDays(2));
        BookingDto dto = bookingService.create(booker.getId(), req);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getItem().getId()).isEqualTo(item.getId());
        assertThat(dto.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);

        // вещь недоступна
        item.setAvailable(false);
        itemRepository.save(item);
        assertThrows(ResponseStatusException.class,
                () -> bookingService.create(booker.getId(), req));

        // владелец пытается забронировать свою же вещь
        item.setAvailable(true);
        itemRepository.save(item);
        assertThrows(ResponseStatusException.class,
                () -> bookingService.create(owner.getId(), req));
    }

    @Test
    void findByBookerAll() {
        saveBooking(BookingStatus.APPROVED, now.minusDays(4), now.minusDays(2));
        saveBooking(BookingStatus.APPROVED, now.plusDays(2), now.plusDays(4));
        saveBooking(BookingStatus.WAITING, now.plusDays(1), now.plusDays(2));
        saveBooking(BookingStatus.REJECTED, now.plusDays(5), now.plusDays(6));

        List<BookingDto> all = bookingService.findByBooker(booker.getId(), BookingState.ALL, PageRequest.of(0, 10));
        assertThat(all).hasSize(4);
    }

    @Test
    void findByBookerPast() {
        Booking past = saveBooking(BookingStatus.APPROVED, now.minusDays(4), now.minusDays(2));
        List<BookingDto> list = bookingService.findByBooker(booker.getId(), BookingState.PAST, PageRequest.of(0, 10));
        assertThat(list)
                .extracting(BookingDto::getId)
                .containsExactly(past.getId());
    }

    @Test
    void findByBookerFuture() {
        Booking future = saveBooking(BookingStatus.APPROVED, now.plusDays(2), now.plusDays(4));
        List<BookingDto> list = bookingService.findByBooker(booker.getId(), BookingState.FUTURE, PageRequest.of(0, 10));
        assertThat(list)
                .extracting(BookingDto::getId)
                .containsExactly(future.getId());
    }

    @Test
    void findByBookerWaiting() {
        Booking waiting = saveBooking(BookingStatus.WAITING, now.plusDays(1), now.plusDays(2));
        List<BookingDto> list = bookingService.findByBooker(booker.getId(), BookingState.WAITING, PageRequest.of(0, 10));
        assertThat(list)
                .extracting(BookingDto::getId)
                .containsExactly(waiting.getId());
    }

    @Test
    void findByBookerRejected() {
        Booking rejected = saveBooking(BookingStatus.REJECTED, now.plusDays(5), now.plusDays(6));
        List<BookingDto> list = bookingService.findByBooker(booker.getId(), BookingState.REJECTED, PageRequest.of(0, 10));
        assertThat(list)
                .extracting(BookingDto::getId)
                .containsExactly(rejected.getId());
    }

    private Booking saveBooking(BookingStatus status, LocalDateTime start, LocalDateTime end) {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(status);
        return bookingRepository.save(b);
    }

    @Test
    void approveShouldChangeStatusOrReject() {
        // бронирование в статусе WAITING
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(now.plusDays(1));
        b.setEnd(now.plusDays(2));
        b.setStatus(BookingStatus.WAITING);
        b = bookingRepository.save(b);

        // подтверждение бронирования
        BookingDto approved = bookingService.approve(owner.getId(), b.getId(), true);
        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);

        // отклонение нового бронирования
        Booking b2 = new Booking();
        b2.setItem(item);
        b2.setBooker(booker);
        b2.setStart(now.plusDays(3));
        b2.setEnd(now.plusDays(4));
        b2.setStatus(BookingStatus.WAITING);
        b2 = bookingRepository.save(b2);
        BookingDto rejected = bookingService.approve(owner.getId(), b2.getId(), false);
        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);

        // только владелец может подтверждать или отклонять
        Booking bk = new Booking();
        bk.setItem(item);
        bk.setBooker(booker);
        bk.setStart(now.plusDays(5));
        bk.setEnd(now.plusDays(6));
        bk.setStatus(BookingStatus.WAITING);
        Booking b3 = bookingRepository.save(bk);
        final Long b3Id = b3.getId();

        assertThrows(ResponseStatusException.class,
                () -> bookingService.approve(booker.getId(), b3Id, true)
        );
    }
}
