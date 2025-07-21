package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingForItemDto;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotNull(message = "Название не может быть пустым")
    @Size(min = 1, max = 200)
    private String name;

    @NotNull(message = "Описание не может быть пустым")
    @Size(min = 1)
    private String description;

    @NotNull(message = "Поле available обязательно")
    private Boolean available;

    private Long requestId;

    private BookingForItemDto lastBooking;
    private BookingForItemDto nextBooking;
    private List<CommentDto> comments;
}
