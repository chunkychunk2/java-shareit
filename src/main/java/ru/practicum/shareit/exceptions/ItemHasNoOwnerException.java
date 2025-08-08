package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ItemHasNoOwnerException extends ShareItException {

    public ItemHasNoOwnerException(String message) {
        super(message);
    }
}