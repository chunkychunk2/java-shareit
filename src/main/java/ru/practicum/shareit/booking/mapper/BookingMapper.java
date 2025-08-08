package ru.practicum.shareit.booking.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    // Booking → BookingDto
    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto.BookerInfo bk = new BookingDto.BookerInfo(
                booking.getBooker().getId()
        );
        BookingDto.ItemInfo it = new BookingDto.ItemInfo(
                booking.getItem().getId(),
                booking.getItem().getName()
        );

        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getStatus(),
                bk,
                it
        );
    }

    // BookingDto → Booking
    public static Booking toModel(User booker, Item item, BookingRequestDto dto) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }
}
