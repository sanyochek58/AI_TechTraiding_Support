package com.example.aitraiding.marketdata.infrastructure;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.MarketDataFetchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тесты на разбор "сырого" ответа Binance klines (JSON-массив массивов).
 * Реальный HTTP не дёргаем — проверяем только логику парсинга
 * через package-private метод {@link BinanceMarketDataClient#parseKlinesResponse}.
 */
public class BinanceMarketDataClientTest {

    private final BinanceMarketDataClient client = new BinanceMarketDataClient();

    @Test
    @DisplayName("Валидный ответ парсится в список свечей с корректным маппингом полей")
    public void shouldParseValidResponse() {
        String json = """
                [
                  [1710000000000, "65000.10", "65100.20", "64900.30", "65050.40", "12.5", 0, "0", 0],
                  [1710000300000, "65050.40", "65200.00", "65000.00", "65180.90", "8.1", 0, "0", 0]
                ]
                """;

        List<Candle> candles = client.parseKlinesResponse(json, "BTCUSDT");

        assertEquals(2, candles.size());

        Candle first = candles.get(0);
        assertEquals("BTCUSDT", first.symbol());
        assertEquals(Instant.ofEpochMilli(1710000000000L), first.openTime());
        assertEquals(new BigDecimal("65000.10"), first.open());
        assertEquals(new BigDecimal("65100.20"), first.high());
        assertEquals(new BigDecimal("64900.30"), first.low());
        assertEquals(new BigDecimal("65050.40"), first.close());
        assertEquals(new BigDecimal("12.5"), first.volume());
    }

    @Test
    @DisplayName("Цены-строки не теряют точность (парсятся через BigDecimal, минуя double)")
    public void shouldPreservePrecision() {
        String json = "[[1710000000000, \"0.12345678901234567890\", \"1\", \"1\", \"1\", \"1\"]]";

        List<Candle> candles = client.parseKlinesResponse(json, "BTCUSDT");

        assertEquals(new BigDecimal("0.12345678901234567890"), candles.get(0).open());
    }

    @Test
    @DisplayName("Пустой массив -> пустой список свечей")
    public void shouldReturnEmptyListForEmptyArray() {
        List<Candle> candles = client.parseKlinesResponse("[]", "BTCUSDT");

        assertTrue(candles.isEmpty());
    }

    @Test
    @DisplayName("Некорректный JSON -> MarketDataFetchException")
    public void shouldThrowOnMalformedJson() {
        assertThrows(MarketDataFetchException.class,
                () -> client.parseKlinesResponse("{ это не массив klines", "BTCUSDT"));
    }
}
