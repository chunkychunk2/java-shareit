package ru.practicum.shareit.request.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

@Component
@RequiredArgsConstructor
public class ItemRequestValidator {

    private final ItemRequestRepository requestRepository;

    public ItemRequest validateRequestExists(Long requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден: " + requestId));
    }
}
