package com.example.uc2;

import java.time.Duration;
import java.util.Collection;

import com.example.home.HomeView;
import com.example.uc2.ApplicationHealthView.Channel;
import com.example.uc2.ApplicationHealthView.Stat;
import com.example.uc2.ProductCatalogService.CatalogLoad;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.PollEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = { ApplicationHealthView.class, HomeView.class })
class ApplicationHealthViewTest extends SpringBrowserlessTest {

    @Autowired
    MeterRegistry registry;

    @Autowired
    ProductCatalogService catalog;

    @Test
    void rendersHeadingStatusAndReadout() {
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        assertTrue(
                findInView(H1.class).all().stream()
                        .anyMatch(h -> h.getText().startsWith("UC2")),
                "UC2 heading should render");

        // The signal-bound status badge is populated by the effect. Before any
        // poll tick the cadence is unknown, so the badge must say so and carry
        // neither the success nor the error theme — it never claims a
        // connection state the server cannot see.
        Span status = findInView(Span.class).id("connection-status");
        assertTrue(status.getText().startsWith("Waiting for the first"),
                "badge should admit the cadence is unknown before the first poll: "
                        + status.getText());
        assertFalse(status.getElement().getThemeList().contains("success"),
                "badge should not be green before any update has arrived");
        assertFalse(status.getElement().getThemeList().contains("error"),
                "badge should not be red before any update has arrived");

        // The readout always has a row per app signal, even with no traffic.
        Grid<Stat> grid = findInView(Grid.class).single();
        assertTrue(test(grid).size() >= 6,
                "readout should list the baseline health signals");
        assertTrue(
                grid.getListDataView().getItems().anyMatch(
                        s -> "Active users (sessions)".equals(s.signal())),
                "active-users signal should be in the readout");
        assertTrue(
                grid.getListDataView().getItems()
                        .anyMatch(s -> s.signal().startsWith("Heap used")),
                "memory signal should be in the readout");

        assertEquals(1,
                findInView(Button.class).withText("Flush client metrics now")
                        .all().size(),
                "the flush-client-metrics button should render");
    }

    @Test
    void clickingFlushButtonIsSafe() {
        // The flush button asks the browser collector to POST its buffer via
        // executeJs; there is no real browser in a browserless test, so the
        // click must simply be a no-op that leaves the view intact rather than
        // throwing.
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        test(findInView(Button.class).withText("Flush client metrics now")
                .single()).click();
        runPendingSignalsTasks();

        assertTrue(
                findInView(Span.class).id("connection-status").getText()
                        .contains("refreshes this session"),
                "view should still render after flushing client metrics");
    }

    @Test
    void pollingDrivesLiveRefreshesThroughTheSignal() {
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        Span status = findInView(Span.class).id("connection-status");
        String first = status.getText();

        // A poll tick recomputes the snapshot and writes the signal; the
        // refresh counter in the connection line must advance, proving the
        // poll → recompute → signal → effect wiring repaints the badge. The
        // view registers its recompute as a UI poll listener in onAttach, so
        // firing a PollEvent drives exactly that path.
        UI ui = UI.getCurrent();
        ComponentUtil.fireEvent(ui, new PollEvent(ui, false));
        runPendingSignalsTasks();

        String second = status.getText();
        assertTrue(!first.equals(second),
                "a poll tick should refresh the live status badge");
        // A tick that arrives on schedule is the one case where the badge may
        // go green, and the effect must apply that theme.
        assertTrue(second.startsWith("Live"),
                "an on-time tick should report a live channel: " + second);
        assertTrue(status.getElement().getThemeList().contains("success"),
                "an on-time tick should turn the badge green");
        assertFalse(status.getElement().getThemeList().contains("error"),
                "an on-time tick should not flag the channel as interrupted");
    }

    @Test
    void channelStateIsDerivedFromTheRefreshCadence() {
        // The badge's three states come from this one pure function, so all of
        // them are covered here rather than left as unreachable styling: no
        // cadence yet, a tick within the poll window, and a late tick (the tab
        // was suspended or the channel dropped and came back).
        int poll = 2000;
        assertEquals(Channel.UNKNOWN,
                ApplicationHealthView.channel(0, 10_000, poll),
                "before the first tick the channel state is unknown");
        assertEquals(Channel.LIVE,
                ApplicationHealthView.channel(10_000, 12_000, poll),
                "a tick within the poll window means updates are flowing");
        assertEquals(Channel.LIVE,
                ApplicationHealthView.channel(10_000, 14_000, poll),
                "a tick at twice the poll interval is still on time");
        assertEquals(Channel.RESUMED,
                ApplicationHealthView.channel(10_000, 25_000, poll),
                "a late tick means updates had stopped arriving");
    }

    @Test
    void pollingStopsAfterLeavingTheView() {
        navigate(ApplicationHealthView.class);
        assertTrue(UI.getCurrent().getPollInterval() > 0,
                "view should enable polling while attached");

        navigate(HomeView.class);
        assertEquals(-1, UI.getCurrent().getPollInterval(),
                "polling should be disabled again once the view is detached");
    }

    @Test
    void readoutReflectsTheSharedRegistryAcrossSessions() {
        // Session 1: record an interaction into the application-scoped registry
        // the view reads from (the same singleton bean injected into the view).
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();
        registry.timer("vaadin.request.duration", "outcome", "success")
                .record(Duration.ofMillis(42));

        // Session 2: a fresh Vaadin environment, same Spring context (same
        // singleton registry). The request timing recorded by session 1 must
        // still be visible — confirming the readout reflects shared, app-wide
        // state rather than per-view bookkeeping.
        cleanVaadinEnvironment();
        initVaadinEnvironment();
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        Grid<Stat> grid = findInView(Grid.class).single();
        Stat requests = grid.getListDataView().getItems()
                .filter(s -> "Server request handling".equals(s.signal()))
                .findFirst().orElse(null);
        assertTrue(requests != null, "request-handling row should be present");
        assertTrue(!"no samples yet".equals(requests.value()),
                "an interaction recorded in the first session should be visible in the second");
    }

    @Test
    void loadingCatalogUpdatesTheReadoutThroughTheSignal() {
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        Div result = findInView(Div.class).id("catalog-result");
        assertTrue(result.getText().contains("not loaded yet"),
                "result should start in the not-loaded state");

        // Clicking the button runs the catalog load and writes the lastLoad
        // signal; the effect must repaint the result text. This wiring holds
        // regardless of whether the kit's database meter is on the classpath.
        test(findInView(Button.class)
                .withText("Load product catalog (forces join-table fetch)")
                .single()).click();
        runPendingSignalsTasks();

        String text = findInView(Div.class).id("catalog-result").getText();
        assertTrue(text.contains("Loaded") && text.contains("products"),
                "result should report the loaded catalog: " + text);
    }

    @Test
    void catalogLoadIsSeededAndReadable() {
        // Service-level smoke test of the JPA layer + seeder, independent of
        // any
        // observability wiring: the catalog must come back non-empty so the
        // join-table fetch demo has something to fan out over.
        CatalogLoad load = catalog.loadCatalog();
        assertTrue(load.products() > 0, "the catalog should be seeded");
        assertTrue(load.categories() >= load.products(),
                "every product should carry at least one category link");
    }

    @Test
    void theKitFetchMeterRevealsTheNPlusOne() {
        navigate(ApplicationHealthView.class);
        runPendingSignalsTasks();

        test(findInView(Button.class)
                .withText("Load product catalog (forces join-table fetch)")
                .single()).click();
        runPendingSignalsTasks();

        // The module pins a kit build with the database feature and sets
        // vaadin.observability.database=true, so the meter must be there. This
        // is asserted rather than assumed on purpose: if the kit ever stops
        // recording vaadin.db.fetch.rows, UC2's headline claim is gone, and a
        // skipped test would hide that behind a green CI run.
        Collection<DistributionSummary> summaries = registry
                .find("vaadin.db.fetch.rows").summaries();
        assertFalse(summaries.isEmpty(),
                "the Observability Kit database feature should record "
                        + "vaadin.db.fetch.rows (vaadin.observability.database"
                        + "=true)");

        // With the meter present, the readout must flag the unbatched eager
        // join fetch as an N+1 (more result-set fetches than products).
        String text = findInView(Div.class).id("catalog-result").getText();
        assertTrue(text.contains("vaadin.db.fetch.rows"),
                "readout should attribute the fetches to the kit meter: "
                        + text);
        assertTrue(text.contains("N+1"),
                "the unbatched eager join fetch should be flagged as N+1: "
                        + text);

        // The same meter also feeds a live row in the health readout.
        Grid<Stat> grid = findInView(Grid.class).single();
        assertTrue(
                grid.getListDataView().getItems().anyMatch(
                        s -> "vaadin.db.fetch.rows".equals(s.source())),
                "the kit DB fetch meter should appear as a live readout row");
    }
}
