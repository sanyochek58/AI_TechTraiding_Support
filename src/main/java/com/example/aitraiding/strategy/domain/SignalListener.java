package com.example.aitraiding.strategy.domain;

import com.example.aitraiding.marketdata.domain.Candle;

@FunctionalInterface
public interface SignalListener {
    void onSignalGenerated(Signal signal, Candle candle);
}
