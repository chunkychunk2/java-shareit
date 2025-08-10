package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.ShareItServer;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ShareItServer.class)
@AutoConfigureMockMvc
@Transactional
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private BookingRepository bookingRepo;

    private User owner;
    private User booker;
    private Item item;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setup() {
        bookingRepo.deleteAll();
        itemRepo.deleteAll();
        userRepo.deleteAll();

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@mail.com");
        owner = userRepo.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@mail.com");
        booker = userRepo.save(booker);

        item = new Item();
        item.setName("Book");
        item.setDescription("For IT");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepo.save(item);

        start = LocalDateTime.now().plusDays(1);
        end   = start.plusDays(2);
    }

    @Test
    void createBooking() throws Exception {
        BookingRequestDto req = new BookingRequestDto(item.getId(), start, end);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .content(mapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is(booker.getId().intValue())))
                .andExpect(jsonPath("$.item.id", is(item.getId().intValue())));
    }

    @Test
    void getBooking() throws Exception {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(BookingStatus.WAITING);
        b = bookingRepo.save(b);

        // booker
        mvc.perform(get("/bookings/{id}", b.getId())
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(b.getId().intValue())));

        // owner
        mvc.perform(get("/bookings/{id}", b.getId())
                        .header("X-Sharer-User-Id", owner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(b.getId().intValue())));
    }

    @Test
    void approveBooking() throws Exception {
        Booking b = new Booking();
        b.setItem(item);
        b.setBooker(booker);
        b.setStart(start);
        b.setEnd(end);
        b.setStatus(BookingStatus.WAITING);
        b = bookingRepo.save(b);

        mvc.perform(patch("/bookings/{id}", b.getId())
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveFirstWaiting() throws Exception {
        Booking w1 = new Booking();
        w1.setItem(item);
        w1.setBooker(booker);
        w1.setStart(start);
        w1.setEnd(end);
        w1.setStatus(BookingStatus.WAITING);
        w1 = bookingRepo.save(w1);

        Booking w2 = new Booking();
        w2.setItem(item);
        w2.setBooker(booker);
        w2.setStart(start.plusDays(1));
        w2.setEnd(end.plusDays(1));
        w2.setStatus(BookingStatus.WAITING);
        bookingRepo.save(w2);

        mvc.perform(patch("/bookings")
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(w1.getId().intValue())))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveFirstWaitingNotFound() throws Exception {
        mvc.perform(patch("/bookings")
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByBooker() throws Exception {
        Booking b1 = new Booking();
        b1.setItem(item);
        b1.setBooker(booker);
        b1.setStart(start);
        b1.setEnd(end);
        b1.setStatus(BookingStatus.WAITING);
        bookingRepo.save(b1);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", booker.getId())
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void findByOwner() throws Exception {
        Booking b1 = new Booking();
        b1.setItem(item);
        b1.setBooker(booker);
        b1.setStart(start);
        b1.setEnd(end);
        b1.setStatus(BookingStatus.WAITING);
        bookingRepo.save(b1);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", owner.getId())
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
