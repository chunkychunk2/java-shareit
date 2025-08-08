package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.EntityNotFoundException;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto createBooking(@RequestHeader(USER_ID_HEADER) Long userId, @Valid @RequestBody BookingRequestDto dto) {
        return bookingService.create(userId, dto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long bookingId,
                                     @RequestParam boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @PatchMapping
    public BookingDto approveFirstWaiting(@RequestHeader(USER_ID_HEADER) Long ownerId, @RequestParam boolean approved) {
        return bookingRepository.findFirstByItemOwnerIdAndStatus(ownerId, BookingStatus.WAITING)
                .map(booking -> bookingService.approve(ownerId, booking.getId(), approved))
                .orElseThrow(() -> new EntityNotFoundException("Нет бронирований в статусе waiting"));
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookingsForBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "ALL") BookingState state,
                                                 @PageableDefault(sort = "start", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.findByBooker(userId, state, pageable);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsForOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestParam(defaultValue = "ALL") BookingState state,
                                                @PageableDefault(sort = "start", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.findByOwner(userId, state, pageable);
    }
}
