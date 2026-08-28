package com.example.uc5;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.example.home.HomeView;
import com.example.uc5.ClientErrorLog.BrowserError;
import com.example.uc5.ConnectionInsightsView.Stat;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.observability.micrometer.MeterNames;
import com.vaadin.observability.micrometer.ObservabilitySettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The connection half of UC5 is the kit's now, so these tests assert that the
 * view reads the kit's meters under their published names and does the one
 * piece of arithmetic left to it — summing the per-state downtime back into a
 * whole outage. What a browserless test cannot reach is the collector itself:
 * its subscription to {@code window.Vaadin.connectionState} and its buffering
 * across an outage need a browser, and the kit covers them in its own
 * {@code ClientProblemsIT}.
 * <p>
 * The error-detail reporter is the remaining application-side piece, and its
 * server half is reached the same way a browser reaches it, by calling the
 * {@code @ClientCallable} with the payload a browser would send.
 */
@SpringBootTest
@ViewPackages(classes = { ConnectionInsightsView.class, HomeView.class })
// A fresh context per test, so the shared MeterRegistry starts empty and a
// count can be asserted exactly rather than as a delta. The error log is a
// singleton bean and is reset by the same reload.
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ConnectionInsightsViewTest extends SpringBrowserlessTest {

    @Autowired
    MeterRegistry registry;

    @Autowired
    ClientErrorLog log;

    @Autowired
    ObservabilitySettings settings;

    @Test
    void moduleKeepsTheKitsClientCollectionOn() {
        // Everything the view reads about the connection arrives through the
        // in-browser collector, which this switch controls. If the module ever
        // turns it off, UC5 has nothing to show and should fail here rather
        // than render a page of em dashes.
        assertTrue(settings.isClient(),
                "vaadin.observability.client should be on");
    }

    @Test
    void rendersHeadingActionsAndBothReadouts() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        assertTrue(
                findInView(H1.class).all().stream()
                        .anyMatch(h -> h.getText().startsWith("UC5")),
                "UC5 heading should render");
        for (String id : new String[] { "simulate-loss",
                "simulate-reconnecting", "throw-error", "reject-promise",
                "refresh", "clear-log" }) {
            assertNotNull(findInView(Button.class).id(id),
                    "action should render: " + id);
        }
        assertTrue(errorRows().isEmpty(), "the detail table starts empty");
        assertTrue(
                findInView(Span.class).id("connection-status").getText()
                        .startsWith("Nothing recorded"),
                "the badge should say nothing has been recorded yet");
    }

    @Test
    void readsTheKitsMetersUnderTheirPublishedNames() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        // The point of the update: none of these are recorded here any more.
        for (String meter : List.of(
                MeterNames.CLIENT_CONNECTION + " {connection-lost}",
                MeterNames.CLIENT_CONNECTION + " {reconnecting}",
                MeterNames.CLIENT_CONNECTION + " {connected}",
                MeterNames.CLIENT_CONNECTION_DOWNTIME + " {connection-lost}",
                MeterNames.CLIENT_CONNECTION_DOWNTIME + " {reconnecting}",
                MeterNames.CLIENT_ERRORS + " {uncaught}",
                MeterNames.CLIENT_ERRORS + " {promise}",
                MeterNames.RESYNC + " {resend}",
                MeterNames.RESYNC + " {resync}")) {
            assertNotNull(meterRow(meter), "readout row for " + meter);
        }
    }

    @Test
    void anUnrecordedMeterReadsAsADashRatherThanZero() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        // "Nothing has happened" and "nothing is watching" are different
        // answers, and a browserless run has no collector at all.
        assertEquals("—",
                meterRow(MeterNames.CLIENT_CONNECTION + " {connection-lost}")
                        .value());
        assertEquals("—", meterRow(
                MeterNames.CLIENT_CONNECTION_DOWNTIME + " {reconnecting}")
                .value());
    }

    @Test
    void downtimeIsReadPerStateAndSummedIntoAWholeOutage() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        // What the collector reports for one outage that Flow retried through
        // before giving up: time under each state, tagged separately.
        downtime(MeterNames.STATE_RECONNECTING).record(Duration.ofMillis(1500));
        downtime(MeterNames.STATE_CONNECTION_LOST)
                .record(Duration.ofMillis(3000));
        transitions(MeterNames.STATE_CONNECTION_LOST).increment();
        refresh();

        assertTrue(
                meterRow(MeterNames.CLIENT_CONNECTION_DOWNTIME
                        + " {connection-lost}").value()
                        .startsWith("1 period(s)"),
                "the given-up-on time should be read from its own tag");
        assertTrue(
                meterRow(MeterNames.CLIENT_CONNECTION_DOWNTIME
                        + " {reconnecting}").value().startsWith("1 period(s)"),
                "so should the retrying time");
        // The kit splits the timer because the two states mean different
        // things; adding them back is the application's job, and is the number
        // an SLO would use.
        assertEquals("%.1f s across both states".formatted(4.5),
                meterRow(MeterNames.CLIENT_CONNECTION_DOWNTIME
                        + " (sum of tags)").value(),
                "the whole outage is the sum, not either tag alone");
    }

    @Test
    void theBadgeSummarisesWhatTheKitRecorded() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        transitions(MeterNames.STATE_CONNECTION_LOST).increment();
        downtime(MeterNames.STATE_CONNECTION_LOST)
                .record(Duration.ofMillis(3000));
        refresh();

        Span status = findInView(Span.class).id("connection-status");
        assertTrue(status.getText().contains("1 loss(es)"),
                "the badge should count the losses: " + status.getText());
        assertTrue(status.getElement().getThemeList().contains("error"),
                "a recorded outage is not good news");
    }

    @Test
    void errorDetailIsKeptWhereTheKitKeepsOnlyACount() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        // The kit's collector counts the error and drops everything else.
        Counter.builder(MeterNames.CLIENT_ERRORS)
                .tag(MeterNames.TAG_KIND, "uncaught").register(registry)
                .increment();
        // The view's own listener keeps what it dropped.
        report("uncaught", "Cannot read properties of undefined",
                "at renderChart (chart.js:42:11)");

        assertEquals("1",
                meterRow(MeterNames.CLIENT_ERRORS + " {uncaught}").value(),
                "the count is the kit's");
        BrowserError detail = errorRows().getFirst();
        assertEquals("Cannot read properties of undefined", detail.message(),
                "the message is not on any meter and has to be kept here");
        assertEquals("at renderChart (chart.js:42:11)", detail.where(),
                "so is the frame it came from");
    }

    @Test
    void doesNotPoll() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        // A poll is a UIDL request, so a polling view probes the connection on
        // every tick and ends an outage early — it would report shorter
        // downtime than a passive tab on the same network. The kit's README
        // says the same. The error report repaints this view instead.
        assertTrue(UI.getCurrent().getPollInterval() < 0,
                "polling would shorten the very outages this view reports");
    }

    @Test
    void errorsReportedByAnotherBrowserAreVisibleHere() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();
        report("uncaught", "boom", "at x (a.js:1:1)");

        // A second browser, i.e. a second session: the tab that failed may
        // never come back, so the log has to be application-wide for anyone to
        // see what happened to it.
        cleanVaadinEnvironment();
        initVaadinEnvironment();
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();

        assertEquals(1, errorRows().size(),
                "the other browser's error should be readable from this one");
        assertEquals("boom", errorRows().getFirst().message());
    }

    @Test
    void clearingTheLogEmptiesTheDetailTable() {
        navigate(ConnectionInsightsView.class);
        runPendingSignalsTasks();
        report("promise", "fetching /api/quotes failed", "");
        assertEquals(1, errorRows().size());

        test(findInView(Button.class).id("clear-log")).click();
        runPendingSignalsTasks();

        assertTrue(errorRows().isEmpty(),
                "clearing should empty the signal-bound detail table");
    }

    @Test
    void theLogIsBoundedSoAMisbehavingClientCannotGrowTheHeap() {
        for (int i = 0; i < ClientErrorLog.CAPACITY + 20; i++) {
            log.add(new BrowserError(Instant.now(), "tab 0/0000", "uncaught",
                    "error " + i, "a.js:1"));
        }

        assertEquals(ClientErrorLog.CAPACITY, log.recent().size());
        assertEquals("error " + (ClientErrorLog.CAPACITY + 19),
                log.recent().getFirst().message(), "newest first");
    }

    // ---------- driving the view ----------

    /** Reports one error exactly as the browser's listener would. */
    private void report(String kind, String message, String frame) {
        ClientErrorReport error = new ClientErrorReport();
        error.setKind(kind);
        error.setMessage(message);
        error.setSource("/uc5:0");
        error.setFrame(frame);
        findInView(ClientErrorReporter.class).single()
                .reportErrors(List.of(error));
        runPendingSignalsTasks();
    }

    private void refresh() {
        test(findInView(Button.class).id("refresh")).click();
        runPendingSignalsTasks();
    }

    /** What the collector records for one connection-state transition. */
    private Counter transitions(String state) {
        return Counter.builder(MeterNames.CLIENT_CONNECTION)
                .tag(MeterNames.TAG_STATE, state).register(registry);
    }

    /** What it records for the time spent in one unreachable state. */
    private Timer downtime(String state) {
        return Timer.builder(MeterNames.CLIENT_CONNECTION_DOWNTIME)
                .tag(MeterNames.TAG_STATE, state).register(registry);
    }

    // ---------- reading the view back ----------

    @SuppressWarnings("unchecked")
    private List<BrowserError> errorRows() {
        Grid<BrowserError> grid = findInView(Grid.class).id("error-detail");
        return grid.getListDataView().getItems().toList();
    }

    @SuppressWarnings("unchecked")
    private Stat meterRow(String meter) {
        Grid<Stat> grid = findInView(Grid.class).id("problem-meters");
        return grid.getListDataView().getItems()
                .filter(stat -> meter.equals(stat.meter())).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no readout row for meter: " + meter));
    }
}
