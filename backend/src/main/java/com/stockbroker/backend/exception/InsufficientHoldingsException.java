package com.stockbroker.backend.exception;

public class InsufficientHoldingsException extends RuntimeException {

    public InsufficientHoldingsException(String message) {
        super(message);
    }
}
