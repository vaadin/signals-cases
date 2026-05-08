package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.TextSelectionTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextArea;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = InsertTemplateView.class)
class InsertTemplateViewTest extends SpringBrowserlessTest {

    @Test
    void viewRenders() {
        navigate(InsertTemplateView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(
                h -> "UC4 — Insert template at cursor".equals(h.getText())));
    }

    @Test
    void greetingInsertsAtCurrentCursor() {
        navigate(InsertTemplateView.class);
        TextArea editor = $(TextArea.class).single();
        // Position the cursor at offset 4 ("Hi,\n" + here)
        TextSelectionTestSupport.setSelection(editor, 4, 4);
        runPendingSignalsTasks();

        String original = editor.getValue();
        Button greeting = $(Button.class).withText("Greeting").single();
        test(greeting).click();
        runPendingSignalsTasks();

        String expected = original.substring(0, 4) + "Hello {name}!"
                + original.substring(4);
        assertEquals(expected, editor.getValue());
    }

    @Test
    void signatureInsertsAtStart() {
        navigate(InsertTemplateView.class);
        TextArea editor = $(TextArea.class).single();
        TextSelectionTestSupport.setSelection(editor, 0, 0);
        runPendingSignalsTasks();

        String original = editor.getValue();
        Button signature = $(Button.class).withText("Signature").single();
        test(signature).click();
        runPendingSignalsTasks();

        assertTrue(editor.getValue().startsWith("Best regards,\nJamie"),
                "value should start with the inserted signature, was: "
                        + editor.getValue());
        assertEquals("Best regards,\nJamie".length() + original.length(),
                editor.getValue().length());
    }
}
