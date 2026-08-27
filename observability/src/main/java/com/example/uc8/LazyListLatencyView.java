package com.example.uc8;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.example.views.MainLayout;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeTable;
import com.vaadin.flow.component.html.NativeTableBody;
import com.vaadin.flow.component.html.NativeTableCell;
import com.vaadin.flow.component.html.NativeTableHeaderCell;
import com.vaadin.flow.component.html.NativeTableRow;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC8 — why a lazy list is slow.
 * <p>
 * A {@link ComboBox} backed by a deliberately slow lazy data provider. Typing a
 * filter makes the component ask its provider twice: once for the count of
 * matches, once for the visible page. Both queries run <em>after</em> the RPC
 * invocation that triggered them has already returned, while the response is
 * being built, which is why the interaction meters cannot see them:
 * {@code vaadin.rpc.duration} for the keystroke measures microseconds of
 * bookkeeping no matter how slow the backend is.
 * <p>
 * The table below reads the data query meters Observability Kit records for
 * those queries, so "the combo box is slow" becomes attributable: how much of
 * it is the count, how much the fetch, and how many items were asked for versus
 * how many came back.
 * <p>
 * <b>Scoping.</b> The kit instruments every {@code DataCommunicator}, in-memory
 * ones included, and the two timers carry no route tag, so an unscoped read
 * would average this combo box together with every grid in the application. The
 * rows therefore read the meters by their tags: the timers split by
 * {@code filtered}, which separates a combo box loading matches for typed text
 * ({@code filtered=true}) from any component loading a whole data set
 * ({@code filtered=false}); the two row summaries are scoped to
 * {@code route=uc8}. And the table itself is a plain HTML table rather than a
 * {@code Grid}, so refreshing it does not issue data provider queries on this
 * very route and perturb the numbers it displays.
 */
@Route(value = LazyListLatencyView.ROUTE, layout = MainLayout.class)
@PageTitle("UC8 — Lazy list latency")
@Menu(order = 8, title = "UC8 — Lazy list latency")
public class LazyListLatencyView extends VerticalLayout {

    /**
     * The route template, which is also the value of the {@code route} tag the
     * kit puts on the row summaries recorded from this view.
     */
    static final String ROUTE = "uc8";

    private static final String COUNT_DURATION = "vaadin.data.count.duration";
    private static final String FETCH_DURATION = "vaadin.data.fetch.duration";
    private static final String FETCH_REQUESTED = "vaadin.data.fetch.requested";
    private static final String FETCH_ROWS = "vaadin.data.fetch.rows";

    private static final String TAG_FILTERED = "filtered";
    private static final String TAG_ROUTE = "route";

    /** Enough items that the combo box has to page rather than load all. */
    private static final List<String> CATALOG = IntStream.range(0, 5_000)
            .mapToObj(i -> "Item %04d".formatted(i)).toList();

    private final transient MeterRegistry registry;
    private final NativeTable meters = new NativeTable();
    private final NativeTableBody meterRows = meters.getBody();
    private final IntegerField delay = new IntegerField(
            "Backend delay per query (ms)");

    public LazyListLatencyView(MeterRegistry registry) {
        this.registry = registry;

        add(new H1("UC8 — Why is this lazy list slow?"));
        add(new Paragraph(
                "Type in the combo box. Each keystroke makes it ask the data "
                        + "provider for a count of matches and for one page of "
                        + "items. Both run after the RPC invocation that "
                        + "triggered them has returned, so vaadin.rpc.duration "
                        + "for the keystroke stays in the microseconds however "
                        + "slow the backend is. The data query meters below are "
                        + "what actually shows the cost."));

        delay.setValue(200);
        // Wide enough for the label to stay on one line: "per query" is the
        // point of the view, since one keystroke costs a count and a fetch.
        delay.setWidth("16em");
        delay.setStepButtonsVisible(true);
        delay.setMin(0);
        delay.setMax(2_000);

        ComboBox<String> comboBox = new ComboBox<>("Search the catalog");
        comboBox.setPageSize(50);
        comboBox.setItems(this::fetch, this::count);
        comboBox.setWidth("20em");

        Button refresh = new Button("Refresh meters", event -> refreshMeters());

        add(new HorizontalLayout(comboBox, delay));
        add(refresh);
        add(buildMeterTable());
        add(new Paragraph(
                "The timers carry no route tag, so they are read by their "
                        + "filtered tag instead: filtered=true is this combo "
                        + "box searching for typed text, filtered=false is any "
                        + "component in the application loading a whole data "
                        + "set. The two summaries are scoped to route=" + ROUTE
                        + ". The table is a plain HTML table rather than a "
                        + "Grid so that refreshing it does not itself issue "
                        + "data provider queries on this route."));
        refreshMeters();
    }

    // ---------- the deliberately slow provider ----------

    private Stream<String> fetch(Query<String, String> query) {
        sleep();
        return matches(query.getFilter().orElse("")).stream()
                .skip(query.getOffset()).limit(query.getLimit());
    }

    private int count(Query<String, String> query) {
        sleep();
        return matches(query.getFilter().orElse("")).size();
    }

    private static List<String> matches(String filter) {
        String needle = filter.toLowerCase(Locale.ROOT);
        return CATALOG.stream()
                .filter(item -> item.toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    private void sleep() {
        Integer ms = delay.getValue();
        if (ms == null || ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ---------- reading the meters ----------

    private NativeTable buildMeterTable() {
        NativeTableRow header = meters.getHead().addRow();
        for (String title : List.of("Meter", "Tags", "Queries", "Value",
                "What it tells you")) {
            header.add(new NativeTableHeaderCell(title));
        }
        meters.setWidthFull();
        return meters;
    }

    private void refreshMeters() {
        meterRows.removeAllRows();
        List.of(timerRow(COUNT_DURATION, true,
                "How long counting the matches for the typed text takes"),
                timerRow(COUNT_DURATION, false,
                        "Counts without a filter: components loading a whole "
                                + "data set, on any route"),
                timerRow(FETCH_DURATION, true,
                        "How long loading one page of matches takes"),
                timerRow(FETCH_DURATION, false,
                        "Fetches without a filter: components loading a "
                                + "whole data set, on any route"),
                summaryRow(FETCH_REQUESTED,
                        "Items the combo box asked for, on this route only"),
                summaryRow(FETCH_ROWS,
                        "Items the provider returned; a persistent gap "
                                + "against the row above means over-fetching "
                                + "or short pages"))
                .forEach(row -> meterRows.add(row.render()));
    }

    private Row timerRow(String name, boolean filtered, String reads) {
        long count = 0;
        double totalMs = 0;
        double maxMs = 0;
        for (Timer timer : registry.find(name)
                .tag(TAG_FILTERED, Boolean.toString(filtered)).timers()) {
            count += timer.count();
            totalMs += timer.totalTime(TimeUnit.MILLISECONDS);
            maxMs = Math.max(maxMs, timer.max(TimeUnit.MILLISECONDS));
        }
        String value = count == 0 ? "—"
                : "mean %.0f ms, max %.0f ms".formatted(totalMs / count, maxMs);
        return new Row(name, TAG_FILTERED + "=" + filtered, count, value,
                reads);
    }

    private Row summaryRow(String name, String reads) {
        long count = 0;
        double total = 0;
        for (DistributionSummary summary : registry.find(name)
                .tag(TAG_ROUTE, ROUTE).summaries()) {
            count += summary.count();
            total += summary.totalAmount();
        }
        String value = count == 0 ? "—"
                : "%.0f items over %d fetches".formatted(total, count);
        return new Row(name, TAG_ROUTE + "=" + ROUTE, count, value, reads);
    }

    /** One row of the meter table. */
    private record Row(String meter, String tags, long count, String value,
            String reads) {

        NativeTableRow render() {
            return new NativeTableRow(new NativeTableCell(meter),
                    new NativeTableCell(tags),
                    new NativeTableCell(Long.toString(count)),
                    new NativeTableCell(value), new NativeTableCell(reads));
        }
    }
}
