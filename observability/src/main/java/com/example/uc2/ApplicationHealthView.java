package com.example.uc2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.uc2.ProductCatalogService.CatalogLoad;
import com.example.views.MainLayout;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.PushConfiguration;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC2 — Watch the app's health from inside the app.
 * <p>
 * A live readout of the application's <em>own</em> signals — active users,
 * memory, recent interaction timings and the current connection status —
 * without leaving the app for an external console (Actuator, Grafana, …). Every
 * value is read straight out of the application's {@link MeterRegistry}, the
 * same registry the Observability Kit binders publish into:
 * <ul>
 * <li>{@code vaadin.sessions.active} / {@code vaadin.ui.active} — gauges of how
 * many sessions and UIs (browser tabs) are currently attached,</li>
 * <li>{@code jvm.memory.used} / {@code jvm.memory.max} (heap) — the JVM gauges
 * Spring Boot's Micrometer support registers out of the box,</li>
 * <li>{@code vaadin.request.duration}, {@code vaadin.rpc.duration} — the
 * server-side interaction timers (request handling and RPC),</li>
 * <li>{@code vaadin.client.bootstrap.duration},
 * {@code vaadin.client.navigation.duration},
 * {@code vaadin.client.web_vitals.lcp/fcp} — what the browser perceived
 * (initial load, client-side navigation, Largest/First Contentful Paint),
 * POSTed back by the in-browser collector and recorded into this same
 * registry,</li>
 * <li>{@code vaadin.errors} / {@code vaadin.client.errors} — server and client
 * error counters.</li>
 * </ul>
 * <p>
 * Note there is deliberately no {@code vaadin.client.rpc.duration}: in
 * Observability Kit the per-request round-trip is measured server-side only
 * ({@code vaadin.rpc.duration}); the client collector emits load- and
 * paint-oriented meters, not per-RPC timings. The readout is bound to a
 * {@link ValueSignal}: each UI poll recomputes the snapshot and {@code set}s
 * the signal, and {@link Signal#effect}s repaint the status badge and grid.
 * Polling is what makes this "live" — the numbers move on their own, and
 * client-collected samples (which the browser only flushes every few seconds)
 * appear without any interaction.
 * <p>
 * Connection status is the one signal with no meter behind it (see
 * {@code API-GAPS.md} #5): the browser's {@code online}/{@code reconnecting}
 * state is never recorded server-side. So instead of claiming to know it, the
 * badge reports the only related thing the server can actually observe: the
 * <em>cadence of this UI's own poll requests</em>. Before the first tick
 * nothing is known and the badge stays neutral; while ticks arrive on schedule
 * it is green; and on a tick that arrives late — the tab was suspended, or the
 * channel dropped and reconnected — it turns red and names the gap. Every
 * state is therefore derived, not hardcoded, and the push mode and transport
 * come from the public {@link PushConfiguration}.
 * <p>
 * The view also carries a small database-health demo: a "load the product
 * catalog" button that triggers the classic N+1 join-table fetch (see
 * {@link Product}). The cost is read straight out of the kit's own database
 * meter — {@code vaadin.db.fetch.rows}, the per-result-set row summary the
 * Observability Kit records when {@code vaadin.observability.database=true} —
 * not from any hand-rolled JDBC bookkeeping. Without {@code @BatchSize} on the
 * eager {@code category} association, loading N products fires N+1 separate
 * result-set fetches (N of them single-row-per-product category fetches plus
 * the product query), so the meter's fetch count jumps to N+1 and the readout
 * calls that out. It is the same problem the bookstore-example avoids by
 * annotating {@code Product.category}; this view makes it observable from
 * inside the app, through the kit. When the running kit build does not include
 * the database feature the meter is simply absent and the readout says so.
 */
@Route(value = "uc2", layout = MainLayout.class)
@PageTitle("UC2 — Application health")
@Menu(order = 2, title = "UC2 — Application health")
public class ApplicationHealthView extends VerticalLayout {

    private static final String SESSIONS = "vaadin.sessions.active";
    private static final String UIS = "vaadin.ui.active";
    private static final String HEAP_USED = "jvm.memory.used";
    private static final String HEAP_MAX = "jvm.memory.max";
    private static final String REQUEST = "vaadin.request.duration";
    private static final String RPC = "vaadin.rpc.duration";
    private static final String CLIENT_BOOTSTRAP = "vaadin.client.bootstrap.duration";
    private static final String CLIENT_NAVIGATION = "vaadin.client.navigation.duration";
    private static final String CLIENT_LCP = "vaadin.client.web_vitals.lcp";
    private static final String CLIENT_FCP = "vaadin.client.web_vitals.fcp";
    private static final String ERRORS = "vaadin.errors";
    private static final String CLIENT_ERRORS = "vaadin.client.errors";
    // The Observability Kit's database meter: a DistributionSummary of the rows
    // read per JDBC result set, recorded when
    // vaadin.observability.database=true.
    private static final String DB_FETCH_ROWS = "vaadin.db.fetch.rows";
    private static final int POLL_MILLIS = 2000;

    /** One line of the health readout. */
    public record Stat(String signal, String value, String source) {
    }

    /**
     * How current the readout is, derived from the observed refresh cadence —
     * the closest thing to a connection state the server can see (there is no
     * connection-state meter; see {@code API-GAPS.md} #5).
     */
    public enum Channel {
        /** No cadence observed yet, so liveness is genuinely unknown. */
        UNKNOWN,
        /** The latest refresh arrived within the expected poll window. */
        LIVE,
        /** The latest refresh was late: updates had stopped arriving. */
        RESUMED
    }

    /** The whole readout at a point in time. */
    public record Health(String connection, Channel channel, List<Stat> stats) {
        static final Health EMPTY = new Health("—", Channel.UNKNOWN, List.of());
    }

    /**
     * The outcome of one catalog load, as seen through the kit's database
     * meter: how many products were loaded and how many separate result-set
     * fetches (and rows) the kit attributed to that interaction.
     * {@code monitored} is false when the running kit build has no database
     * feature, so the meter never appeared.
     */
    public record DbFetchReadout(int products, int categories, long fetches,
            long rows, boolean monitored) {

        static final DbFetchReadout NONE = new DbFetchReadout(-1, 0, 0, 0,
                false);

        boolean isLoaded() {
            return products >= 0;
        }

        /**
         * More fetches than products is the signature of an unbatched N+1: one
         * query for the products plus one per-product collection fetch.
         */
        boolean looksLikeNPlusOne() {
            return monitored && products > 0 && fetches > products;
        }
    }

    private final transient MeterRegistry registry;
    private final transient ProductCatalogService catalog;
    private final ValueSignal<Health> health = new ValueSignal<>(Health.EMPTY);
    private final ValueSignal<DbFetchReadout> lastLoad = new ValueSignal<>(
            DbFetchReadout.NONE);
    private final Span status = new Span();
    private final Grid<Stat> grid = new Grid<>();
    private final Div catalogResult = new Div();
    private int refreshes;
    private long lastRefreshAt;
    private @Nullable Registration pollRegistration;

    public ApplicationHealthView(MeterRegistry registry,
            ProductCatalogService catalog) {
        this.registry = registry;
        this.catalog = catalog;

        add(new H1("UC2 — Watch the app's health from inside the app"));
        add(new Paragraph("A live view of the application's own signals — "
                + "active users, memory, recent interaction timings and the "
                + "current connection status — read straight from the "
                + "application's MeterRegistry. No external console required: "
                + "the readout is bound to a signal and refreshes on its own "
                + "every couple of seconds, so the numbers move as the app is "
                + "used."));

        status.setId("connection-status");
        status.getElement().getThemeList().add("badge");
        add(status);

        // The client meters are flushed by the browser only every few seconds.
        // This button asks the collector to POST whatever it has buffered right
        // now, so the client rows fill in without waiting for that timer.
        Button flush = new Button("Flush client metrics now",
                e -> flushClientMetrics());
        add(flush);

        grid.addColumn(Stat::signal).setHeader("Signal").setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(Stat::value).setHeader("Value").setAutoWidth(true);
        grid.addColumn(Stat::source).setHeader("Source meter")
                .setAutoWidth(true).setFlexGrow(1);
        grid.setAllRowsVisible(true);
        add(grid);

        // The primary signal-bound containers: re-run whenever the snapshot is
        // set (on poll). The badge reflects the refresh cadence; the grid the
        // numeric readout.
        Signal.effect(status, () -> {
            Health h = health.get();
            status.setText(h.connection());
            // Neutral until a cadence is known, green while ticks arrive on
            // time, red for a tick that arrives late.
            ThemeList themes = status.getElement().getThemeList();
            themes.set("success", h.channel() == Channel.LIVE);
            themes.set("error", h.channel() == Channel.RESUMED);
        });
        Signal.effect(grid, () -> grid.setItems(health.get().stats()));

        add(catalogSection());

        add(gapsCallout());

        recompute();
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        // Poll so the readout is genuinely live: the snapshot is recomputed on
        // every tick, surfacing other sessions coming and going, memory moving,
        // and client-collected samples the browser flushes every few seconds —
        // all without a click. recompute() runs on the UI thread, so the signal
        // write is safe.
        //
        // setPollInterval and the listener live on the UI, which outlives this
        // view, so both are undone in onDetach — otherwise the UI would keep
        // polling and keep invoking recompute() on a detached view after
        // navigating away (also pinning this view in memory). Polling is
        // assumed
        // disabled elsewhere, so onDetach simply turns it back off.
        UI ui = event.getUI();
        ui.setPollInterval(POLL_MILLIS);
        pollRegistration = ui.addPollListener(e -> recompute());
    }

    @Override
    protected void onDetach(DetachEvent event) {
        if (pollRegistration != null) {
            pollRegistration.remove();
            pollRegistration = null;
        }
        event.getUI().setPollInterval(-1);
        super.onDetach(event);
    }

    /**
     * Asks the in-browser collector to flush its buffer immediately instead of
     * waiting for its 5 s periodic timer. {@code window.__vaadinMicrometer
     * .flush()} is exposed by the kit's {@code VaadinMetricsClient.js} — but
     * only as an internal: its own source comments it "for tests / dashboards
     * (debug only)" and the {@code __} prefix says the same, so this button
     * leans on something the kit does not promise to keep (see
     * {@code API-GAPS.md} #9, which asks for a public flush). The call is
     * guarded so it degrades to a no-op if the internal disappears. The flushed
     * samples arrive in a follow-up request (the
     * collector's {@code recordSamples} callable, sent in the same UIDL message
     * as this script's return), so by the time the {@code then} callback fires
     * they are already in the registry and recompute() shows them. The 2 s poll
     * backstops anything that lands later.
     */
    private void flushClientMetrics() {
        getUI().ifPresent(ui -> ui.getPage()
                .executeJs("window.__vaadinMicrometer && "
                        + "window.__vaadinMicrometer.flush();")
                .then(ignored -> recompute()));
    }

    private void recompute() {
        refreshes++;
        long now = System.currentTimeMillis();
        long gapMillis = lastRefreshAt == 0 ? 0 : now - lastRefreshAt;
        Channel channel = channel(lastRefreshAt, now, POLL_MILLIS);
        lastRefreshAt = now;

        List<Stat> stats = new ArrayList<>();
        stats.add(new Stat("Active users (sessions)",
                count(gaugeValue(SESSIONS)), SESSIONS));
        stats.add(new Stat("Active UIs (browser tabs)", count(gaugeValue(UIS)),
                UIS));
        stats.add(new Stat("Heap used", megabytes(gaugeSum(HEAP_USED, "heap")),
                HEAP_USED + " {area=heap}"));
        stats.add(new Stat("Heap max", megabytes(gaugeSum(HEAP_MAX, "heap")),
                HEAP_MAX + " {area=heap}"));
        stats.add(timing("Server request handling", REQUEST));
        stats.add(timing("Server-side RPC", RPC));
        // Client-perceived timings, POSTed back by the in-browser collector.
        // These only appear once the browser emits and flushes them — bootstrap
        // and the web vitals shortly after the initial load, navigation on a
        // client-side route change — and never in a browserless test, which
        // doesn't run the JS collector.
        stats.add(timing("App bootstrap (client)", CLIENT_BOOTSTRAP));
        stats.add(timing("Client navigation", CLIENT_NAVIGATION));
        stats.add(timing("Largest Contentful Paint (client)", CLIENT_LCP));
        stats.add(timing("First Contentful Paint (client)", CLIENT_FCP));
        stats.add(
                new Stat("Server errors", count(counterTotal(ERRORS)), ERRORS));
        stats.add(new Stat("Client JS errors",
                count(counterTotal(CLIENT_ERRORS)), CLIENT_ERRORS));
        // The kit's database meter, live: it climbs as queries run (e.g. when
        // the catalog button below fires its N+1 fan-out).
        stats.add(dbFetchStat());

        health.set(new Health(connection(channel, gapMillis), channel, stats));
    }

    /**
     * Derives the channel state from the observed refresh cadence: the view
     * polls every {@code pollMillis}, so a tick within two poll intervals of
     * the previous one means updates are flowing, and a later one means they
     * had stopped in between. Pure and package-private so all three branches
     * are exercised in tests without a clock or a browser.
     */
    static Channel channel(long previousRefreshAt, long now, int pollMillis) {
        if (previousRefreshAt == 0) {
            return Channel.UNKNOWN;
        }
        return now - previousRefreshAt > 2L * pollMillis ? Channel.RESUMED
                : Channel.LIVE;
    }

    /**
     * The server's view of the connection. There is no connection-state meter
     * (API-GAPS.md #5: the browser's online/reconnecting state is never
     * recorded server-side), so the text says only what the server can see —
     * whether this UI's poll requests are still arriving on schedule — and
     * never claims to know the browser is online. The push mode and transport
     * come from the public {@link PushConfiguration}.
     */
    private String connection(Channel channel, long gapMillis) {
        String state = switch (channel) {
        case UNKNOWN -> "Waiting for the first live update";
        case LIVE -> "Live — updates arriving every " + (POLL_MILLIS / 1000)
                + " s";
        case RESUMED -> String.format("Resumed after %.1f s without updates",
                gapMillis / 1000d);
        };
        UI ui = UI.getCurrent();
        String push = "push n/a";
        if (ui != null) {
            PushConfiguration pc = ui.getPushConfiguration();
            push = "push " + pc.getPushMode() + " over " + pc.getTransport();
        }
        return state + " (" + push + "); " + refreshes
                + " refreshes this session";
    }

    private Stat timing(String label, String meter) {
        Collection<Timer> timers = registry.find(meter).timers();
        long total = 0;
        double sumMs = 0;
        double maxMs = 0;
        for (Timer t : timers) {
            total += t.count();
            sumMs += t.totalTime(TimeUnit.MILLISECONDS);
            maxMs = Math.max(maxMs, t.max(TimeUnit.MILLISECONDS));
        }
        String value = total == 0 ? "no samples yet"
                : String.format("mean %.1f ms, max %.1f ms (%d)", sumMs / total,
                        maxMs, total);
        return new Stat(label, value, meter);
    }

    private double gaugeValue(String meter) {
        Gauge gauge = registry.find(meter).gauge();
        return gauge == null ? Double.NaN : gauge.value();
    }

    private double gaugeSum(String meter, String area) {
        Collection<Gauge> gauges = registry.find(meter).tag("area", area)
                .gauges();
        if (gauges.isEmpty()) {
            return Double.NaN;
        }
        double sum = 0;
        for (Gauge g : gauges) {
            double v = g.value();
            // jvm.memory.max reports -1 for pools with no defined maximum;
            // skip those so an unbounded pool doesn't drag the total negative.
            if (v > 0) {
                sum += v;
            }
        }
        return sum;
    }

    private double counterTotal(String meter) {
        Collection<Counter> counters = registry.find(meter).counters();
        double sum = 0;
        for (Counter c : counters) {
            sum += c.count();
        }
        return sum;
    }

    private static String count(double value) {
        return Double.isNaN(value) ? "—" : Long.toString(Math.round(value));
    }

    private static String megabytes(double bytes) {
        return Double.isNaN(bytes) ? "—"
                : String.format("%.0f MB", bytes / (1024 * 1024));
    }

    /**
     * The database-health demo: a button that loads the product catalog and a
     * signal-bound readout of what that load cost, read from the kit's
     * {@code vaadin.db.fetch.rows} meter. The click handler runs on the UI
     * thread (so the {@code lastLoad} write is safe); it brackets the load with
     * a read of the kit meter so the fetch count is attributed to exactly this
     * interaction, then the effect repaints the result text.
     */
    private VerticalLayout catalogSection() {
        Button load = new Button(
                "Load product catalog (forces join-table fetch)",
                e -> loadCatalog());
        load.setId("load-catalog");

        catalogResult.setId("catalog-result");
        Signal.effect(catalogResult,
                () -> catalogResult.setText(describe(lastLoad.get())));

        VerticalLayout section = new VerticalLayout(
                new H2("Database health — N+1 join-table fetch"),
                new Paragraph("Each product has an eager many-to-many "
                        + "category association mapped through a join table, "
                        + "deliberately without @BatchSize. Loading the catalog "
                        + "therefore costs one query for the products plus one "
                        + "single-row fetch per product for its categories — "
                        + "the classic N+1. The Observability Kit's database "
                        + "feature (vaadin.observability.database=true) records "
                        + "each result-set fetch into vaadin.db.fetch.rows; "
                        + "click the button and that kit meter gives the problem "
                        + "away."),
                load, catalogResult);
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    /**
     * Loads the catalog and attributes the kit's recorded fetches to this click
     * by reading {@code vaadin.db.fetch.rows} immediately before and after the
     * load.
     */
    private void loadCatalog() {
        FetchTotals before = fetchTotals();
        CatalogLoad load = catalog.loadCatalog();
        FetchTotals after = fetchTotals();
        lastLoad.set(new DbFetchReadout(load.products(), load.categories(),
                after.count() - before.count(),
                Math.round(after.rows() - before.rows()), after.present()));
    }

    private static String describe(DbFetchReadout load) {
        if (!load.isLoaded()) {
            return "Catalog not loaded yet — click the button to fetch it.";
        }
        String head = String.format("Loaded %d products (%d category links). ",
                load.products(), load.categories());
        if (!load.monitored()) {
            return head + "The Observability Kit database meter "
                    + "(vaadin.db.fetch.rows) is not present — run a kit build "
                    + "with the database feature and set "
                    + "vaadin.observability.database=true to see the per-fetch "
                    + "breakdown.";
        }
        String counted = String.format(
                "The kit's vaadin.db.fetch.rows summary recorded %d separate "
                        + "result-set fetches (%d rows total) for that one "
                        + "click. ",
                load.fetches(), load.rows());
        if (load.looksLikeNPlusOne()) {
            return head + counted + "That is the N+1: one product query plus a "
                    + "single-row category fetch per product. Add "
                    + "@BatchSize(size = 100) to Product.category to collapse "
                    + "these into a couple of batched fetches.";
        }
        return head + counted
                + "No N+1 here — the per-product fetches were batched.";
    }

    /** Aggregate state of the {@code vaadin.db.fetch.rows} summary. */
    private record FetchTotals(boolean present, long count, double rows) {
    }

    /**
     * Reads the kit's per-result-set fetch summary across all route tags.
     * {@code present} is false when the meter has never been registered — i.e.
     * the running kit build has no database feature, or it is disabled.
     */
    private FetchTotals fetchTotals() {
        Collection<DistributionSummary> summaries = registry.find(DB_FETCH_ROWS)
                .summaries();
        long count = 0;
        double rows = 0;
        for (DistributionSummary s : summaries) {
            count += s.count();
            rows += s.totalAmount();
        }
        return new FetchTotals(!summaries.isEmpty(), count, rows);
    }

    /** Live row for the kit's database fetch meter in the health readout. */
    private Stat dbFetchStat() {
        FetchTotals t = fetchTotals();
        String value = !t.present()
                ? "no samples yet (enable vaadin.observability.database)"
                : String.format("%d fetches, %.1f rows mean", t.count(),
                        t.count() == 0 ? 0 : t.rows() / t.count());
        return new Stat("DB result-set fetches", value, DB_FETCH_ROWS);
    }

    private static Details gapsCallout() {
        UnorderedList list = new UnorderedList(
                new ListItem("Connection state: the browser's "
                        + "online/reconnecting/offline state is never recorded "
                        + "server-side, so the badge can only report the "
                        + "cadence of this UI's own poll requests, not the "
                        + "client's connection store (gap #5)."),
                new ListItem("@Push updates: server-pushed changes are not "
                        + "instrumented on the client, so push-delivered "
                        + "activity doesn't show up in the timing rows "
                        + "(gap #4)."),
                new ListItem("Per-UI footprint: we can count sessions and UIs, "
                        + "but not how much server memory each UI's state holds "
                        + "— the signal that actually predicts when to scale "
                        + "(gap #6)."),
                new ListItem("Flushing client samples: the \"flush now\" button "
                        + "has to call window.__vaadinMicrometer.flush(), which "
                        + "the kit documents as debug-only internal — there is "
                        + "no public API to drain the client collector "
                        + "(gap #12)."),
                new ListItem("Per-interaction DB attribution: the kit's "
                        + "vaadin.db.fetch.rows summary is cumulative, so "
                        + "\"what did this click cost\" has to be computed by "
                        + "bracketing the meter around the load (gap #13)."));
        Details details = new Details("What this can't show yet (and why)",
                list);
        details.add(new Anchor(
                "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md",
                "See API-GAPS.md"));
        return details;
    }
}
