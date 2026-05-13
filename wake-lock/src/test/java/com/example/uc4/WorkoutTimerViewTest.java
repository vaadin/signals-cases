package com.example.uc4;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.WakeLockTestSupport;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = WorkoutTimerView.class)
class WorkoutTimerViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithStartButton() {
        navigate(WorkoutTimerView.class);
        runPendingSignalsTasks();

        assertTrue($view(H1.class).all().stream()
                .anyMatch(h -> h.getText() != null
                        && h.getText().contains("Workout interval timer")),
                "view should render heading");
        assertTrue($view(Button.class).withText("Start").all().size() == 1,
                "Start button should render initially");
        assertTrue($view(Button.class).withText("Reset").all().size() == 1,
                "Reset button should render initially");
    }

    @Test
    void clickingStartFlipsButtonLabelToPause() {
        navigate(WorkoutTimerView.class);
        runPendingSignalsTasks();

        test($view(Button.class).withText("Start").single()).click();
        runPendingSignalsTasks();

        assertTrue($view(Button.class).withText("Pause").all().size() == 1,
                "Start should become Pause once running");
    }

    @Test
    void resetReturnsToInitialState() {
        navigate(WorkoutTimerView.class);
        runPendingSignalsTasks();

        test($view(Button.class).withText("Start").single()).click();
        runPendingSignalsTasks();
        test($view(Button.class).withText("Reset").single()).click();
        runPendingSignalsTasks();

        assertTrue($view(Button.class).withText("Start").all().size() == 1,
                "Reset should put the button back to Start");
        assertClockShows("00:30");
        assertPhaseShows("WORK");
    }

    @Test
    void badgeReflectsLockStateWhileRunning() {
        navigate(WorkoutTimerView.class);
        runPendingSignalsTasks();

        assertBadgeContains("Released");

        // Start the timer — the effect calls request() on the wake lock.
        test($view(Button.class).withText("Start").single()).click();
        runPendingSignalsTasks();

        // The browser confirms.
        WakeLockTestSupport.simulateAcquired();
        runPendingSignalsTasks();
        assertBadgeContains("Holding");

        // Pause — the effect calls release(); the browser confirms.
        test($view(Button.class).withText("Pause").single()).click();
        runPendingSignalsTasks();
        WakeLockTestSupport.simulateReleased();
        runPendingSignalsTasks();
        assertBadgeContains("Released");
    }

    private void assertBadgeContains(String fragment) {
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> s.getText() != null
                        && s.getText().contains(fragment)),
                "expected status badge to contain \"" + fragment + "\"");
    }

    private void assertClockShows(String text) {
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> text.equals(s.getText())),
                "expected clock to show \"" + text + "\"");
    }

    private void assertPhaseShows(String text) {
        assertTrue($view(Span.class).all().stream()
                .anyMatch(s -> text.equals(s.getText())),
                "expected phase label to show \"" + text + "\"");
    }
}
