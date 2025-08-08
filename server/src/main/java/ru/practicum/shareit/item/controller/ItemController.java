package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@RequestBody ItemDto dto, @RequestHeader(X_SHARER_USER_ID) Long userId) {
        return service.create(dto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId, @RequestHeader(X_SHARER_USER_ID) Long userId,
            @RequestBody CommentRequestDto dto) {
        return service.addComment(userId, itemId, dto);
    }

    @PutMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader(X_SHARER_USER_ID) Long userId) {
        return service.update(itemId, dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchUpdate(@PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader(X_SHARER_USER_ID) Long userId) {
        return service.update(itemId, dto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@RequestHeader(X_SHARER_USER_ID) Long userId, @PathVariable Long itemId) {
        return service.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader(X_SHARER_USER_ID) Long userId) {
        return service.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text, @RequestHeader(X_SHARER_USER_ID) Long userId) {
        return service.search(text);
    }
}
