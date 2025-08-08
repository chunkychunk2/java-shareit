package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
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
    BookingState status;

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
