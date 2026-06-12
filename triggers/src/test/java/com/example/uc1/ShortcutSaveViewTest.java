package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ShortcutSaveView.class)
class ShortcutSaveViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithFieldAndStatus() {
        navigate(ShortcutSaveView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Ctrl+S save".equals(h.getText())));
        assertNotNull(findInView(TextField.class).id("message"));
        assertEquals("(not saved yet)",
                findInView(Span.class).id("status").getText());
    }
}
