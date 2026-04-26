package com.example.usecase25;

import java.math.BigDecimal;

record StockQuote(String symbol, String name, BigDecimal price,
        BigDecimal change, BigDecimal changePercent) {
}
