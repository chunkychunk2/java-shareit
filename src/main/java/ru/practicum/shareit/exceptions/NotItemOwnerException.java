package ru.practicum.shareit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotItemOwnerException extends ShareItException {

    public NotItemOwnerException(String message) {
        super(message);
    }
}