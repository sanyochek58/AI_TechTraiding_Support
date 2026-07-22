package com.example.aitraiding.signalhistory.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Signal(
        String symbol,
        Signal signal,
        BigDecimal priceAtSignal,
        Instant timestamp
){}
