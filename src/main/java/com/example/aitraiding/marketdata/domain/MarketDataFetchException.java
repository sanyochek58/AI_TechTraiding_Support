package com.example.aitraiding.marketdata.domain;

public class MarketDataFetchException extends RuntimeException {
    public MarketDataFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
