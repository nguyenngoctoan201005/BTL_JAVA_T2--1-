package com.coffeeshop.backend.exception;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentExceptionHanlder extends RuntimeException {
    public PaymentExceptionHanlder(String message) {
        super(message);
    }
    
}
