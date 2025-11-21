package com.paymentapp.exception;

public class UserApiException extends RuntimeException {
    public UserApiException(String message) {
        super(message);
    }
}
