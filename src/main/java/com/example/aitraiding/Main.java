package com.example.aitraiding;

import com.example.aitraiding.marketdata.application.MarketDataService;
import com.example.aitraiding.console.CandleConsolePrinter;
import com.example.aitraiding.marketdata.domain.Candle;
import com.example.aitraiding.marketdata.domain.MarketDataProvider;
import com.example.aitraiding.marketdata.domain.SmaCalculator;
import com.example.aitraiding.marketdata.infrastructure.BinanceMarketDataClient;
import com.example.aitraiding.strategy.domain.Signal;
import com.example.aitraiding.strategy.domain.SignalStrategy;
import com.example.aitraiding.strategy.domain.SmaCrossoverStrategy;

import java.math.BigDecimal;
import java.util.List;

public class Main  {
    public static void main(String[] args) {
        MarketDataProvider provider = new BinanceMarketDataClient();
        MarketDataService service = new MarketDataService(provider);

        SmaCalculator calculator = new SmaCalculator();

        List<Candle> candles = service.getRecentCandles("BTCUSDT","5m", 10);

        new CandleConsolePrinter().print(candles);

        BigDecimal result = calculator.calculateSma(candles, 5);
        System.out.println("Среднее значение цены в результате торгов: " + result);


        SignalStrategy strategy = new SmaCrossoverStrategy(calculator, 5);
        Signal signal = strategy.generateSignal(candles);
        System.out.println("Сигнал: " + signal);
    }
}