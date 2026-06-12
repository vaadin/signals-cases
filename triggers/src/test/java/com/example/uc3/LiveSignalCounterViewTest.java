package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = LiveSignalCounterView.class)
class LiveSignalCounterViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersAtZero() {
        navigate(LiveSignalCounterView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC3 — Copy a live signal value"
                        .equals(h.getText())));
        assertEquals("0", findInView(Span.class).id("counter").getText());
    }

    @Test
    void tickIncrementsCounter() {
        navigate(LiveSignalCounterView.class);
        Button tick = findInView(Button.class).id("tick");
        test(tick).click();
        test(tick).click();
        test(tick).click();
        assertEquals("3", findInView(Span.class).id("counter").getText(),
                "server-side click handler advances the signal");
    }
}
