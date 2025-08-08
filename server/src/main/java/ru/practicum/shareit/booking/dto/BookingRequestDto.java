package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDto {

    @NotNull(message = "Идентификатор вещи обязателен")
    Long itemId;

    @NotNull(message = "Дата начала бронирования обязательна")
    LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования обязательна")
    LocalDateTime end;
}
