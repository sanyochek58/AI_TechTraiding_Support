package com.example.aitraiding.marketdata.domain;

@FunctionalInterface
public interface CandleListener {
    void onCandleClosed(Candle candle);
}
