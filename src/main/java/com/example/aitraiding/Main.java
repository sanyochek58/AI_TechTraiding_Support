package com.example.aitraiding;

import com.example.aitraiding.marketdata.application.MarketDataService;
import com.example.aitraiding.console.CandleConsolePrinter;
import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.LiveCandleSubscriber;
import com.example.aitraiding.marketdata.domain.MarketDataProvider;
import com.example.aitraiding.marketdata.infrastructure.BinanceKlineWebSocketClient;
import com.example.aitraiding.signalhistory.application.SignalHistoryService;
import com.example.aitraiding.signalhistory.domain.SignalHistoryRepository;
import com.example.aitraiding.signalhistory.domain.SignalRecord;
import com.example.aitraiding.signalhistory.infrastructure.SqliteSignalHistoryRepository;
import com.example.aitraiding.strategy.application.SignalGenerationService;
import com.example.aitraiding.strategy.domain.SmaCalculator;
import com.example.aitraiding.marketdata.infrastructure.BinanceMarketDataClient;
import com.example.aitraiding.strategy.domain.SignalStrategy;
import com.example.aitraiding.strategy.domain.SmaCrossoverStrategy;

import java.math.BigDecimal;
import java.util.List;

public class Main  {
    public static void main(String[] args) throws InterruptedException {
        MarketDataProvider provider = new BinanceMarketDataClient();
        MarketDataService service = new MarketDataService(provider);

        SmaCalculator calculator = new SmaCalculator();

        List<Candle> candles = service.getRecentCandles("BTCUSDT","5m", 10);

        new CandleConsolePrinter().print(candles);

        BigDecimal result = calculator.calculateSma(candles, 5);
        System.out.println("Среднее значение цены в результате торгов: " + result);

        // ----------------------------------------------------------------------------

        String dbUrl = "jdbc:sqlite:/Users/admin/DataGripProjects/Ai_Traiding_system_db/signals.db";
        SignalHistoryRepository historyRepository = new SqliteSignalHistoryRepository(dbUrl);
        SignalHistoryService historyService = new SignalHistoryService(historyRepository);

        SignalStrategy strategy = new SmaCrossoverStrategy(calculator, 5);
        SignalGenerationService signalGenerationService = new SignalGenerationService(strategy, 5, 10,
                (signal, candle) -> {
                    System.out.println("Новый сигнал: " + signal + " | по свече: " + candle);

                    SignalRecord record = new SignalRecord(
                            candle.symbol(), signal, candle.close(), candle.openTime()
                    );
                    historyService.save(record);
                });

        signalGenerationService.initializeWithHistory(candles);

        LiveCandleSubscriber subscriber = new BinanceKlineWebSocketClient();
        subscriber.subscribe("BTCUSDT", "5m", candle -> {
            signalGenerationService.onNewCandle(candle);
        });

        Thread.sleep(2_400_000);

    }
}