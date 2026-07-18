package com.example.aitraiding.strategy.domain;

import com.example.aitraiding.marketdata.domain.Candle;

import java.util.List;

public interface SignalStrategy {
    Signal generateSignal(List<Candle> candles);
}
