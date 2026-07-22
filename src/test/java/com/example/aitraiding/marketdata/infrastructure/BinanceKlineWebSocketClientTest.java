package com.example.aitraiding.marketdata.infrastructure;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.MarketDataFetchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тесты на разбор kline-сообщения WebSocket-стрима Binance.
 * Реальное соединение не открываем — проверяем логику парсинга
 * и фильтр "свеча закрыта" (поле k.x) через package-private
 * {@link BinanceKlineWebSocketClient#parseCandleWebSocketResponse}.
 */
public class BinanceKlineWebSocketClientTest {

    private final BinanceKlineWebSocketClient client = new BinanceKlineWebSocketClient();

    private String message(boolean isClosed) {
        return """
                {
                  "e": "kline",
                  "k": {
                    "t": 1710000000000,
                    "o": "65000.10",
                    "h": "65100.20",
                    "l": "64900.30",
                    "c": "65050.40",
                    "v": "12.5",
                    "x": %s
                  }
                }
                """.formatted(isClosed);
    }

    @Test
    @DisplayName("Свеча закрыта (x=true) -> возвращается Candle с корректными полями")
    public void shouldParseClosedCandle() {
        Candle candle = client.parseCandleWebSocketResponse(message(true), "BTCUSDT");

        assertEquals("BTCUSDT", candle.symbol());
        assertEquals(Instant.ofEpochMilli(1710000000000L), candle.openTime());
        assertEquals(new BigDecimal("65000.10"), candle.open());
        assertEquals(new BigDecimal("65100.20"), candle.high());
        assertEquals(new BigDecimal("64900.30"), candle.low());
        assertEquals(new BigDecimal("65050.40"), candle.close());
        assertEquals(new BigDecimal("12.5"), candle.volume());
    }

    @Test
    @DisplayName("Свеча ещё не закрыта (x=false) -> null (сигнал не генерируем по незакрытой свече)")
    public void shouldReturnNullForOpenCandle() {
        Candle candle = client.parseCandleWebSocketResponse(message(false), "BTCUSDT");

        assertNull(candle);
    }

    @Test
    @DisplayName("Некорректный JSON -> MarketDataFetchException")
    public void shouldThrowOnMalformedJson() {
        assertThrows(MarketDataFetchException.class,
                () -> client.parseCandleWebSocketResponse("{ это не json", "BTCUSDT"));
    }
}
