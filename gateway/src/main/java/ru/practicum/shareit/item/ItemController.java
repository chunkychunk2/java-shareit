package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    public static final String X_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestBody @Valid ItemCreateDto dto) {
        log.info("Создание вещи {} пользователем {}", dto, userId);
        return itemClient.createItem(userId, dto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId,
                                             @RequestBody @Valid CommentRequestDto dto) {
        log.info("Добавление отзыва {} пользователем {} для вещи {}", dto, userId, itemId);
        return itemClient.addComment(userId, itemId, dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId,
            @RequestBody @Valid ItemCreateDto dto) {
        log.info("Обновление вещи по id={} пользователем {}, {}", itemId, userId, dto);
        return itemClient.updateItem(userId, itemId, dto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@RequestHeader(X_SHARER_USER_ID) long userId, @PathVariable long itemId) {
        log.info("Получение вещи по id={} для пользователя {}", itemId, userId);
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(X_SHARER_USER_ID) long userId) {
        log.info("Получение всех вещей {}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestHeader(X_SHARER_USER_ID) long userId, @RequestParam String text) {
        log.info("Поиск вещи '{}' для пользователя {}", text, userId);
        return itemClient.searchItems(userId, text);
    }
}
