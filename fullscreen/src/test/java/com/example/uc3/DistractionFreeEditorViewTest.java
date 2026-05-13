package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.FullscreenTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.FullscreenState;
import com.vaadin.flow.component.textfield.TextArea;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = DistractionFreeEditorView.class)
class DistractionFreeEditorViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithEditorAndButtons() {
        navigate(DistractionFreeEditorView.class);

        assertTrue($view(H1.class).all().stream().anyMatch(h -> h.getText()
                .equals("UC3 — Distraction-free editor")));
        assertTrue(!$view(TextArea.class).all().isEmpty());
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Expand to fullscreen".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Done".equals(b.getText())));
    }

    @Test
    void wordCountUpdatesOnTextChange() {
        navigate(DistractionFreeEditorView.class);
        runPendingSignalsTasks();

        TextArea editor = $view(TextArea.class).all().getFirst();
        test(editor).setValue("hello world this is fullscreen");
        runPendingSignalsTasks();

        assertTrue(
                $view(Span.class).all().stream()
                        .anyMatch(s -> "5 words".equals(s.getText())),
                "word count should report 5 words after typing");
    }

    @Test
    void stateBadgeReflectsFullscreenSignal() {
        navigate(DistractionFreeEditorView.class);
        runPendingSignalsTasks();

        FullscreenTestSupport.setFullscreenState(FullscreenState.FULLSCREEN);
        runPendingSignalsTasks();
        assertBadgeContains("Focused");

        FullscreenTestSupport.setFullscreenState(FullscreenState.UNSUPPORTED);
        runPendingSignalsTasks();
        assertBadgeContains("not supported");
    }

    private void assertBadgeContains(String fragment) {
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> s.getClassNames().contains("status-badge")
                        && s.getText() != null
                        && s.getText().contains(fragment)),
                "expected status badge to contain \"" + fragment + "\"");
    }
}
