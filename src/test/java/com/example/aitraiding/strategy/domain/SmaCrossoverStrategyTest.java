package com.example.aitraiding.strategy.domain;

import com.example.aitraiding.marketdata.domain.Candle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmaCrossoverStrategyTest {

    private final SmaCalculator calculator = new SmaCalculator();
    private final int period = 3;

    private Candle createCandle(String close) {
        return new Candle(
                "BTCUSDT",
                Instant.now(),
                new BigDecimal(close),
                new BigDecimal(close),
                new BigDecimal(close),
                new BigDecimal(close),
                BigDecimal.ZERO
        );
    }

    private List<Candle> initCandles(String... closePrices) {
        List<Candle> candles = new ArrayList<>();
        for (String close : closePrices) {
            candles.add(createCandle(close));
        }
        return candles;
    }

    @Test
    @DisplayName("Цена выше SMA -> сигнал LONG")
    public void shouldReturnLongWhenPriceAboveSma() {
        SmaCrossoverStrategy strategy = new SmaCrossoverStrategy(calculator, period);
        List<Candle> candles = initCandles("7", "10", "11");

        Signal signal = strategy.generateSignal(candles);

        assertEquals(Signal.LONG, signal);
    }

    @Test
    @DisplayName("Цена ниже SMA -> сигнал SHORT")
    public void shouldReturnShortWhenPriceUnderSma(){
        SmaCrossoverStrategy strategy = new SmaCrossoverStrategy(calculator, period);
        List<Candle> candles = initCandles("3", "2", "1");

        Signal signal = strategy.generateSignal(candles);

        assertEquals(Signal.SHORT, signal);
    }

    @Test
    @DisplayName("Цена равна SMA -> cигнал HOLD")
    public void shouldReturnHoldWhenPriceAboveSma() {
        SmaCrossoverStrategy strategy = new SmaCrossoverStrategy(calculator, period);
        List<Candle> candles = initCandles("3", "3", "3");

        Signal signal = strategy.generateSignal(candles);

        assertEquals(Signal.HOLD, signal);
    }

}
