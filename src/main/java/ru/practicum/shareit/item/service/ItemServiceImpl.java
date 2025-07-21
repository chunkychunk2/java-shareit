package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.ItemValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.UserValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserValidator userValidator;
    private final ItemValidator itemValidator;

    @Override
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userValidator.validateUserExists(userId);
        Item item = ItemMapper.toModel(dto);
        item.setOwner(owner);
        return ItemMapper.toDto(repository.save(item));
    }

    @Override
    public ItemDto update(Long itemId, ItemDto dto, Long userId) {
        Item existing = itemValidator.validateItemExists(itemId);
        itemValidator.validateOwnership(existing, userId);

        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existing.setAvailable(dto.getAvailable());
        }

        Item updated = repository.update(existing);
        return ItemMapper.toDto(updated);
    }

    @Override
    public Optional<ItemDto> getById(Long itemId, Long userId) {
        return repository.findById(itemId).map(ItemMapper::toDto);
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId) {
        return repository.findAll().stream().filter(item -> item.getOwner() != null && item.getOwner().getId().equals(userId)).map(ItemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String s = text.toLowerCase();
        return repository.findAll().stream().filter(Item::getAvailable).filter(item -> item.getName().toLowerCase().contains(s) || item.getDescription().toLowerCase().contains(s)).map(ItemMapper::toDto).collect(Collectors.toList());
    }
}