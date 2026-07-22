package com.example.aitraiding.signalhistory.infrastructure;

import com.example.aitraiding.signalhistory.domain.SignalHistoryRepository;
import com.example.aitraiding.signalhistory.domain.SignalRecord;
import com.example.aitraiding.strategy.domain.Signal;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SqliteSignalHistoryRepository implements SignalHistoryRepository {

    private final String dbUrl;

    public SqliteSignalHistoryRepository(String dbUrl) {
        this.dbUrl = dbUrl;

        try (Connection connection = DriverManager.getConnection(dbUrl);
             Statement statement = connection.createStatement()) {

            String sql = "CREATE TABLE IF NOT EXISTS signals(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "symbol TEXT NOT NULL," +
                    "signal TEXT NOT NULL," +
                    "price_at_signal TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL)";

            statement.execute(sql);
            System.out.println("Соединение с БД: УСПЕХ, таблица готова");

        } catch (SQLException ex) {
            throw new IllegalArgumentException("Ошибка при подключении к Базе данных", ex);
        }
    }

    @Override
    public void save(SignalRecord signalRecord) {
        String sql = "INSERT INTO signals (symbol, signal, price_at_signal, timestamp) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, signalRecord.symbol());
            stmt.setString(2, signalRecord.signal().toString());
            stmt.setString(3, signalRecord.priceAtSignal().toString());
            stmt.setString(4, signalRecord.timestamp().toString());

            int rows = stmt.executeUpdate();
            System.out.println("Запись в БД: сохранено строк = " + rows + " | " + signalRecord);

        } catch (SQLException e) {
            throw new IllegalArgumentException("Некорректный запрос к БД", e);
        }
    }

    @Override
    public List<SignalRecord> findRecent(int limit) {
        String sql = "SELECT symbol, signal, price_at_signal, timestamp FROM signals ORDER BY timestamp DESC LIMIT ?";
        List<SignalRecord> records = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String symbol = resultSet.getString("symbol");
                    Signal signal = Signal.valueOf(resultSet.getString("signal"));
                    BigDecimal price = new BigDecimal(resultSet.getString("price_at_signal"));
                    Instant timestamp = Instant.parse(resultSet.getString("timestamp"));

                    records.add(new SignalRecord(symbol, signal, price, timestamp));
                }
            }

            return records;

        } catch (SQLException e) {
            throw new IllegalArgumentException("Ошибка при чтении истории сигналов из БД", e);
        }
    }
}