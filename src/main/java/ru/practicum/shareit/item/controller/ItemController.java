package ru.practicum.shareit.item.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping(path = "/items")
@Validated
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Valid @RequestBody ItemDto dto, @RequestHeader(USER_ID_HEADER) Long userId) {
        return service.create(dto, userId);
    }

    @PutMapping("/{itemId}")
    public ItemDto update(@Valid @PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader(USER_ID_HEADER) Long userId) {
        return service.update(itemId, dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchUpdate(@PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader(USER_ID_HEADER) Long userId) {
        return service.update(itemId, dto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable Long itemId, @RequestHeader(USER_ID_HEADER) Long userId) {
        return service.getById(itemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь с id " + itemId + " не найдена"));
    }


    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader(USER_ID_HEADER) Long userId) {
        return service.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text, @RequestHeader(USER_ID_HEADER) Long userId) {
        return service.search(text);
    }
}