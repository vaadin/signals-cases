package com.example.usecase25;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.html.Span;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase25View.class)
@WithMockUser
class UseCase25ViewTest extends SpringBrowserlessTest {

    @Test
    void allEightStockSymbolsRendered() {
        navigate(UseCase25View.class);
        runPendingSignalsTasks();

        // All 8 stock symbols from StockPriceSimulator.INITIAL_STOCKS
        for (String symbol : new String[] { "AAPL", "GOOGL", "MSFT", "AMZN",
                "TSLA", "NVDA", "META", "NFLX" }) {
            assertTrue(
                    $view(Span.class).all().stream()
                            .anyMatch(s -> symbol.equals(s.getText())),
                    "Missing stock symbol: " + symbol);
        }
    }

    @Test
    void initialPricesDisplayed() {
        navigate(UseCase25View.class);
        runPendingSignalsTasks();

        // Verify some initial prices are displayed
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> s.getText() != null
                        && s.getText().contains("$189.84")));
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> s.getText() != null
                        && s.getText().contains("$141.80")));
    }
}
