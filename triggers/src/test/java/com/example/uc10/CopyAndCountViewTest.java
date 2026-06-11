package com.example.uc10;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyAndCountView.class)
class CopyAndCountViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersZeroCount() {
        navigate(CopyAndCountView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC10 — Copy + server callback".equals(h.getText())));
        assertNotNull(findInView(Button.class).id("copy"));
        assertEquals("copied 0×", findInView(Span.class).id("count").getText());
    }
}
