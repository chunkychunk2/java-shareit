package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.validation.ItemRequestValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.UserValidator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserValidator userValidator;
    private final ItemRequestValidator requestValidator;

    @Override
    public ItemRequestDto create(Long userId, ItemRequestCreateDto dto) {
        User owner = userValidator.validateUserExists(userId);
        ItemRequest request = ItemRequestMapper.toModel(dto);
        request.setRequester(owner);
        ItemRequest saved = requestRepository.save(request);
        return ItemRequestMapper.toDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> findByUser(Long userId) {
        userValidator.validateUserExists(userId);
        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream()
                .map(request -> {
                    List<Item> requestItems = itemsByRequest.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toDto(request, requestItems);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> findAll(Long userId) {
        userValidator.validateUserExists(userId);
        List<ItemRequest> requests = requestRepository.findByRequesterIdNotOrderByCreatedDesc(userId);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsByRequest = items.stream()
                .collect(Collectors.groupingBy(Item::getRequestId));

        return requests.stream()
                .map(request -> {
                    List<Item> requestItems = itemsByRequest.getOrDefault(request.getId(), Collections.emptyList());
                    return ItemRequestMapper.toDto(request, requestItems);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto findById(Long userId, Long requestId) {
        userValidator.validateUserExists(userId);
        ItemRequest r = requestValidator.validateRequestExists(requestId);
        List<Item> items = itemRepository.findByRequestId(requestId);
        return ItemRequestMapper.toDto(r, items);
    }
}
