package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto dto, Long userId);

    ItemDto update(Long itemId, ItemDto dto, Long userId);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getAllByUser(Long userId);

    List<ItemDto> search(String text);

    CommentDto addComment(Long userId, Long itemId, CommentRequestDto dto);
}
