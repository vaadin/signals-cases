package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = RefreshStaleDataView.class)
class RefreshStaleDataViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithRateAndManualRefresh() {
        navigate(RefreshStaleDataView.class);
        runPendingSignalsTasks();

        // The card shows a 4-decimal exchange rate.
        assertTrue($view(Span.class).all().stream().anyMatch(s -> {
            String t = s.getText();
            return t != null && t.matches("\\d+\\.\\d{4}");
        }), "rate value should be rendered as 0.0000-style number");

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Refresh now".equals(b.getText())));
    }

    @Test
    void manualRefreshUpdatesRateValue() {
        navigate(RefreshStaleDataView.class);
        runPendingSignalsTasks();

        Span before = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().matches("\\d+\\.\\d{4}"))
                .findFirst().orElseThrow();
        String beforeRate = before.getText();

        // Click refresh enough times to almost certainly land on a different
        // value (the random walk has a 50% chance per click of changing
        // direction, but the value differs from the previous one with very
        // high probability after a few clicks).
        Button refresh = $view(Button.class).all().stream()
                .filter(b -> "Refresh now".equals(b.getText())).findFirst()
                .orElseThrow();
        for (int i = 0; i < 10; i++) {
            test(refresh).click();
            runPendingSignalsTasks();
        }

        Span after = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().matches("\\d+\\.\\d{4}"))
                .findFirst().orElseThrow();
        assertTrue(!beforeRate.equals(after.getText()),
                "rate should change after several manual refreshes");
    }
}
