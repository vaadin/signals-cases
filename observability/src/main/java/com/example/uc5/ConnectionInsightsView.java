package com.example.uc5;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.uc5.ClientErrorLog.BrowserError;
import com.example.views.MainLayout;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.observability.micrometer.MeterNames;

/**
 * UC5 — Notice connection and client-side problems.
 * <p>
 * The class of failure that never reaches a server log: a user's browser loses
 * the server and gets it back, and a script fails in a tab nobody is watching.
 * The server sees a session that goes quiet and then talks again, and — for the
 * failed script — nothing at all.
 * <p>
 * All of the connection half is now the Observability Kit's, and this view only
 * reads it. The in-browser collector subscribes to Flow's own
 * {@code window.Vaadin.connectionState} and records
 * {@link MeterNames#CLIENT_CONNECTION} per transition and
 * {@link MeterNames#CLIENT_CONNECTION_DOWNTIME} for the time spent unable to
 * reach the server, both tagged {@link MeterNames#TAG_STATE}. Three properties
 * of those meters are what this view exists to make visible:
 * <ul>
 * <li><b>Downtime is per state, not per outage.</b> Flow enters
 * {@code reconnecting} on the first failed request and only reaches
 * {@code connection-lost} once it has given up retrying, so the two answer
 * different questions — a network that hiccuped versus a server the browser has
 * written off — and a short outage that recovers while Flow is still retrying
 * never enters {@code connection-lost} at all. The readout shows both, and
 * their sum, which is the length of the whole outage.</li>
 * <li><b>The clock is the browser's.</b> A transition into an unreachable state
 * cannot be sent while the browser is in it, so the collector buffers into
 * {@code sessionStorage} and flushes on recovery, measuring the outage on the
 * clock that timestamped it.</li>
 * <li><b>The timer under-reports by construction.</b> A browser that never
 * comes back reports nothing, so the transition <em>count</em> is the honest
 * measure of how often, and the timer only of how long the observed ones
 * lasted.</li>
 * </ul>
 * <p>
 * Two server-side signals sit alongside them: {@link MeterNames#RESYNC}, which
 * counts the messages a client re-sent having had no answer and the full state
 * rebuilds it asked for after losing one — Flow handles both internally, so
 * without the kit they are invisible — and {@link MeterNames#CLIENT_THROTTLED},
 * which matters here because the reports of one outage all arrive in a single
 * flush and can outrun the per-session rate limit.
 * <p>
 * What the kit still does not collect is the <em>detail</em> of a browser
 * error: {@link MeterNames#CLIENT_ERRORS} is a count tagged {@code uncaught} or
 * {@code promise}, and the message, script and stack are dropped where they are
 * collected. {@link ClientErrorReporter} keeps them, and the section below the
 * meters is what a count cannot tell you. That is the remaining half of
 * {@code API-GAPS.md} #5.
 * <p>
 * <b>Why this view does not poll.</b> A poll is a UIDL request, so a polling
 * view probes the connection on every tick: the loading round trip itself is
 * ignored by the collector, but a poll that gets through ends the outage as far
 * as the browser is concerned, and a polling tab therefore reports shorter
 * downtime than a passive one on the same network. The kit's own README says as
 * much. So the readout refreshes from the report instead — an error report is
 * an RPC and repaints this view from inside it — and the refresh button is for
 * problems reported by <em>other</em> tabs, which this one has no way to be
 * told about.
 *
 * @see <a href=
 *      "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md">API-GAPS.md</a>
 */
@Route(value = "uc5", layout = MainLayout.class)
@PageTitle("UC5 — Connection & client problems")
@Menu(order = 5, title = "UC5 — Connection & client problems")
public class ConnectionInsightsView extends VerticalLayout {

    /** How long the simulated outage lasts, in milliseconds. */
    private static final int OUTAGE_MILLIS = 3000;

    /**
     * Drives Flow's own connection-state store through a state and back, which
     * is what the kit's own integration test does: it is the same store Flow's
     * reconnect logic drives, so it fires exactly the listeners a real outage
     * fires — the browser's "Connection lost" indicator included.
     * <p>
     * The 300 ms head start matters: this script runs while the response to the
     * click is still being applied, and finishing that response sets the store
     * back to {@code connected}. Waiting for the request to end leaves the
     * simulated state standing.
     */
    private static final String SIMULATE = """
            const state = $0;
            const millis = $1;
            setTimeout(function () {
                const store = window.Vaadin && window.Vaadin.connectionState;
                if (!store) {
                    return;
                }
                store.state = state;
                setTimeout(function () {
                    store.state = 'connected';
                }, millis);
            }, 300);
            """;

    /** Thrown asynchronously, so it reaches window.onerror uncaught. */
    private static final String THROW = """
            setTimeout(function () {
                throw new Error($0);
            }, 0);
            """;

    private static final String REJECT = "Promise.reject(new Error($0));";

    /**
     * Asks the collector to send what it is holding, so the meters move without
     * waiting for its 5 s timer. Still the debug internal UC2 leans on — there
     * is no public drain (gap #12).
     */
    private static final String FLUSH = """
            window.__vaadinMicrometer && window.__vaadinMicrometer.flush();
            """;

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /** One row of the meter readout. */
    public record Stat(String signal, String value, String meter,
            String reads) {
    }

    /** The whole readout at a point in time. */
    public record Readout(String status, boolean degraded, List<Stat> meters,
            List<BrowserError> errors) {
        static final Readout EMPTY = new Readout("", false, List.of(),
                List.of());
    }

    private final transient MeterRegistry registry;
    private final transient ClientErrorLog log;
    private final ValueSignal<Readout> readout = new ValueSignal<>(
            Readout.EMPTY);
    private final Span status = new Span();
    private final Grid<Stat> meters = new Grid<>();
    private final Grid<BrowserError> errors = new Grid<>();

    public ConnectionInsightsView(MeterRegistry registry, ClientErrorLog log) {
        this.registry = registry;
        this.log = log;

        add(new H1("UC5 — Notice connection and client-side problems"));
        add(new Paragraph("A user's browser loses the server and gets it back; "
                + "a script fails in a tab nobody is watching. Neither reaches "
                + "a server log. Every meter below is the Observability Kit's: "
                + "its in-browser collector subscribes to Flow's own "
                + "connection-state store and reports what it saw once the "
                + "browser can talk again. Simulate a problem with the "
                + "buttons, or take the network away in devtools — both go "
                + "through the same store."));

        status.setId("connection-status");
        status.getElement().getThemeList().add("badge");
        add(status);

        add(actions());

        meters.addColumn(Stat::signal).setHeader("Signal").setFlexGrow(1);
        meters.addColumn(Stat::value).setHeader("Value").setAutoWidth(true);
        meters.addColumn(Stat::meter).setHeader("Meter").setAutoWidth(true);
        meters.addColumn(Stat::reads).setHeader("What it tells you")
                .setFlexGrow(2);
        meters.setAllRowsVisible(true);
        meters.setId("problem-meters");
        meters.addThemeName("wrap-cell-content");
        add(new H2("What the kit records"), meters);

        errors.addColumn(e -> TIME.format(e.recordedAt())).setHeader("Recorded")
                .setAutoWidth(true);
        errors.addColumn(BrowserError::client).setHeader("Browser")
                .setAutoWidth(true);
        errors.addColumn(BrowserError::kind).setHeader("Kind")
                .setAutoWidth(true);
        errors.addColumn(BrowserError::message).setHeader("Message")
                .setFlexGrow(2);
        errors.addColumn(BrowserError::where).setHeader("Where").setFlexGrow(2);
        errors.setAllRowsVisible(true);
        errors.setId("error-detail");
        errors.addThemeName("wrap-cell-content");
        add(new H2("What the count can't tell you"), errors);
        add(new Paragraph("The rows above count browser errors and keep "
                + "nothing else. These are the same errors with the message, "
                + "script and first stack frame the collector drops where it "
                + "collects them — kept by this view's own listener, because "
                + "the kit's ingest takes its own sample names and a message "
                + "could not travel as a meter tag anyway. This is the half of "
                + "gap #5 that is still open."));

        // The primary signal-bound containers: badge and both grids repaint
        // from one snapshot, so the meters and the detail cannot disagree.
        Signal.effect(status, () -> {
            Readout current = readout.get();
            status.setText(current.status());
            ThemeList themes = status.getElement().getThemeList();
            themes.set("error", current.degraded());
        });
        Signal.effect(meters, () -> meters.setItems(readout.get().meters()));
        Signal.effect(errors, () -> errors.setItems(readout.get().errors()));

        add(callout());

        // Only the error detail is the application's now; the connection
        // meters arrive through the kit's own collector, on every UI.
        add(new ClientErrorReporter(log, this::recompute));

        recompute();
    }

    private HorizontalLayout actions() {
        Button lose = new Button("Simulate a 3 s connection loss",
                event -> simulate(MeterNames.STATE_CONNECTION_LOST,
                        OUTAGE_MILLIS,
                        "Connection lost for 3 s. Leave the page alone: any "
                                + "request that gets through ends the outage "
                                + "as far as the browser is concerned."));
        lose.addThemeVariants(ButtonVariant.ERROR, ButtonVariant.PRIMARY);
        lose.setId("simulate-loss");

        // The other unreachable state, and the one a short real outage
        // actually spends its time in.
        Button reconnect = new Button("Simulate 1.5 s reconnecting",
                event -> simulate(MeterNames.STATE_RECONNECTING, 1500,
                        "Reconnecting for 1.5 s — the state a short outage "
                                + "recovers from without ever being given up "
                                + "on."));
        reconnect.addThemeVariants(ButtonVariant.WARNING,
                ButtonVariant.PRIMARY);
        reconnect.setId("simulate-reconnecting");

        Button error = new Button("Throw an uncaught browser error",
                event -> run(THROW, "UC5: rendering the sales chart failed"));
        error.setId("throw-error");

        Button rejection = new Button("Reject a promise",
                event -> run(REJECT, "UC5: fetching /api/quotes failed"));
        rejection.setId("reject-promise");

        Button refresh = new Button("Refresh", event -> {
            getUI().ifPresent(ui -> ui.getPage().executeJs(FLUSH)
                    .then(ignored -> recompute()));
            recompute();
        });
        refresh.setId("refresh");

        Button clear = new Button("Clear error log", event -> {
            log.clear();
            recompute();
        });
        clear.setId("clear-log");

        HorizontalLayout actions = new HorizontalLayout(lose, reconnect, error,
                rejection, refresh, clear);
        actions.setWrap(true);
        return actions;
    }

    private void simulate(String state, int millis, String message) {
        getUI().ifPresent(
                ui -> ui.getPage().executeJs(SIMULATE, state, millis));
        Notification.show(message);
    }

    private void run(String script, String message) {
        getUI().ifPresent(ui -> ui.getPage().executeJs(script, message));
    }

    /**
     * Rebuilds the snapshot. Called from the click handlers and from the error
     * reporter's {@code @ClientCallable}, both of which run on this UI's
     * thread, so the signal write is safe.
     */
    void recompute() {
        List<BrowserError> recent = log.recent();
        List<Stat> stats = stats();
        readout.set(new Readout(statusOf(), degraded(), stats, recent));
    }

    private List<Stat> stats() {
        List<Stat> stats = new ArrayList<>();
        stats.add(new Stat("Browsers that gave up on the server",
                counter(MeterNames.CLIENT_CONNECTION, MeterNames.TAG_STATE,
                        MeterNames.STATE_CONNECTION_LOST),
                tagged(MeterNames.CLIENT_CONNECTION,
                        MeterNames.STATE_CONNECTION_LOST),
                "Flow reaches this state only after exhausting its retries, so "
                        + "each one is an outage a user sat through"));
        stats.add(new Stat("Browsers that started retrying",
                counter(MeterNames.CLIENT_CONNECTION, MeterNames.TAG_STATE,
                        MeterNames.STATE_RECONNECTING),
                tagged(MeterNames.CLIENT_CONNECTION,
                        MeterNames.STATE_RECONNECTING),
                "Entered on the first failed request — the honest count of how "
                        + "often the connection faltered"));
        stats.add(new Stat("Recoveries",
                counter(MeterNames.CLIENT_CONNECTION, MeterNames.TAG_STATE,
                        MeterNames.STATE_CONNECTED),
                tagged(MeterNames.CLIENT_CONNECTION,
                        MeterNames.STATE_CONNECTED),
                "Fewer recoveries than losses means browsers that never came "
                        + "back, and whose downtime is therefore unmeasured"));
        stats.add(new Stat("Time spent given up on",
                downtime(MeterNames.STATE_CONNECTION_LOST),
                tagged(MeterNames.CLIENT_CONNECTION_DOWNTIME,
                        MeterNames.STATE_CONNECTION_LOST),
                "A server the browser had written off; measured on the "
                        + "browser's clock, since the report can only be sent "
                        + "once it is back"));
        stats.add(new Stat("Time spent retrying",
                downtime(MeterNames.STATE_RECONNECTING),
                tagged(MeterNames.CLIENT_CONNECTION_DOWNTIME,
                        MeterNames.STATE_RECONNECTING),
                "A network that hiccuped; a short outage never leaves this "
                        + "state, so it would be missed by an end-to-end "
                        + "measure"));
        stats.add(new Stat("Whole outages, both states summed", wholeOutages(),
                MeterNames.CLIENT_CONNECTION_DOWNTIME + " (sum of tags)",
                "The length of an outage end to end, which is the sum and not "
                        + "either tag alone"));
        stats.add(new Stat("Uncaught browser errors",
                counter(MeterNames.CLIENT_ERRORS, MeterNames.TAG_KIND,
                        "uncaught"),
                tagged(MeterNames.CLIENT_ERRORS, "uncaught"),
                "A count, and nothing else — see the detail below"));
        stats.add(new Stat("Unhandled promise rejections",
                counter(MeterNames.CLIENT_ERRORS, MeterNames.TAG_KIND,
                        "promise"),
                tagged(MeterNames.CLIENT_ERRORS, "promise"),
                "The other half of the same counter"));
        stats.add(new Stat("Messages the client re-sent, having had no answer",
                counter(MeterNames.RESYNC, MeterNames.TAG_TYPE,
                        MeterNames.RESYNC_TYPE_RESEND),
                tagged(MeterNames.RESYNC, MeterNames.RESYNC_TYPE_RESEND),
                "The server side of a lost message; Flow replays its cached "
                        + "response and says nothing"));
        stats.add(new Stat("Full UI-state resynchronizations",
                counter(MeterNames.RESYNC, MeterNames.TAG_TYPE,
                        MeterNames.RESYNC_TYPE_RESYNC),
                tagged(MeterNames.RESYNC, MeterNames.RESYNC_TYPE_RESYNC),
                "The client gave up waiting and asked for the whole UI state "
                        + "again"));
        stats.add(new Stat("Client samples refused",
                counter(MeterNames.CLIENT_DROPPED) + " dropped, "
                        + counter(MeterNames.CLIENT_THROTTLED) + " throttled",
                MeterNames.CLIENT_DROPPED + " / " + MeterNames.CLIENT_THROTTLED,
                "One outage flushes as one batch, which can outrun the "
                        + "per-session rate limit; the kit sends connection "
                        + "samples first, so what is lost here is timing"));
        return stats;
    }

    /**
     * The badge: what the kit's connection meters say has happened since this
     * server started, in a sentence.
     */
    private String statusOf() {
        long lost = count(MeterNames.CLIENT_CONNECTION,
                MeterNames.STATE_CONNECTION_LOST);
        long retrying = count(MeterNames.CLIENT_CONNECTION,
                MeterNames.STATE_RECONNECTING);
        long browserErrors = Math.round(counterTotal(
                registry.find(MeterNames.CLIENT_ERRORS).counters()));
        if (lost + retrying + browserErrors == 0) {
            return "Nothing recorded since this server started. Simulate a "
                    + "problem, or go offline in devtools and come back.";
        }
        double seconds = totalSeconds(MeterNames.STATE_CONNECTION_LOST)
                + totalSeconds(MeterNames.STATE_RECONNECTING);
        return ("%d loss(es), %d retry period(s) totalling %.1f s, "
                + "%d browser error(s) since this server started")
                .formatted(lost, retrying, seconds, browserErrors);
    }

    /** Red once anything has gone wrong; there is no good news to report. */
    private boolean degraded() {
        return count(MeterNames.CLIENT_CONNECTION,
                MeterNames.STATE_CONNECTION_LOST)
                + count(MeterNames.CLIENT_CONNECTION,
                        MeterNames.STATE_RECONNECTING)
                + Math.round(counterTotal(registry
                        .find(MeterNames.CLIENT_ERRORS).counters())) > 0;
    }

    private static String tagged(String meter, String tagValue) {
        return meter + " {" + tagValue + "}";
    }

    /**
     * Sums a counter across all of its series. A meter that was never
     * registered reads as a dash rather than 0: "nothing has happened" and
     * "nothing is watching" are different answers.
     */
    private String counter(String meter) {
        return format(registry.find(meter).counters());
    }

    /** The same sum, narrowed to one tag. */
    private String counter(String meter, String tagKey, String tagValue) {
        return format(registry.find(meter).tag(tagKey, tagValue).counters());
    }

    private static String format(Collection<Counter> counters) {
        return counters.isEmpty() ? "—"
                : Long.toString(Math.round(counterTotal(counters)));
    }

    private long count(String meter, String state) {
        return Math.round(counterTotal(registry.find(meter)
                .tag(MeterNames.TAG_STATE, state).counters()));
    }

    private static double counterTotal(Collection<Counter> counters) {
        double total = 0;
        for (Counter counter : counters) {
            total += counter.count();
        }
        return total;
    }

    /** The downtime timer for one unreachable state. */
    private String downtime(String state) {
        Timer timer = registry.find(MeterNames.CLIENT_CONNECTION_DOWNTIME)
                .tag(MeterNames.TAG_STATE, state).timer();
        if (timer == null || timer.count() == 0) {
            return "—";
        }
        return "%d period(s), %.1f s total, longest %.1f s".formatted(
                timer.count(), timer.totalTime(TimeUnit.SECONDS),
                timer.max(TimeUnit.SECONDS));
    }

    /**
     * The length of the outages end to end. The kit splits the timer by state
     * because the two states mean different things; adding them back is the
     * application's to do, and is the number an SLO would use.
     */
    private String wholeOutages() {
        double seconds = totalSeconds(MeterNames.STATE_CONNECTION_LOST)
                + totalSeconds(MeterNames.STATE_RECONNECTING);
        return seconds == 0 ? "—"
                : "%.1f s across both states".formatted(seconds);
    }

    private double totalSeconds(String state) {
        Timer timer = registry.find(MeterNames.CLIENT_CONNECTION_DOWNTIME)
                .tag(MeterNames.TAG_STATE, state).timer();
        return timer == null ? 0 : timer.totalTime(TimeUnit.SECONDS);
    }

    private static Details callout() {
        UnorderedList list = new UnorderedList(new ListItem(
                "Browser errors are counted, not described. The message, "
                        + "script and stack are dropped where they are "
                        + "collected, and the ingest allowlist would reject "
                        + "them anyway, so the detail table above is this "
                        + "view's own listener — the open half of gap #5."),
                new ListItem("That listener also cannot say how long a report "
                        + "waited. The collector buffers its samples through "
                        + "an outage, into sessionStorage, and stamps each "
                        + "with a client-measured age; application data cannot "
                        + "go through that pipeline, so an error reported "
                        + "during an outage arrives whenever Flow's message "
                        + "queue delivers it, undated."),
                new ListItem("Nor does it group. The kit's insight buffer "
                        + "already folds repeats into one entry with an "
                        + "occurrence count, hashes the session id and "
                        + "truncates messages; a hundred tabs hitting the same "
                        + "broken chart give a hundred rows here."),
                new ListItem("Downtime under-reports by construction: a "
                        + "browser that never comes back reports nothing, and "
                        + "an outage spanning a reload keeps its count but "
                        + "loses its clock. Read the counts for how often, the "
                        + "timer only for how long the observed ones lasted."),
                new ListItem("Instrumentation attached to a view watches only "
                        + "that view. The kit's collector is attached to every "
                        + "UI; this view's error listener is added by UC5, so "
                        + "navigating away stops it."),
                new ListItem("Push transport is still not instrumented on the "
                        + "client, so an app using @Push has no client-side "
                        + "view of its own delivery (gap #4)."));
        Details details = new Details("What this can't show yet (and why)",
                list);
        details.add(new Anchor(
                "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md",
                "See API-GAPS.md"));
        return details;
    }
}
