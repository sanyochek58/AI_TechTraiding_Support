package com.example.aitraiding.strategy.domain;

import com.example.aitraiding.marketdata.domain.Candle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SmaCalculatorTest {

    private final SmaCalculator smaCalculator = new SmaCalculator();

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
    @DisplayName("Корректная работа калькулятора")
    public void shouldCalculateSmaCorrectly(){
        List<Candle> candles = initCandles("7", "10", "11");

        BigDecimal result = smaCalculator.calculateSma(candles, 3);

        assertEquals(new BigDecimal("9.33"), result);
    }

    @Test
    @DisplayName("Выброс ошибки из за недостаточного количества свечей")
    public void shouldThrowWhenNotEnoughCandles(){
        List<Candle> candles = initCandles("7", "10", "11");

        assertThrows(IllegalArgumentException.class, () -> smaCalculator.calculateSma(candles, 5));
    }

    @Test
    @DisplayName("Корректная работа при граничном случае")
    public void shouldCalculateSmaCorrectlyWithOtherIncident(){
        List<Candle> candles = initCandles("7", "10", "11");

        BigDecimal result = smaCalculator.calculateSma(candles, 1);

        assertEquals(new BigDecimal("11.00"), result);
    }
}
