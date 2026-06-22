package com.example.uc1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.home.HomeView;
import com.example.uc1.InteractionLatencyView.Row;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = { InteractionLatencyView.class, HomeView.class })
class InteractionLatencyViewTest extends SpringBrowserlessTest {

    @Test
    void rendersHeadingActionsAndReadout() {
        navigate(InteractionLatencyView.class);
        runPendingSignalsTasks();

        assertTrue(findInView(H1.class).all().stream()
                .anyMatch(h -> h.getText().startsWith("UC1")),
                "UC1 heading should render");
        for (String label : new String[] { "instant", "light (150 ms)",
                "heavy (400 ms)" }) {
            assertEquals(1,
                    findInView(Button.class).withText(label).all().size(),
                    "action button should render: " + label);
        }
        // The fixed meter rows always render even with no samples yet: server
        // request, server RPC, client navigation, client LCP, client FCP.
        Grid<Row> grid = findInView(Grid.class).single();
        assertTrue(test(grid).size() >= 5,
                "readout should have at least the five baseline meter rows");
    }

    @Test
    void clickingActionRecordsPerActionTimerInTheReadout() {
        navigate(InteractionLatencyView.class);
        runPendingSignalsTasks();

        test(findInView(Button.class).withText("instant").single()).click();
        runPendingSignalsTasks();

        Grid<Row> grid = findInView(Grid.class).single();
        Row instant = grid.getListDataView().getItems()
                .filter(r -> "action: instant".equals(r.segment())).findFirst()
                .orElse(null);
        assertTrue(instant != null,
                "a per-action row for the clicked action should appear");
        assertTrue(instant.count() >= 1,
                "the per-action timer should have recorded at least one sample");
    }

    @Test
    void pollingStopsAfterLeavingTheView() {
        navigate(InteractionLatencyView.class);
        assertTrue(UI.getCurrent().getPollInterval() > 0,
                "view should enable polling while attached");

        navigate(HomeView.class);
        assertEquals(-1, UI.getCurrent().getPollInterval(),
                "polling should be disabled again once the view is detached");
    }

    @Test
    void metricsAreSharedAcrossSessions() {
        // Session 1 triggers an action; it records into the application-scoped
        // MeterRegistry the view reads from.
        navigate(InteractionLatencyView.class);
        runPendingSignalsTasks();
        test(findInView(Button.class).withText("heavy (400 ms)").single())
                .click();
        runPendingSignalsTasks();

        // Session 2: a fresh Vaadin environment, same Spring context (same
        // singleton registry). The action recorded by session 1 must still be
        // visible — confirming the readout reflects shared, app-wide state.
        cleanVaadinEnvironment();
        initVaadinEnvironment();
        navigate(InteractionLatencyView.class);
        runPendingSignalsTasks();

        Grid<Row> grid = findInView(Grid.class).single();
        assertTrue(
                grid.getListDataView().getItems().anyMatch(
                        r -> "action: heavy (400 ms)".equals(r.segment())),
                "action recorded in the first session should be visible in the second");
    }
}
