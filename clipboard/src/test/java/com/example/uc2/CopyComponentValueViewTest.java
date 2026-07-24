package com.example.uc2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ViewPackages(classes = CopyComponentValueView.class)
class CopyComponentValueViewTest extends SpringBrowserlessTest {

    @Test
    void clickingCopy_writesCurrentFieldValueToClipboard() {
        navigate(CopyComponentValueView.class);
        TextField field = shareLinkField();
        assertEquals("https://example.com/share/abc123", field.getValue());

        test(copyButton()).click();

        assertEquals("https://example.com/share/abc123",
                ClipboardSimulator.current().text());
    }

    @Test
    void editingField_thenCopying_writesTheEditedValue() {
        navigate(CopyComponentValueView.class);

        test(shareLinkField()).setValue("https://example.com/share/xyz999");
        test(copyButton()).click();

        // The value is read from the field at click time, so the edit is
        // reflected without a prior round-trip.
        assertEquals("https://example.com/share/xyz999",
                ClipboardSimulator.current().text());
    }

    private TextField shareLinkField() {
        return findInView(TextField.class).all().stream()
                .filter(f -> "Share link".equals(f.getLabel())).findFirst()
                .orElseThrow();
    }

    private Button copyButton() {
        return findInView(Button.class).all().stream()
                .filter(b -> "Copy".equals(b.getText())).findFirst()
                .orElseThrow();
    }
}
