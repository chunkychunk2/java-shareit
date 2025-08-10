package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.validation.BookingValidator;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.validation.ItemValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.UserValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserValidator userValidator;
    private final ItemValidator itemValidator;
    private final BookingValidator bookingValidator;

    @Override
    public BookingDto create(Long userId, BookingRequestDto dto) {
        User user = userValidator.validateUserExists(userId);
        Item item = itemValidator.validateItemExists(dto.getItemId());

        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Нельзя бронировать свою вещь");
        }
        Booking saved = bookingRepository.save(BookingMapper.toModel(user, item, dto));
        return BookingMapper.toDto(saved);
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingValidator.validateBookingExists(bookingId);
        bookingValidator.validateOwner(booking, ownerId);
        bookingValidator.validateStatusIsWaiting(booking);
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingValidator.validateBookingExists(bookingId);
        bookingValidator.validateAccess(booking, userId);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> findByBooker(Long userId, BookingState state, Pageable pageable) {
        userValidator.validateUserExists(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> list = switch (state) {
            case CURRENT  -> bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST     -> bookingRepository.findByBookerIdAndEndIsBefore(userId, now, pageable);
            case FUTURE   -> bookingRepository.findByBookerIdAndStartIsAfter(userId, now, pageable);
            case WAITING  -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            case ALL      -> bookingRepository.findByBookerId(userId, pageable);
        };
        return list.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findByOwner(Long userId, BookingState state, Pageable pageable) {
        userValidator.validateUserExists(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> list = switch (state) {
            case CURRENT  -> bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now, pageable);
            case PAST     -> bookingRepository.findByItemOwnerIdAndEndIsBefore(userId, now, pageable);
            case FUTURE   -> bookingRepository.findByItemOwnerIdAndStartIsAfter(userId, now, pageable);
            case WAITING  -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, pageable);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, pageable);
            case ALL      -> bookingRepository.findByItemOwnerId(userId, pageable);
        };
        return list.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }
}

