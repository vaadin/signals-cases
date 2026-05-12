package com.example.uc7;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = FormatAndSelectView.class)
class FormatAndSelectViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithDefaultTitle() {
        navigate(FormatAndSelectView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(
                h -> "UC7 — Post-transform select-all".equals(h.getText())));
        assertEquals("Hello, Awesome World!",
                $(TextField.class).single().getValue());
    }

    @Test
    void formatSlugifiesTheValue() {
        navigate(FormatAndSelectView.class);

        TextField title = $(TextField.class).single();
        test(title).setValue("Hello, Awesome World!!!");

        test($(Button.class).withText("Format").single()).click();
        runPendingSignalsTasks();

        assertEquals("hello-awesome-world", title.getValue());
    }
}
