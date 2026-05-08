package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.BlurNotifier.BlurEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.FocusNotifier.FocusEvent;
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

        ComponentUtil.fireEvent(quantity, new FocusEvent<>(quantity, true));
        runPendingSignalsTasks();
        assertEquals("1", counter.getText());

        ComponentUtil.fireEvent(quantity, new BlurEvent<>(quantity, true));
        ComponentUtil.fireEvent(quantity, new FocusEvent<>(quantity, true));
        runPendingSignalsTasks();
        assertEquals("2", counter.getText());
    }
}
