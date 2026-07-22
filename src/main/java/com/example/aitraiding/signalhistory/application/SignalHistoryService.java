package com.example.aitraiding.signalhistory.application;

import com.example.aitraiding.signalhistory.domain.SignalHistoryRepository;
import com.example.aitraiding.signalhistory.domain.SignalRecord;

import java.math.BigDecimal;
import java.util.List;

public class SignalHistoryService {

    private final SignalHistoryRepository signalHistoryRepository;

    public SignalHistoryService(SignalHistoryRepository signalHistoryRepository) {
        this.signalHistoryRepository = signalHistoryRepository;
    }

    public void save(SignalRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("SignalRecord не может быть null");
        }
        if (record.symbol() == null || record.symbol().isBlank()) {
            throw new IllegalArgumentException("Символ актива не может быть пустым");
        }
        if (record.priceAtSignal().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена во время сигнала не может быть отрицательной");
        }

        signalHistoryRepository.save(record);
    }


    public List<SignalRecord> findRecent(int limit){
        if (limit <= 0) {
            throw new IllegalArgumentException("limit должен быть положительным и больше 0");
        }
        return signalHistoryRepository.findRecent(limit);
    }

}
