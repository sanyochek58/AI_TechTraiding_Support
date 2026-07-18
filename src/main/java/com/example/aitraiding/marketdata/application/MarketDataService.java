package com.example.aitraiding.marketdata.application;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.MarketDataProvider;

import java.util.List;

/**
 * Application-слой (use-case) для работы с рыночными данными.
 * <p>
 * Оркестрирует получение свечей через {@link MarketDataProvider},
 * не зная деталей конкретной реализации (Binance REST, WebSocket,
 * заглушка для тестов — не важно, какая именно). Здесь может
 * появиться дополнительная логика, не относящаяся к "чистому" домену:
 * валидация входных параметров, логирование, кэширование.
 */
public class MarketDataService {

    private final MarketDataProvider provider;

    public MarketDataService(MarketDataProvider provider) {
        this.provider = provider;
    }

    /**
     * Возвращает последние N свечей по указанному символу и интервалу.
     *
     * @param symbol   торговая пара, например "BTCUSDT"
     * @param interval интервал свечи, например "5m"
     * @param limit    сколько последних свечей вернуть (должно быть > 0)
     * @throws IllegalArgumentException если limit не положительный
     */
    public List<Candle> getRecentCandles(String symbol, String interval, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit должен быть положительным и больше 0");
        }
        return provider.getRecentCandles(symbol, interval, limit);
    }
}