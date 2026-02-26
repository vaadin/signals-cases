package com.example.usecase25;

import jakarta.annotation.security.PermitAll;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.example.usecase23.SchedulerService;
import com.example.views.MainLayout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

import org.jspecify.annotations.Nullable;

@PageTitle("Use Case 25: Stock Ticker")
@Route(value = "use-case-25", layout = MainLayout.class)
@Menu(order = 25, title = "UC 25: Stock Ticker")
@PermitAll
public class UseCase25View extends Main {

    private static final List<StockQuote> INITIAL_STOCKS = List.of(
            new StockQuote("AAPL", "Apple Inc.", 189.84, 0, 0),
            new StockQuote("GOOGL", "Alphabet Inc.", 141.80, 0, 0),
            new StockQuote("MSFT", "Microsoft Corp.", 378.91, 0, 0),
            new StockQuote("AMZN", "Amazon.com Inc.", 178.25, 0, 0),
            new StockQuote("TSLA", "Tesla Inc.", 248.42, 0, 0),
            new StockQuote("NVDA", "NVIDIA Corp.", 495.22, 0, 0),
            new StockQuote("META", "Meta Platforms", 390.42, 0, 0),
            new StockQuote("NFLX", "Netflix Inc.", 476.58, 0, 0));

    private final List<ValueSignal<StockQuote>> stockSignals = new ArrayList<>();
    private final Random random = new Random();
    private @Nullable String taskId;

    public UseCase25View(SchedulerService schedulerService) {
        addClassName("stock-ticker-view");
        getStyle().set("display", "block").set("padding", "var(--lumo-space-l)");

        var title = new H2("Use Case 25: Stock Ticker");
        var description = new Paragraph(
                "Real-time stock price updates using Element.flashClass() to "
                        + "visually highlight price direction — green flash for "
                        + "price up, red flash for price down.");

        // Table header
        Div header = createHeaderRow();

        // Stock rows container
        Div stockList = new Div();
        stockList.getStyle().set("display", "flex")
                .set("flex-direction", "column");

        for (StockQuote initial : INITIAL_STOCKS) {
            var stockSignal = new ValueSignal<>(initial);
            stockSignals.add(stockSignal);
            stockList.add(createStockRow(stockSignal));
        }

        add(title, description, header, stockList);

        addAttachListener(event -> {
            UI ui = event.getUI();
            taskId = "stock-ticker-" + ui.getUIId();
            schedulerService.scheduleTask(taskId, ui,
                    this::updatePrices, 1500, 1500, TimeUnit.MILLISECONDS);
        });

        addDetachListener(event -> {
            if (taskId != null) {
                schedulerService.cancelTask(taskId);
            }
        });
    }

    private Div createHeaderRow() {
        Div row = new Div();
        row.getStyle().set("display", "grid")
                .set("grid-template-columns",
                        "80px 1fr 120px 100px 100px")
                .set("gap", "var(--lumo-space-s)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border-bottom", "2px solid var(--lumo-contrast-20pct)")
                .set("font-weight", "bold")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        row.add(headerCell("Symbol"), headerCell("Company"),
                headerCell("Price", true), headerCell("Change", true),
                headerCell("% Change", true));
        return row;
    }

    private Span headerCell(String text) {
        return headerCell(text, false);
    }

    private Span headerCell(String text, boolean alignRight) {
        Span span = new Span(text);
        if (alignRight) {
            span.getStyle().set("text-align", "right");
        }
        return span;
    }

    private Div createStockRow(ValueSignal<StockQuote> stockSignal) {
        Div row = new Div();
        row.getStyle().set("display", "grid")
                .set("grid-template-columns",
                        "80px 1fr 120px 100px 100px")
                .set("gap", "var(--lumo-space-s)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("align-items", "center")
                .set("border-bottom",
                        "1px solid var(--lumo-contrast-10pct)");

        // Symbol
        Span symbol = new Span();
        symbol.bindText(stockSignal.map(StockQuote::symbol));
        symbol.getStyle().set("font-weight", "bold")
                .set("font-family", "monospace");

        // Company name
        Span name = new Span();
        name.bindText(stockSignal.map(StockQuote::name));
        name.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Price
        Span price = new Span();
        price.bindText(stockSignal
                .map(q -> String.format("$%.2f", q.price())));
        price.getStyle().set("text-align", "right")
                .set("font-family", "monospace")
                .set("font-weight", "600")
                .set("border-radius", "4px")
                .set("padding", "2px 6px");

        // Change
        Span change = new Span();
        change.bindText(stockSignal.map(q -> {
            String prefix = q.change() >= 0 ? "+" : "";
            return prefix + String.format("%.2f", q.change());
        }));
        change.getStyle().set("text-align", "right")
                .set("font-family", "monospace");

        // % Change
        Span pctChange = new Span();
        pctChange.bindText(stockSignal.map(q -> {
            String prefix = q.changePercent() >= 0 ? "+" : "";
            return prefix + String.format("%.2f%%", q.changePercent());
        }));
        pctChange.getStyle().set("text-align", "right")
                .set("font-family", "monospace");

        // React to price changes with flash effect
        List<Element> flashTargets = List.of(price.getElement(),
                change.getElement(), pctChange.getElement());
        Signal.effect(row, () -> {
            StockQuote current = stockSignal.get();
            String flashClass = current.change() >= 0 ? "price-up"
                    : "price-down";
            flashTargets.forEach(el -> el.flashClass(flashClass));
        });

        row.add(symbol, name, price, change, pctChange);
        return row;
    }

    /**
     * Scheduled callback — only updates signal data, no UI calls.
     */
    private void updatePrices() {
        int stocksToUpdate = 2 + random.nextInt(4); // 2–5 stocks per tick
        for (int i = 0; i < stocksToUpdate; i++) {
            int index = random.nextInt(stockSignals.size());
            ValueSignal<StockQuote> stockSignal = stockSignals.get(index);

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
