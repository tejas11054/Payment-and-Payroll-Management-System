package com.paymentapp.exception;

public class DuplicateBankAccountException extends RuntimeException {
    public DuplicateBankAccountException(String message) {
        super(message);
    }
}
