package com.example.usecase25;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

class StockPriceSimulator {

    static final List<StockQuote> INITIAL_STOCKS = List.of(
            new StockQuote("AAPL", "Apple Inc.", new BigDecimal("189.84"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("GOOGL", "Alphabet Inc.", new BigDecimal("141.80"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("MSFT", "Microsoft Corp.", new BigDecimal("378.91"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("AMZN", "Amazon.com Inc.", new BigDecimal("178.25"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("TSLA", "Tesla Inc.", new BigDecimal("248.42"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("NVDA", "NVIDIA Corp.", new BigDecimal("495.22"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("META", "Meta Platforms", new BigDecimal("390.42"), BigDecimal.ZERO, BigDecimal.ZERO),
            new StockQuote("NFLX", "Netflix Inc.", new BigDecimal("476.58"), BigDecimal.ZERO, BigDecimal.ZERO));

    private static final Random random = new Random();

    static void updatePrices(ListSignal<StockQuote> stockSignals) {
        List<ValueSignal<StockQuote>> entries = stockSignals.peek();
        int stocksToUpdate = 2 + random.nextInt(4); // 2–5 stocks per tick
        for (int i = 0; i < stocksToUpdate; i++) {
            int index = random.nextInt(entries.size());
            ValueSignal<StockQuote> stockSignal = entries.get(index);

            StockQuote current = stockSignal.peek();
            BigDecimal oldPrice = current.price();

            // Random price change: -2% to +2%
            double changePct = (random.nextDouble() - 0.5) * 4.0;
            BigDecimal priceChange = oldPrice.multiply(BigDecimal.valueOf(changePct))
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal newPrice = oldPrice.add(priceChange).max(new BigDecimal("1.0"));

            BigDecimal totalChange = newPrice.subtract(INITIAL_STOCKS.get(index).price());
            BigDecimal totalChangePct = totalChange
                    .divide(INITIAL_STOCKS.get(index).price(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));

            stockSignal.set(new StockQuote(current.symbol(), current.name(),
                    newPrice, totalChange, totalChangePct));
        }
    }
}
