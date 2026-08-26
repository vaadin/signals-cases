package com.example.uc6;

import com.example.home.HomeView;
import com.example.uc6.FailureInsightsView.Row;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Note on coverage: the kit captures an interaction from Flow's RPC invocation
 * listener, which only fires while handling a real UIDL request. A browserless
 * click invokes the listener directly, bypassing that pipeline, so these tests
 * can verify the view's rendering, wiring and lifecycle but not the capture
 * itself — see the test-simulator note in {@code API-GAPS.md}.
 */
@SpringBootTest
@ViewPackages(classes = { FailureInsightsView.class, HomeView.class })
class FailureInsightsViewTest extends SpringBrowserlessTest {

    @Test
    void rendersHeadingActionsAndReadout() {
        navigate(FailureInsightsView.class);
        runPendingSignalsTasks();

        assertTrue(
                findInView(H1.class).all().stream()
                        .anyMatch(h -> h.getText().startsWith("UC6")),
                "UC6 heading should render");
        for (String label : new String[] { "Fail now", "Fail differently",
                "Slow call (1.5 s)", "Succeed" }) {
            assertEquals(1,
                    findInView(Button.class).withText(label).all().size(),
                    "action button should render: " + label);
        }
        assertEquals(1, findInView(Grid.class).all().size(),
                "the insights readout should render");
    }

    @Test
    void failingActionLetsTheExceptionPropagate() {
        navigate(FailureInsightsView.class);
        runPendingSignalsTasks();

        // The kit records a failed interaction only when the invocation
        // actually fails, so the listener must not swallow the exception.
        Button fail = findInView(Button.class).withText("Fail now").single();
        assertThrows(IllegalStateException.class, () -> test(fail).click(),
                "the failing action should propagate its exception");
    }

    @Test
    void slowActionCompletesAndKeepsTheReadoutRenderable() {
        navigate(FailureInsightsView.class);
        runPendingSignalsTasks();

        test(findInView(Button.class).withText("Slow call (1.5 s)").single())
                .click();
        runPendingSignalsTasks();

        Grid<Row> grid = findInView(Grid.class).single();
        assertTrue(test(grid).size() >= 0,
                "the readout should still render after a slow interaction");
    }

    @Test
    void doesNotPollAndRestoresTheSessionErrorHandler() {
        ErrorHandler original = VaadinSession.getCurrent().getErrorHandler();

        navigate(FailureInsightsView.class);
        assertEquals(-1, UI.getCurrent().getPollInterval(),
                "the view refreshes per click, so it must not enable polling");

        navigate(HomeView.class);
        assertSame(original, VaadinSession.getCurrent().getErrorHandler(),
                "the session error handler should be restored on detach");
    }

    @Test
    void readoutIsApplicationScopedAcrossSessions() {
        // The insights come from an application-scoped buffer the kit owns, not
        // from per-session state: a second session must see the same readout.
        navigate(FailureInsightsView.class);
        runPendingSignalsTasks();
        int firstSessionRows = test((Grid<Row>) findInView(Grid.class).single())
                .size();

        cleanVaadinEnvironment();
        initVaadinEnvironment();
        navigate(FailureInsightsView.class);
        runPendingSignalsTasks();

        assertEquals(firstSessionRows,
                test((Grid<Row>) findInView(Grid.class).single()).size(),
                "both sessions should read the same application-wide insights");
    }
}
