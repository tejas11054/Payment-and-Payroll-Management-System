package com.paymentapp.exception;

public class DuplicateOrgNameException extends RuntimeException {
    public DuplicateOrgNameException(String message) {
        super(message);
    }
}