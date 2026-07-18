package com.example.aitraiding.console;

import com.example.aitraiding.marketdata.domain.Candle;

import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CandleConsolePrinter {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneOffset.UTC);

    public void print(List<Candle> candles) {
        for (Candle candle : candles) {
            System.out.printf(
                    "[%-8s] %-6s | Open %-10s High %-10s Low %-10s Close %-10s | Vol %s%n",
                    candle.symbol(),
                    TIME_FORMATTER.format(candle.openTime()),
                    round(candle.open()),
                    round(candle.high()),
                    round(candle.low()),
                    round(candle.close()),
                    round(candle.volume())
            );
        }
    }

    private String round(java.math.BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}