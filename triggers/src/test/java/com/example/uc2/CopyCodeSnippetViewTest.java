package com.example.uc2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyCodeSnippetView.class)
class CopyCodeSnippetViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSnippetAndCopyButton() {
        navigate(CopyCodeSnippetView.class);

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> "UC2 — Copy a code snippet".equals(h.getText())));
        assertNotNull(findInView(Pre.class).id("snippet"));
        assertNotNull(findInView(Button.class).id("copy"));
    }
}
