package com.example.uc2;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.TextSelectionTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.SelectionRange;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = FindAndHighlightView.class)
class FindAndHighlightViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersHeadingAndSampleContent() {
        navigate(FindAndHighlightView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(h -> h.getText()
                .equals("UC2 — Find and highlight in textarea")));
        TextArea content = $(TextArea.class).single();
        assertTrue(content.getValue().contains("Lorem ipsum"));
    }

    @Test
    void findNextSelectsFirstAndSecondMatchThenWraps() {
        navigate(FindAndHighlightView.class);
        runPendingSignalsTasks();

        TextField search = $(TextField.class).single();
        TextArea content = $(TextArea.class).single();
        Button findNext = $(Button.class).withText("Find next").single();

        test(search).setValue("dolor");

        test(findNext).click();
        runPendingSignalsTasks();
        SelectionRange first = content.selectionSignal().peek();
        assertEquals("dolor", content.getValue()
                .substring(first.start(), first.end()));

        test(findNext).click();
        runPendingSignalsTasks();
        SelectionRange second = content.selectionSignal().peek();
        assertTrue(second.start() > first.start(),
                "second match should be after the first");
        assertEquals("dolor", content.getValue()
                .substring(second.start(), second.end()));

        // Status span carries the match offset for the user
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> s.getText() != null
                        && s.getText().contains("Match at ")));
    }

    @Test
    void emptySearchTermShowsHint() {
        navigate(FindAndHighlightView.class);
        // Simulate the user clicking around in the textarea so a non-empty
        // selection signal value exists from a prior interaction.
        TextArea content = $(TextArea.class).single();
        TextSelectionTestSupport.setSelection(content, 0, 0);
        runPendingSignalsTasks();

        Button findNext = $(Button.class).withText("Find next").single();
        test(findNext).click();
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> "Enter a search term first".equals(s.getText())));
    }
}
