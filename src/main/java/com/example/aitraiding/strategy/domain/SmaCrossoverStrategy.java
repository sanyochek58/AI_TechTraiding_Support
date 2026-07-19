package com.example.aitraiding.strategy.domain;

import com.example.aitraiding.marketdata.domain.Candle;

import java.math.BigDecimal;
import java.util.List;

public class SmaCrossoverStrategy implements SignalStrategy {

    private final SmaCalculator calculator;
    private final int period;

    public SmaCrossoverStrategy(SmaCalculator calculator, int period) {
        this.calculator = calculator;
        this.period = period;
    }

    @Override
    public Signal generateSignal(List<Candle> candles){
        BigDecimal sma = calculator.calculateSma(candles, period);
        Candle lastCandle = candles.get(candles.size()-1);

        int compareTo = lastCandle.close().compareTo(sma);

        if(compareTo == 0){
            return Signal.HOLD;
        }
        else if(compareTo < 0){
            return Signal.SHORT;
        }
        else {
            return Signal.LONG;
        }
    }
}
