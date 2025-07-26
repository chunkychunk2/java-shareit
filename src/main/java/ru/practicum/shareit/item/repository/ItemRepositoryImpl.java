package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private final AtomicLong idGen = new AtomicLong(0);
    private final Map<Long, Item> storage = new HashMap<>();

    @Override
    public Item save(Item item) {
        long id = idGen.incrementAndGet();
        item.setId(id);
        storage.put(id, item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Item update(Item item) {
        storage.put(item.getId(), item);
        return item;
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}