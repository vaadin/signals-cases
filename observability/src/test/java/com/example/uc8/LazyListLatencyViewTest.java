package com.example.uc8;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.home.HomeView;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeTable;
import com.vaadin.flow.component.html.NativeTableRow;
import com.vaadin.flow.component.textfield.IntegerField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = { LazyListLatencyView.class, HomeView.class })
class LazyListLatencyViewTest extends SpringBrowserlessTest {

    private static final int METER = 0;
    private static final int TAGS = 1;
    private static final int QUERIES = 2;
    private static final int VALUE = 3;

    @Autowired
    MeterRegistry registry;

    @Test
    void rendersTheComboBoxAndTheMeterTable() {
        navigate(LazyListLatencyView.class);

        assertEquals("UC8 — Why is this lazy list slow?",
                findInView(H1.class).first().getText());
        assertNotNull(findInView(ComboBox.class).first(),
                "the lazy combo box is the subject of the use case");
        assertNotNull(findInView(IntegerField.class).first(),
                "the backend delay has to be adjustable to make the cost "
                        + "visible");
        assertNotNull(
                findInView(Button.class).withText("Refresh meters").first());
        assertNotNull(findInView(NativeTable.class).first());
    }

    @Test
    void theMeterTableIsNotAGrid() {
        // The kit instruments every DataCommunicator, in-memory ones included,
        // and tags the row summaries by route. A Grid showing the meters
        // would record a count and a fetch on route uc8 at every refresh,
        // perturbing the very numbers it displays.
        navigate(LazyListLatencyView.class);

        assertTrue(findInView(Grid.class).all().isEmpty(),
                "reading the meters must not issue data queries on this route");
    }

    @Test
    void theMeterTableNamesTheFourDataQueryMetersByTheirTags() {
        navigate(LazyListLatencyView.class);

        assertEquals(List.of("vaadin.data.count.duration",
                "vaadin.data.count.duration", "vaadin.data.fetch.duration",
                "vaadin.data.fetch.duration", "vaadin.data.fetch.requested",
                "vaadin.data.fetch.rows"), column(METER),
                "the view exists to show these four meters");
        assertEquals(
                List.of("filtered=true", "filtered=false", "filtered=true",
                        "filtered=false", "route=uc8", "route=uc8"),
                column(TAGS),
                "the timers have no route tag and are split by filtered "
                        + "instead; the summaries are scoped to this route");
    }

    @Test
    void theProviderIsSlowEnoughToBeWorthMeasuring() {
        navigate(LazyListLatencyView.class);

        IntegerField delay = findInView(IntegerField.class).first();
        assertTrue(delay.getValue() > 0,
                "a zero default delay would make the use case show nothing");
    }

    @Test
    void anUnmeasuredMeterReadsAsADashRatherThanZero() {
        // The registry is shared across the tests in this context, so this
        // asserts the formatting rule rather than that nothing has run yet.
        navigate(LazyListLatencyView.class);

        List<NativeTableRow> rows = rows();
        assertFalse(rows.isEmpty());
        assertTrue(
                rows.stream().filter(row -> "0".equals(cell(row, QUERIES)))
                        .allMatch(row -> "—".equals(cell(row, VALUE))),
                "a meter with no recordings shows a dash, not 0 ms");
    }

    @Test
    void refreshReadsOnlyTheFilteredTimersAndThisRoutesSummaries() {
        navigate(LazyListLatencyView.class);

        // What the kit would record for this combo box searching typed text.
        timer("vaadin.data.count.duration", "true").record(120,
                TimeUnit.MILLISECONDS);
        timer("vaadin.data.fetch.duration", "true").record(80,
                TimeUnit.MILLISECONDS);
        summary("vaadin.data.fetch.requested", "uc8").record(50);
        summary("vaadin.data.fetch.rows", "uc8").record(50);
        // What an in-memory grid on some other view would record: no filter,
        // another route. None of it may leak into the combo box's rows.
        timer("vaadin.data.count.duration", "false").record(9_000,
                TimeUnit.MILLISECONDS);
        timer("vaadin.data.fetch.duration", "false").record(9_000,
                TimeUnit.MILLISECONDS);
        summary("vaadin.data.fetch.requested", "uc2").record(999);
        summary("vaadin.data.fetch.rows", "uc2").record(999);

        test(findInView(Button.class).withText("Refresh meters").single())
                .click();

        assertEquals("mean 120 ms, max 120 ms",
                value("vaadin.data.count.duration", "filtered=true"));
        assertEquals("mean 80 ms, max 80 ms",
                value("vaadin.data.fetch.duration", "filtered=true"));
        assertEquals("50 items over 1 fetches",
                value("vaadin.data.fetch.requested", "route=uc8"));
        assertEquals("50 items over 1 fetches",
                value("vaadin.data.fetch.rows", "route=uc8"));
        // The unfiltered rows are still shown, so that the app-wide cost is
        // not hidden, but kept apart from the combo box's own.
        assertEquals("mean 9000 ms, max 9000 ms",
                value("vaadin.data.count.duration", "filtered=false"));
        assertEquals("mean 9000 ms, max 9000 ms",
                value("vaadin.data.fetch.duration", "filtered=false"));
    }

    private Timer timer(String name, String filtered) {
        return Timer.builder(name).tag("outcome", "success")
                .tag("filtered", filtered).register(registry);
    }

    private DistributionSummary summary(String name, String route) {
        return DistributionSummary.builder(name).tag("route", route)
                .register(registry);
    }

    private List<NativeTableRow> rows() {
        return findInView(NativeTable.class).single().getBody().getRows();
    }

    private List<String> column(int index) {
        return rows().stream().map(row -> cell(row, index)).toList();
    }

    private String value(String meter, String tags) {
        return rows().stream()
                .filter(row -> meter.equals(cell(row, METER))
                        && tags.equals(cell(row, TAGS)))
                .map(row -> cell(row, VALUE)).findFirst()
                .orElseThrow(() -> new AssertionError(
                        "no row for " + meter + " " + tags));
    }

    private static String cell(NativeTableRow row, int index) {
        return row.getDataCell(index).orElseThrow().getText();
    }
}
