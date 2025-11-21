package com.paymentapp.exception;

public class CustomConcurrentUpdateException extends RuntimeException {
    public CustomConcurrentUpdateException(String message) {
        super(message);
    }
}
