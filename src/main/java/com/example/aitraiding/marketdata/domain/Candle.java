package com.example.aitraiding.marketdata.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Доменная модель свечи (OHLCV) — базовая единица рыночных данных.
 *
 * @param symbol    торговый символ, например "BTCUSDT"
 * @param openTime  время открытия свечи (начало интервала)
 * @param open      цена в момент открытия
 * @param high      максимальная цена за интервал
 * @param low       минимальная цена за интервал
 * @param close     цена в момент закрытия (конец интервала)
 * @param volume    объём торгов за интервал (сколько актива куплено/продано)
 */
public record Candle(
        String symbol,
        Instant openTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        BigDecimal volume
) {
}