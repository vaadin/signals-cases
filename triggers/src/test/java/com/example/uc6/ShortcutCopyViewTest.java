package com.example.uc6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ShortcutCopyView.class)
class ShortcutCopyViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersSourceField() {
        navigate(ShortcutCopyView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC6 — Copy via keyboard shortcut"
                        .equals(h.getText())));
        assertNotNull(findInView(TextField.class).id("source"));
    }
}
