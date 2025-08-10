package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    // ItemRequest → ItemRequestDto
    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        List<ItemResponseDto> responses = items.stream()
                .map(i -> new ItemResponseDto(
                        i.getId(),
                        i.getName(),
                        i.getOwner().getId()
                ))
                .collect(Collectors.toList());

        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated(),
                responses
        );
    }

    // ItemRequestDto → ItemRequest
    public static ItemRequest toModel(ItemRequestCreateDto dto) {
        if (dto == null) {
            return null;
        }
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        return request;
    }
}
