package com.example.aitraiding.strategy.application;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.strategy.domain.Signal;
import com.example.aitraiding.strategy.domain.SignalStrategy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class SignalGenerationService {

    private final SignalStrategy strategy;
    private final int period;
    private final Deque<Candle> buffer = new ArrayDeque<>();
    private final int maxBufferSize;

    public SignalGenerationService(SignalStrategy strategy, int period, int maxBufferSize) {
        this.strategy = strategy;
        this.period = period;
        this.maxBufferSize = maxBufferSize;
    }

    public void initializeWithHistory(List<Candle> historicalCandles){
        for (Candle candle : historicalCandles) {
            buffer.addLast(candle);
            if(buffer.size() > maxBufferSize){
                buffer.removeFirst();
            }
        }
    }

    public void onNewCandle(Candle candle) {
        buffer.addLast(candle);

        if (buffer.size() > maxBufferSize) {
            buffer.removeFirst();
        }

        if (buffer.size() >= period) {
            List<Candle> candleList = new ArrayList<>(buffer);
            Signal signal = strategy.generateSignal(candleList);
            System.out.println("Новый сигнал: " + signal + " | по свече: " + candle);
        }
    }
}