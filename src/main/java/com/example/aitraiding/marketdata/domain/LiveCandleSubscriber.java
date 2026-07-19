package com.example.aitraiding.marketdata.domain;

public interface LiveCandleSubscriber {
    void subscribe(String symbol, String interval, CandleListener listener);

}
