package com.example.aitraiding.marketdata.infrastructure;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.CandleListener;
import com.example.aitraiding.marketdata.domain.LiveCandleSubscriber;
import com.example.aitraiding.marketdata.domain.MarketDataFetchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

public class BinanceKlineWebSocketClient implements LiveCandleSubscriber {

    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "wss://stream.testnet.binance.vision/ws/";

    @Override
    public void subscribe(String symbol, String interval,  CandleListener listenerCandle){
        String url = BASE_URL + symbol.toLowerCase() + "@kline_" + interval;
        connect(url, symbol, listenerCandle);
    }

    // package-private, а не private — чтобы юнит-тест мог проверить парсинг
    // и фильтр "свеча закрыта" напрямую, без реального WebSocket-соединения.
    Candle parseCandleWebSocketResponse(String json, String symbol) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode kLine = root.get("k");
            boolean isClosed = kLine.get("x").asBoolean();

            if (isClosed) {
                return new Candle(
                        symbol,
                        Instant.ofEpochMilli(kLine.get("t").asLong()),
                        new BigDecimal(kLine.get("o").asText()),
                        new BigDecimal(kLine.get("h").asText()),
                        new BigDecimal(kLine.get("l").asText()),
                        new BigDecimal(kLine.get("c").asText()),
                        new BigDecimal(kLine.get("v").asText())
                );
            }
            return null;

        } catch (JsonProcessingException e) {
            throw new MarketDataFetchException("Ошибка парсинга WebSoсket-сообщения от Binance", e);
        }
    }

    private void connect(String url, String symbol, CandleListener listenerCandle){
        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
                Candle candle = parseCandleWebSocketResponse(data.toString(), symbol);

                if(candle != null){
                    listenerCandle.onCandleClosed(candle);
                }
                socket.request(1);
                return null;
            }

            @Override
            public void onOpen(WebSocket socket) {
                System.out.println("Соединение открыто: ");
                WebSocket.Listener.super.onOpen(socket);
            }

            @Override
            public void onError(WebSocket socket, Throwable throwable) {
                System.out.println("Ошибка: " + throwable.getMessage());
                reconnect(url, symbol, listenerCandle);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                System.out.println("Соединение закрыто. Код: " + statusCode + ", причина: " + reason);
                reconnect(url, symbol, listenerCandle);
                return null;
            }
        };

        WebSocket socket = client.newWebSocketBuilder()
                .buildAsync(URI.create(url), listener)
                .join();
    }

    private void reconnect(String url, String symbol, CandleListener listenerCandle){
        try {
            Thread.sleep(5000);
            connect(url, symbol, listenerCandle);
        } catch (InterruptedException e) {
            throw new MarketDataFetchException("Ошибка переподключения к Binance", e);
        }
    }
}
