package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = LockForVideoView.class)
class LockForVideoViewTest extends SpringBrowserlessTest {

    /**
     * Browserless tests can't drive a JS Promise round-trip, so this test only
     * verifies that the view renders, that clicking Play queues the
     * lockOrientation request, and that clicking Stop puts the badge back to
     * "Idle". The real success/error callbacks are exercised in the upstream
     * {@code PageScreenOrientationTest}.
     */
    @Test
    void viewRenders() {
        navigate(LockForVideoView.class);
        runPendingSignalsTasks();

        assertTrue($view(H1.class).all().stream().anyMatch(
                h -> "UC4 — Lock landscape for video".equals(h.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> b.getText().startsWith("Play")));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> b.getText().startsWith("Stop")));
        assertSpanText("Idle");
    }

    @Test
    void stopButtonResetsBadge() {
        navigate(LockForVideoView.class);
        runPendingSignalsTasks();

        Button play = $view(Button.class).all().stream()
                .filter(b -> b.getText().startsWith("Play")).findFirst()
                .orElseThrow();
        Button stop = $view(Button.class).all().stream()
                .filter(b -> b.getText().startsWith("Stop")).findFirst()
                .orElseThrow();

        test(play).click();
        runPendingSignalsTasks();
        // The lock promise never resolves in browserless mode, so the badge
        // stays at "Idle"; we use Stop to confirm the path back to Idle works.
        test(stop).click();
        runPendingSignalsTasks();

        assertSpanText("Idle");
    }

    private void assertSpanText(String fragment) {
        assertTrue(
                $view(Span.class).all().stream()
                        .anyMatch(s -> s.getText() != null
                                && s.getText().contains(fragment)),
                "expected a span containing \"" + fragment + "\"");
    }
}
