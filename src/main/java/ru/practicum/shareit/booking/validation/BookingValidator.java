package ru.practicum.shareit.booking.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingOwnItemException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemNotAvailableException;

@Component
@RequiredArgsConstructor
public class BookingValidator {
    private final BookingRepository bookingRepository;

    public Booking validateBookingExists(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Бронирование не найдено: " + bookingId));
    }

    public void validateOwner(Booking booking, Long ownerId) {
        Long actualOwner = booking.getItem().getOwner().getId();
        if (!actualOwner.equals(ownerId)) {
            throw new BookingOwnItemException("Только владелец вещи может менять статус бронирования");
        }
    }

    public void validateAccess(Booking booking, Long userId) {
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (!bookerId.equals(userId) && !ownerId.equals(userId)) {
            throw new BookingOwnItemException("Доступ запрещён");
        }
    }

    public void validateStatusIsWaiting(Booking booking) {
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ItemNotAvailableException("Нельзя изменить статус бронирования повторно");
        }
    }
}
