package com.example.aitraiding.marketdata.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Калькулятор простого скользящего среднего (Simple Moving Average, SMA)
 * по цене закрытия (close) последних N свечей.
 * <p>
 * SMA — один из базовых индикаторов технического анализа: сглаживает
 * "шум" цены, показывая среднее значение за выбранный период, а не
 * дёрганое движение каждой отдельной свечи.
 * <p>
 * Чистая доменная логика — не зависит от инфраструктуры (HTTP, БД),
 * что позволяет тестировать её в полной изоляции.
 */
public class SmaCalculator {

    /**
     * Рассчитывает SMA по цене закрытия последних {@code period} свечей.
     *
     * @param candles список свечей, отсортированный от старых к новым
     * @param period  количество последних свечей для усреднения (окно SMA)
     * @return среднее значение close за период, округлённое до 2 знаков
     *         после запятой (HALF_UP)
     * @throws IllegalArgumentException если количество переданных свечей
     *                                   меньше, чем {@code period}
     */
    public BigDecimal calculateSma(List<Candle> candles, int period) {
        if (candles.size() < period) {
            throw new IllegalArgumentException(
                    "Недостаточно данных для расчёта SMA: нужно минимум " + period
                            + " свечей, передано " + candles.size()
            );
        }

        // Берём только последние `period` свечей — SMA считается
        // по "скользящему окну", а не по всей истории.
        List<Candle> lastCandles = candles.subList(candles.size() - period, candles.size());

        // BigDecimal неизменяем (immutable): add() возвращает НОВЫЙ объект,
        // поэтому обязательно переприсваиваем sum на каждой итерации.
        BigDecimal sum = BigDecimal.ZERO;
        for (Candle candle : lastCandles) {
            sum = sum.add(candle.close());
        }

        // divide() требует явно указать точность и режим округления,
        // иначе можно получить ArithmeticException на бесконечной дроби.
        return sum.divide(new BigDecimal(period), 2, RoundingMode.HALF_UP);
    }
}