package com.example.usecase25;

import java.util.List;
import java.util.Random;

import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

class StockPriceSimulator {

    static final List<StockQuote> INITIAL_STOCKS = List.of(
            new StockQuote("AAPL", "Apple Inc.", 189.84, 0, 0),
            new StockQuote("GOOGL", "Alphabet Inc.", 141.80, 0, 0),
            new StockQuote("MSFT", "Microsoft Corp.", 378.91, 0, 0),
            new StockQuote("AMZN", "Amazon.com Inc.", 178.25, 0, 0),
            new StockQuote("TSLA", "Tesla Inc.", 248.42, 0, 0),
            new StockQuote("NVDA", "NVIDIA Corp.", 495.22, 0, 0),
            new StockQuote("META", "Meta Platforms", 390.42, 0, 0),
            new StockQuote("NFLX", "Netflix Inc.", 476.58, 0, 0));

    private static final Random random = new Random();

    static void updatePrices(ListSignal<StockQuote> stockSignals) {
        List<ValueSignal<StockQuote>> entries = stockSignals.peek();
        int stocksToUpdate = 2 + random.nextInt(4); // 2–5 stocks per tick
        for (int i = 0; i < stocksToUpdate; i++) {
            int index = random.nextInt(entries.size());
            ValueSignal<StockQuote> stockSignal = entries.get(index);

            StockQuote current = stockSignal.peek();
            double oldPrice = current.price();

            // Random price change: -2% to +2%
            double changePct = (random.nextDouble() - 0.5) * 4.0;
            double priceChange = oldPrice * changePct / 100.0;
            double newPrice = Math.max(1.0, oldPrice + priceChange);

            double totalChange = newPrice
                    - INITIAL_STOCKS.get(index).price();
            double totalChangePct = (totalChange
                    / INITIAL_STOCKS.get(index).price()) * 100.0;

            stockSignal.set(new StockQuote(current.symbol(), current.name(),
                    newPrice, totalChange, totalChangePct));
        }
    }
}
