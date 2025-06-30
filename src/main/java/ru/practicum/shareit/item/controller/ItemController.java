package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/items")
@Validated
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto create(@Valid @RequestBody ItemDto dto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.create(dto, userId);
    }

    @PutMapping("/{itemId}")
    public ItemDto update(@Valid @PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.update(itemId, dto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchUpdate(@PathVariable Long itemId, @RequestBody ItemDto dto, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.update(itemId, dto, userId);
    }

    @GetMapping("/{itemId}")
    public Optional<ItemDto> getById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getAllByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.getAllByUser(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return service.search(text);
    }
}