package com.example.uc15;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ScrollIntoViewView.class)
class ScrollIntoViewViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithJumpButtonsAndSections() {
        navigate(ScrollIntoViewView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC15 — Scroll into view".equals(h.getText())));
        assertNotNull(findInView(Button.class).id("to-a"));
        assertNotNull(findInView(Button.class).id("to-b"));
        assertNotNull(findInView(Button.class).id("to-c"));
        assertNotNull(findInView(Div.class).id("section-a"));
        assertNotNull(findInView(Div.class).id("section-b"));
        assertNotNull(findInView(Div.class).id("section-c"));
    }
}
