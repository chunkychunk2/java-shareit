package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @NotNull(message = "Дата начала бронирования обязательна")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    private LocalDateTime end;

    @NotNull(message = "Статус бронирования обязателен")
    private BookingStatus status;

    @NotNull
    private BookerInfo booker;

    @NotNull
    private ItemInfo item;

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