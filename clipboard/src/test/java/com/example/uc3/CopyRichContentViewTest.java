package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyRichContentView.class)
class CopyRichContentViewTest extends SpringBrowserlessTest {

    @Test
    void clickingCopy_writesBothPlainTextAndHtml() {
        navigate(CopyRichContentView.class);

        test(copyButton()).click();

        ClipboardSimulator clipboard = ClipboardSimulator.current();
        assertEquals("Visit Vaadin (https://vaadin.com) for Java web apps.",
                clipboard.text());
        String html = clipboard.html();
        assertNotNull(html);
        assertTrue(html.contains("<a href=\"https://vaadin.com\">Vaadin</a>"),
                "HTML representation should carry the rich markup: " + html);
        assertTrue(html.contains("<strong>Java web apps</strong>"), html);
    }

    private Button copyButton() {
        return findInView(Button.class).all().stream()
                .filter(b -> "Copy rich content".equals(b.getText()))
                .findFirst().orElseThrow();
    }
}
