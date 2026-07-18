package com.example.aitraiding.marketdata.domain;

import java.io.IOException;
import java.util.List;

/**
 * "Порт" — контракт на получение рыночных данных (свечей).
 * Реализация (адаптер) находится в infrastructure —
 * см. BinanceMarketDataClient.
 */
public interface MarketDataProvider {

    /**
     * Возвращает последние N свечей по указанному символу и интервалу.
     *
     * @param symbol   торговая пара, например "BTCUSDT"
     * @param interval интервал свечи, например "5m"
     * @param limit    сколько последних свечей вернуть
     */

    List<Candle> getRecentCandles(String symbol, String interval, int limit);
}