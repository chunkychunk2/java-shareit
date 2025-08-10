package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    Long id;

    @NotNull(message = "Дата начала бронирования обязательна")
    LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    LocalDateTime end;

    @NotNull(message = "Статус бронирования обязателен")
    BookingStatus status;

    @NotNull
    BookerInfo booker;

    @NotNull
    ItemInfo item;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookerInfo {
        private Long id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemInfo {
        private Long id;
        private String name;
    }
}