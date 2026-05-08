package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.TextSelectionTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.SelectionRange;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = ValidationJumpView.class)
class ValidationJumpViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersHeading() {
        navigate(ValidationJumpView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(
                h -> "UC3 — Jump to validation error".equals(h.getText())));
    }

    @Test
    void invalidCharactersGetSelected() {
        navigate(ValidationJumpView.class);
        runPendingSignalsTasks();

        TextField username = $(TextField.class).single();
        Button submit = $(Button.class).withText("Submit").single();

        // Default value "My Cool User" has the run "M" first invalid.
        test(submit).click();
        runPendingSignalsTasks();

        SelectionRange sel = username.selectionSignal().peek();
        assertTrue(!sel.isEmpty(), "Selection should mark the bad characters");
        String selected = username.getValue().substring(sel.start(), sel.end());
        // Must contain at least one non-[a-z0-9_] char
        assertTrue(selected.chars().anyMatch(c -> !Character.isDigit(c)
                && (c < 'a' || c > 'z') && c != '_'),
                "selected substring should be the offending run, was: \""
                        + selected + "\"");

        assertTrue($view(Span.class).all().stream().anyMatch(s -> s.getText() != null
                && s.getText().startsWith("Invalid characters")));
    }

    @Test
    void shortValueGetsFullySelected() {
        navigate(ValidationJumpView.class);
        TextField username = $(TextField.class).single();
        username.setValue("ab");

        Button submit = $(Button.class).withText("Submit").single();
        test(submit).click();
        runPendingSignalsTasks();

        SelectionRange sel = username.selectionSignal().peek();
        assertEquals(0, sel.start());
        assertEquals(2, sel.end());
    }

    @Test
    void validValueShowsSuccessAndCollapsesAnyLeftoverSelection() {
        navigate(ValidationJumpView.class);
        TextField username = $(TextField.class).single();
        username.setValue("good_name_42");
        // Simulate leftover highlight from a prior failed validation.
        TextSelectionTestSupport.setSelection(username, 0, 4);
        runPendingSignalsTasks();

        Button submit = $(Button.class).withText("Submit").single();
        test(submit).click();
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream().anyMatch(s -> s.getText() != null
                && s.getText().contains("is valid")));
        SelectionRange sel = username.selectionSignal().peek();
        assertTrue(sel.isEmpty(),
                "deselect() in the success path should collapse the selection");
    }
}
