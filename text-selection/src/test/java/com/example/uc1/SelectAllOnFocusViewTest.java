package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = SelectAllOnFocusView.class)
class SelectAllOnFocusViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithExpectedHeadings() {
        navigate(SelectAllOnFocusView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(
                h -> "UC1 — Select all on focus".equals(h.getText())));
        assertEquals("10", $(TextField.class).single().getValue());
    }

    @Test
    void focusEventIncrementsCounter() {
        navigate(SelectAllOnFocusView.class);

        TextField quantity = $(TextField.class).single();
        Span counter = $view(Span.class).withText("0").single();

        test(quantity).focus();
        runPendingSignalsTasks();
        assertEquals("1", counter.getText());

        test(quantity).blur();
        test(quantity).focus();
        runPendingSignalsTasks();
        assertEquals("2", counter.getText());
    }
}
