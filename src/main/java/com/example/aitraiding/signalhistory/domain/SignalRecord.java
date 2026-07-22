package com.example.aitraiding.signalhistory.domain;

import com.example.aitraiding.strategy.domain.Signal;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Запись об одном сгенерированном торговом сигнале — то, что
 * реально сохраняется в историю для последующего backtesting.
 *
 * @param symbol         торговый символ, например "BTCUSDT"
 * @param signal         сам сигнал (LONG/SHORT/HOLD)
 * @param priceAtSignal  цена актива в момент генерации сигнала
 * @param timestamp      момент времени, когда сигнал был сгенерирован
 */
public record SignalRecord(
        String symbol,
        Signal signal,
        BigDecimal priceAtSignal,
        Instant timestamp
) {
}