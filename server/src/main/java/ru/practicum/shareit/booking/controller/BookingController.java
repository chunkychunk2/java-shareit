package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestHeader(X_SHARER_USER_ID) Long userId, @RequestBody BookingRequestDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long bookingId,
                                     @RequestParam boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @PatchMapping
    public BookingDto approveFirstWaiting(@RequestHeader(X_SHARER_USER_ID) Long ownerId, @RequestParam boolean approved) {
        List<Booking> waiting = bookingRepository
                .findByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING, Pageable.unpaged());
        if (waiting.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Нет бронирований в статусе waiting");
        }
        Long bookingId = waiting.get(0).getId();
        return bookingService.approve(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsForBooker(@RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @PageableDefault(sort = "start", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.findByBooker(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsForOwner(@RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @PageableDefault(sort = "start", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.findByOwner(userId, state, pageable);
    }
}
