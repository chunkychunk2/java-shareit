package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;

    @NotNull(message = "Название не может быть пустым")
    @Size(min = 1, max = 200)
    String name;

    @NotNull(message = "Описание не может быть пустым")
    @Size(min = 1)
    String description;

    @NotNull(message = "Поле available обязательно")
    Boolean available;

    Long requestId;

    BookingForItemDto lastBooking;
    BookingForItemDto nextBooking;
    List<CommentDto> comments;
}
