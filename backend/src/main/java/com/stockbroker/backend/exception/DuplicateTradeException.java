package com.stockbroker.backend.exception;

public class DuplicateTradeException extends RuntimeException {

    public DuplicateTradeException(String message) {
        super(message);
    }
}
