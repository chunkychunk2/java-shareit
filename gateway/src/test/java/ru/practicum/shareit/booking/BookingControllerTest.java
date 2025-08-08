package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(BookingController.class)
    static class TestConfig {
    }

    @Test
    void getBookings() throws Exception {
        long userId = 1L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        BookingDto dto = new BookingDto(
                10L,
                start,
                end,
                BookingState.WAITING,
                new BookingDto.BookerInfo(2L),
                new BookingDto.ItemInfo(5L, "Book"));

        when(bookingClient.getBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of(dto)));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].status", is("WAITING")))
                .andExpect(jsonPath("$[0].booker.id", is(2)))
                .andExpect(jsonPath("$[0].item.id", is(5)))
                .andExpect(jsonPath("$[0].item.name", is("Book")));

        verify(bookingClient).getBookings(userId, BookingState.ALL, 0, 10);
    }

    @Test
    void bookItem() throws Exception {
        long userId = 1L;
        BookItemRequestDto req = new BookItemRequestDto(5L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        BookingDto response = new BookingDto(
                20L,
                req.getStart(),
                req.getEnd(),
                BookingState.WAITING,
                new BookingDto.BookerInfo(userId),
                new BookingDto.ItemInfo(req.getItemId(), "Book"));

        when(bookingClient.bookItem(eq(userId), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.status(201).body(response));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .content(mapper.writeValueAsString(req))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(20)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.booker.id", is((int)userId)))
                .andExpect(jsonPath("$.item.name", is("Book")));

        verify(bookingClient).bookItem(eq(userId), any(BookItemRequestDto.class));
    }

    @Test
    void getBooking() throws Exception {
        long userId = 1L;
        long bookingId = 30L;
        BookingDto response = new BookingDto(
                bookingId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                BookingState.WAITING,
                new BookingDto.BookerInfo(userId),
                new BookingDto.ItemInfo(7L, "Book")
        );

        when(bookingClient.getBooking(eq(userId), eq(bookingId)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int)bookingId)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.id", is(7)))
                .andExpect(jsonPath("$.booker.id", is((int)userId)));

        verify(bookingClient).getBooking(userId, bookingId);
    }

    @Test
    void approveBooking_ReturnsOk() throws Exception {
        long userId = 1L;
        long bookingId = 40L;
        boolean approved = true;
        BookingDto response = new BookingDto(
                bookingId,
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(2),
                BookingState.WAITING,
                new BookingDto.BookerInfo(3L),
                new BookingDto.ItemInfo(8L, "Book")
        );

        when(bookingClient.approve(eq(bookingId), eq(approved), eq(userId)))
                .thenReturn(ResponseEntity.ok(response));

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", String.valueOf(approved))
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.name", is("Book")));

        verify(bookingClient).approve(bookingId, approved, userId);
    }

    @Test
    void getOwnerBookings() throws Exception {
        long userId = 2L;
        BookingDto dto = new BookingDto(
                50L,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                BookingState.WAITING,
                new BookingDto.BookerInfo(5L),
                new BookingDto.ItemInfo(9L, "Book")
        );

        when(bookingClient.getOwnerBookings(eq(userId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(ResponseEntity.ok(List.of(dto)));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$[0].booker.id", is(5)))
                .andExpect(jsonPath("$[0].item.name", is("Book")));

        verify(bookingClient).getOwnerBookings(userId, BookingState.ALL, 0, 10);
    }
}
