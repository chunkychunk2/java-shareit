package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    ItemDto create(ItemDto dto, Long userId);

    ItemDto update(Long itemId, ItemDto dto, Long userId);

    Optional<ItemDto> getById(Long itemId, Long userId);

    List<ItemDto> getAllByUser(Long userId);

    List<ItemDto> search(String text);
}