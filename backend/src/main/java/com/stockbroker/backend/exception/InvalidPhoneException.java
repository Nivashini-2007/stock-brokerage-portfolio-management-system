package com.stockbroker.backend.exception;

public class InvalidPhoneException extends RuntimeException {

    public InvalidPhoneException(String message) {
        super(message);
    }
}
