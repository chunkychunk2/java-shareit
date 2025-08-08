package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingForItemDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.validation.ItemValidator;
import ru.practicum.shareit.request.validation.ItemRequestValidator;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.validation.UserValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final BookingRepository bookingRepository;
    private final UserValidator userValidator;
    private final ItemValidator itemValidator;
    private final CommentRepository commentRepository;
    private final ItemRequestValidator requestValidator;

    @Override
    public ItemDto create(ItemDto dto, Long userId) {
        User owner = userValidator.validateUserExists(userId);
        Item item = ItemMapper.toModel(dto);
        item.setOwner(owner);
        if (dto.getRequestId() != null) {
            requestValidator.validateRequestExists(dto.getRequestId());
            item.setRequestId(dto.getRequestId());
        }
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

        Item updated = repository.save(existing);
        return ItemMapper.toDto(updated);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        userValidator.validateUserExists(userId);
        Item item = itemValidator.validateItemExists(itemId);

        List<CommentDto> comments = commentRepository
                .findByItemIdOrderByCreatedDesc(item.getId())
                .stream().map(c -> new CommentDto(
                        c.getId(),
                        c.getText(),
                        c.getAuthor().getName(),
                        c.getCreated()))
                .collect(Collectors.toList());

        BookingForItemDto last = null;
        BookingForItemDto next = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            last = bookingRepository
                    .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(itemId, BookingStatus.APPROVED, now)
                    .map(b -> new BookingForItemDto(
                            b.getId(),
                            b.getBooker().getId()))
                    .orElse(null);

            next = bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(itemId, BookingStatus.APPROVED, now)
                    .map(b -> new BookingForItemDto(
                            b.getId(),
                            b.getBooker().getId()))
                    .orElse(null);
        }
        return ItemMapper.toDto(item, last, next, comments);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String s = text.toLowerCase();
        return repository.findAll().stream()
                .filter(Item::getAvailable)
                .filter(item -> {
                    if (item.getName() != null && item.getName().toLowerCase().contains(s)) {
                        return true;
                    }
                    String desc = item.getDescription();
                    return desc != null && desc.toLowerCase().contains(s);
                })
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getAllByUser(Long userId) {
        userValidator.validateUserExists(userId);
        List<Item> items = repository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    BookingForItemDto last = bookingRepository
                            .findFirstByItemIdAndStatusAndStartBeforeOrderByStartDesc(item.getId(), BookingStatus.APPROVED, now)
                            .map(b -> new BookingForItemDto(
                                    b.getId(),
                                    b.getBooker().getId()))
                            .orElse(null);

                    BookingForItemDto next = bookingRepository
                            .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                            .map(b -> new BookingForItemDto(
                                    b.getId(),
                                    b.getBooker().getId()))
                            .orElse(null);

                    List<CommentDto> comments = commentRepository
                            .findByItemIdOrderByCreatedDesc(item.getId())
                            .stream().map(c -> new CommentDto(
                                    c.getId(),
                                    c.getText(),
                                    c.getAuthor().getName(),
                                    c.getCreated()))
                            .collect(Collectors.toList());

                    return new ItemDto(
                            item.getId(),
                            item.getName(),
                            item.getDescription(),
                            item.getAvailable(),
                            item.getRequestId(),
                            last,
                            next,
                            comments
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto dto) {
        User user = userValidator.validateUserExists(userId);
        Item item = itemValidator.validateItemExists(itemId);

        LocalDateTime now = LocalDateTime.now();
        boolean hasBooked = bookingRepository
                .findByBookerIdAndItemIdAndEndBefore(userId, itemId, now)
                .stream().findAny().isPresent();
        if (!hasBooked) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь не брал эту вещь в аренду или аренда ещё не закончилась");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(now);
        Comment saved = commentRepository.save(comment);

        return new CommentDto(
                saved.getId(),
                saved.getText(),
                saved.getAuthor().getName(),
                saved.getCreated()
        );
    }
}
