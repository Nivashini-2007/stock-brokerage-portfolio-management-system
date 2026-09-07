package com.stockbroker.backend.exception;

public class InsufficientMarginException extends RuntimeException {

    public InsufficientMarginException(String message) {
        super(message);
    }
}
