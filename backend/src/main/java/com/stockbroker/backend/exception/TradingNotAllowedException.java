package com.stockbroker.backend.exception;

/**
 * Thrown when a client attempts to trade while KYC is not verified,
 * the trading account is not ACTIVE, or the stock is in a circuit halt.
 */
public class TradingNotAllowedException extends RuntimeException {

    public TradingNotAllowedException(String message) {
        super(message);
    }
}
