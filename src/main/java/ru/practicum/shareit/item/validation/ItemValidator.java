package ru.practicum.shareit.item.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ItemNotAvailableException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    private final ItemRepository itemRepository;

    public Item validateItemExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("Вещь не найдена: " + itemId));
    }

    public void validateOwnership(Item item, Long userId) {
        if (item.getOwner() == null || item.getOwner().getId() == null) {
            throw new ItemNotAvailableException("У вещи отсутствует владелец");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new ItemNotAvailableException("Только владелец может редактировать: " + item.getId());
        }
    }
}