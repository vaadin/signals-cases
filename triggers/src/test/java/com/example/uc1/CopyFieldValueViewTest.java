package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Browserless tests for {@link CopyFieldValueView}. The new
 * {@code com.vaadin.flow.component.trigger.internal} API installs handlers
 * via {@code Element#addJsInitializer} and exposes no introspection surface,
 * so wiring is verified by Playwright; this test only checks the view
 * structure and that constructing it doesn't throw.
 */
@SpringBootTest
@ViewPackages(classes = CopyFieldValueView.class)
class CopyFieldValueViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithExpectedComponents() {
        navigate(CopyFieldValueView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC1 — Copy field value on click"
                        .equals(h.getText())));
        assertNotNull(findInView(TextField.class).id("source"));
        assertNotNull(findInView(Button.class).id("copy"));
    }
}
