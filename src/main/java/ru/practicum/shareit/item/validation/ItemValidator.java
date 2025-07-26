package ru.practicum.shareit.item.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.NotItemOwnerException;
import ru.practicum.shareit.exceptions.ItemHasNoOwnerException;

@Component
@RequiredArgsConstructor
public class ItemValidator {
    private final ItemRepository itemRepository;

    public Item validateItemExists(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Item not found: " + itemId));
    }

    public void validateOwnership(Item item, Long userId) {
        if (item.getOwner() == null || item.getOwner().getId() == null) {
            throw new ItemHasNoOwnerException("Item has no owner");
        }
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotItemOwnerException("Only the owner can edit item: " + item.getId());
        }
    }
}
