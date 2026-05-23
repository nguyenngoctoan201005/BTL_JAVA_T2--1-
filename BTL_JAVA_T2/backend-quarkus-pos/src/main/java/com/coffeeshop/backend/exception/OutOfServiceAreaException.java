package com.coffeeshop.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class OutOfServiceAreaException extends RuntimeException {
    public OutOfServiceAreaException(String message) {
        super(message);
    }
}
