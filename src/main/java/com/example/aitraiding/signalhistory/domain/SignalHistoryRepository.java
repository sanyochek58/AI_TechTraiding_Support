package com.example.aitraiding.signalhistory.domain;

import java.util.List;

/**
 * "Порт" — контракт на сохранение и получение истории сигналов.
 * Реализация (адаптер) находится в infrastructure — SQLite-репозиторий.
 */
public interface SignalHistoryRepository {

    void save(SignalRecord signalRecord);

    List<SignalRecord> findRecent(int limit);
}