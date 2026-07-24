package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;
import com.vaadin.flow.component.html.Pre;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = CopyStaticTextView.class)
class CopyStaticTextViewTest extends SpringBrowserlessTest {

    private static final String LINK = "https://example.com/share/abc123";

    @Test
    void textToCopyIsShown() {
        navigate(CopyStaticTextView.class);

        assertTrue(findInView(Pre.class).all().stream()
                .anyMatch(p -> LINK.equals(p.getText())));
    }

    @Test
    void clickingCopy_writesLinkToClipboard() {
        navigate(CopyStaticTextView.class);

        test(copyButton()).click();

        assertEquals(LINK, ClipboardSimulator.current().text());
    }

    @Test
    void clickingCopy_flashesCopiedFeedbackFromServerCallback() {
        navigate(CopyStaticTextView.class);
        Button copyButton = copyButton();
        assertEquals("Copy link", copyButton.getText());

        test(copyButton).click();

        // The onCopied callback runs on the server once the (simulated) write
        // resolves, flipping the button label to "Copied".
        assertEquals("Copied", copyButton.getText());
    }

    private Button copyButton() {
        return findInView(Button.class).all().stream()
                .filter(b -> "Copy link".equals(b.getText())
                        || "Copied".equals(b.getText()))
                .findFirst().orElseThrow();
    }
}
