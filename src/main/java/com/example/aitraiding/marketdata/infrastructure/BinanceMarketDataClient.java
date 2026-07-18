package com.example.aitraiding.marketdata.infrastructure;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.MarketDataFetchException;
import com.example.aitraiding.marketdata.domain.MarketDataProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Адаптер (реализация порта {@link MarketDataProvider}) для получения
 * свечей с Binance Testnet через публичный REST-эндпоинт klines.
 * <p>
 * Это единственное место в проекте, которое знает про конкретный формат
 * ответа Binance (массив массивов без именованных полей) и детали HTTP.
 * Наружу (в domain и application) утечка этих деталей не допускается —
 * все "грязные" checked-исключения (IOException, InterruptedException,
 * JsonProcessingException) перехватываются здесь и оборачиваются
 * в {@link MarketDataFetchException}, чтобы не нарушать контракт
 * интерфейса {@link MarketDataProvider}.
 */
public class BinanceMarketDataClient implements MarketDataProvider {

    private static final String BASE_URL = "https://testnet.binance.vision/api";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<Candle> getRecentCandles(String symbol, String interval, int limit) {
        try {
            String url = BASE_URL + "/v3/klines?symbol=" + symbol
                    + "&interval=" + interval + "&limit=" + limit;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Binance при ошибке (неверный символ, лимит запросов и т.д.)
            // возвращает JSON-объект с описанием ошибки вместо массива
            // свечей — падаем сразу с понятным сообщением (fail-fast),
            // а не пытаемся распарсить это как валидные данные.
            if (response.statusCode() != 200) {
                throw new MarketDataFetchException(
                        "Binance вернул ошибку, статус: " + response.statusCode()
                                + ", тело: " + response.body(),
                        null
                );
            }

            return parseKlinesResponse(response.body(), symbol);

        } catch (IOException | InterruptedException e) {
            throw new MarketDataFetchException("Не удалось получить свечи с Binance", e);
        }
    }

    /**
     * Парсит "сырой" ответ Binance klines (JSON-массив массивов,
     * где значения определяются позицией, а не именем поля)
     * в список доменных объектов {@link Candle}.
     * <p>
     * Формат одного элемента: [openTime, open, high, low, close, volume, ...]
     * (последующие поля ответа Binance для наших задач пока не нужны).
     */
    private List<Candle> parseKlinesResponse(String json, String symbol) {
        ObjectMapper mapper = new ObjectMapper();
        List<Candle> candles = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(json);

            for (JsonNode candleArray : root) {
                long openTimeMillis = candleArray.get(0).asLong();
                String openPrice = candleArray.get(1).asText();
                String highPrice = candleArray.get(2).asText();
                String lowPrice = candleArray.get(3).asText();
                String closePrice = candleArray.get(4).asText();
                String volume = candleArray.get(5).asText();

                // Цены приходят строками — намеренно, чтобы не терять
                // точность через double. Передаём строку напрямую в
                // BigDecimal, минуя double целиком.
                Candle candle = new Candle(
                        symbol,
                        Instant.ofEpochMilli(openTimeMillis),
                        new BigDecimal(openPrice),
                        new BigDecimal(highPrice),
                        new BigDecimal(lowPrice),
                        new BigDecimal(closePrice),
                        new BigDecimal(volume)
                );
                candles.add(candle);
            }

            return candles;

        } catch (JsonProcessingException e) {
            throw new MarketDataFetchException("Ошибка парсинга ответа Binance", e);
        }
    }
}