package com.example.aitraiding.strategy.application;

import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.strategy.domain.Signal;
import com.example.aitraiding.strategy.domain.SignalStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignalGenerationServiceTest {

    private static final int PERIOD = 3;
    private static final int MAX_BUFFER = 5;

    /**
     * Стаб-стратегия: всегда возвращает заранее заданный сигнал и
     * запоминает список свечей, с которым её вызвали в последний раз, —
     * чтобы проверить, что сервис отдаёт стратегии правильный срез буфера.
     */
    private static class RecordingStrategy implements SignalStrategy {
        private final Signal signalToReturn;
        List<Candle> lastCandles;
        int callCount;

        RecordingStrategy(Signal signalToReturn) {
            this.signalToReturn = signalToReturn;
        }

        @Override
        public Signal generateSignal(List<Candle> candles) {
            this.lastCandles = candles;
            this.callCount++;
            return signalToReturn;
        }
    }

    /**
     * Слушатель, который запоминает все пары (сигнал, свеча),
     * пришедшие от сервиса.
     */
    private static class RecordingListener {
        final List<Signal> signals = new ArrayList<>();
        final List<Candle> candles = new ArrayList<>();

        void onSignalGenerated(Signal signal, Candle candle) {
            signals.add(signal);
            candles.add(candle);
        }
    }

    private Candle candle(String close) {
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

    @Test
    @DisplayName("Свечей в буфере меньше period -> сигнал не генерируется")
    public void shouldNotEmitSignalWhenBufferSmallerThanPeriod() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.LONG);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        service.onNewCandle(candle("10"));
        service.onNewCandle(candle("11"));

        assertEquals(0, strategy.callCount);
        assertTrue(listener.signals.isEmpty());
    }

    @Test
    @DisplayName("Буфер достиг period -> сигнал генерируется и уходит в listener")
    public void shouldEmitSignalWhenBufferReachesPeriod() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.LONG);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        service.onNewCandle(candle("10"));
        service.onNewCandle(candle("11"));
        service.onNewCandle(candle("12"));

        assertEquals(1, listener.signals.size());
        assertEquals(Signal.LONG, listener.signals.get(0));
    }

    @Test
    @DisplayName("В listener уходит именно последняя пришедшая свеча")
    public void shouldPassLatestCandleToListener() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.SHORT);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        service.onNewCandle(candle("10"));
        service.onNewCandle(candle("11"));
        Candle latest = candle("12");
        service.onNewCandle(latest);

        assertSame(latest, listener.candles.get(0));
    }

    @Test
    @DisplayName("Буфер не превышает maxBufferSize: стратегии отдаётся не более maxBufferSize свечей")
    public void shouldCapBufferAtMaxBufferSize() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.HOLD);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        // подаём больше свечей, чем вмещает буфер
        for (int i = 0; i < MAX_BUFFER + 3; i++) {
            service.onNewCandle(candle(String.valueOf(100 + i)));
        }

        assertEquals(MAX_BUFFER, strategy.lastCandles.size());
    }

    @Test
    @DisplayName("initializeWithHistory наполняет буфер, но не генерирует сигналы")
    public void shouldNotEmitSignalsDuringHistoryInitialization() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.LONG);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        List<Candle> history = List.of(candle("10"), candle("11"), candle("12"), candle("13"));
        service.initializeWithHistory(history);

        assertEquals(0, strategy.callCount);
        assertTrue(listener.signals.isEmpty());
    }

    @Test
    @DisplayName("После инициализации историей первая же новая свеча даёт сигнал")
    public void shouldEmitOnFirstCandleAfterHistoryInitialization() {
        RecordingStrategy strategy = new RecordingStrategy(Signal.LONG);
        RecordingListener listener = new RecordingListener();
        SignalGenerationService service =
                new SignalGenerationService(strategy, PERIOD, MAX_BUFFER, listener::onSignalGenerated);

        service.initializeWithHistory(List.of(candle("10"), candle("11"), candle("12")));
        service.onNewCandle(candle("13"));

        assertEquals(1, listener.signals.size());
    }
}
