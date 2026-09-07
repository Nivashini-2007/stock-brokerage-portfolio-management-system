package com.stockbroker.backend.exception;

public class UnauthorisedAccessException extends RuntimeException {

    public UnauthorisedAccessException(String message) {
        super(message);
    }
}
