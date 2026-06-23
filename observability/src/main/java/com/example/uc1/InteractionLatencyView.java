package com.example.uc1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.jspecify.annotations.Nullable;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC1 — How responsive is each interaction?
 * <p>
 * Trigger interactions of different server cost, then watch where the time goes.
 * The numbers come from Observability Kit's instrumentation, read out of the
 * application's {@link MeterRegistry}:
 * <ul>
 * <li>{@code vaadin.request.duration} — total server-side request handling,</li>
 * <li>{@code vaadin.rpc.duration} — the per-RPC invocation the framework now
 * times server-side (Flow's RPC invocation listener),</li>
 * <li>{@code vaadin.client.navigation.duration} and
 * {@code vaadin.client.web_vitals.lcp}/{@code .fcp} — page-load signals the
 * in-browser collector records into the same registry,</li>
 * <li>{@code uc1.interaction} — a per-action timer this view records itself,
 * because the framework tags an interaction only as {@code rpc}, not by which
 * action triggered it (see {@code API-GAPS.md}).</li>
 * </ul>
 * There is deliberately no client round-trip row: the browser collector does
 * not emit a per-RPC duration ({@code vaadin.client.rpc.duration} is measured
 * server-side only), so a single click's client/network share cannot be read —
 * see the callout and {@code API-GAPS.md}.
 * <p>
 * The readout is bound to a {@link ValueSignal}: a UI poll (and each click)
 * recomputes the snapshot and {@code set}s the signal, and a {@link Signal#effect}
 * repaints the grid. Polling is what lets the client samples — which the browser
 * only flushes every few seconds — show up without further interaction.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("UC1 — Interaction latency")
@Menu(order = 1, title = "UC1 — Interaction latency")
public class InteractionLatencyView extends VerticalLayout {

    private static final String SERVER_REQUEST = "vaadin.request.duration";
    private static final String SERVER_RPC = "vaadin.rpc.duration";
    private static final String CLIENT_NAVIGATION = "vaadin.client.navigation.duration";
    private static final String CLIENT_LCP = "vaadin.client.web_vitals.lcp";
    private static final String CLIENT_FCP = "vaadin.client.web_vitals.fcp";
    private static final String ACTION = "uc1.interaction";
    private static final int POLL_MILLIS = 2000;

    /** One row of the latency readout. */
    public record Row(String segment, long count, double meanMs, double maxMs) {
    }

    /** The whole readout at a point in time. */
    public record Snapshot(List<Row> rows) {
        static final Snapshot EMPTY = new Snapshot(List.of());
    }

    private final transient MeterRegistry registry;
    private final ValueSignal<Snapshot> snapshot = new ValueSignal<>(
            Snapshot.EMPTY);
    private final Grid<Row> grid = new Grid<>();
    private @Nullable Registration pollRegistration;

    public InteractionLatencyView(MeterRegistry registry) {
        this.registry = registry;

        add(new H1("UC1 — How responsive is each interaction?"));
        add(new Paragraph("Trigger interactions of different server cost, then "
                + "watch where the server time goes: total request handling, the "
                + "per-RPC invocation the framework now times, and a per-action "
                + "timer this view keeps. The browser adds page-load signals — "
                + "navigation timing and web vitals — recorded into the same "
                + "MeterRegistry by Observability Kit's client collector. The grid "
                + "is bound to a signal and refreshes as you interact and on a "
                + "short poll."));

        add(new HorizontalLayout(
                action("instant", () -> {
                    /* no server-side work */ }),
                action("light (150 ms)", () -> sleep(150)),
                action("heavy (400 ms)", () -> sleep(400))));

        grid.addColumn(Row::segment).setHeader("Segment").setAutoWidth(true)
                .setFlexGrow(1);
        grid.addColumn(r -> r.count() == 0 ? "—" : Long.toString(r.count()))
                .setHeader("Count").setAutoWidth(true);
        grid.addColumn(r -> millis(r.meanMs())).setHeader("Mean (ms)")
                .setAutoWidth(true);
        grid.addColumn(r -> millis(r.maxMs())).setHeader("Max (ms)")
                .setAutoWidth(true);
        grid.setAllRowsVisible(true);
        add(grid);

        // The primary signal-bound container: re-runs whenever the snapshot is
        // set (on poll or after an action).
        Signal.effect(grid, () -> grid.setItems(snapshot.get().rows()));

        add(gapsCallout());

        recompute();
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        // Poll so client-collected samples (flushed by the browser every few
        // seconds) and any cross-session activity appear without a click. The
        // recompute runs on the UI thread, so the signal write is safe.
        //
        // setPollInterval and the listener live on the UI, which outlives this
        // view, so both are undone in onDetach — otherwise the UI would keep
        // polling and keep invoking recompute() on a detached view after
        // navigating away (also pinning this view in memory). Polling is
        // assumed disabled elsewhere, so onDetach simply turns it back off.
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

    private Button action(String name, Runnable serverWork) {
        return new Button(name, e -> timed(name, serverWork));
    }

    /**
     * Records the server-side cost of one action under {@link #ACTION}, tagged
     * by action name — the per-action granularity the framework's own
     * {@code vaadin.request.duration} timer does not provide.
     */
    private void timed(String name, Runnable serverWork) {
        Timer.Sample sample = Timer.start(registry);
        try {
            serverWork.run();
        } finally {
            sample.stop(registry.timer(ACTION, "action", name));
        }
        recompute();
    }

    private void recompute() {
        List<Row> rows = new ArrayList<>();
        rows.add(aggregate("Server handling — " + SERVER_REQUEST,
                SERVER_REQUEST));
        rows.add(aggregate("Server RPC invocation — " + SERVER_RPC,
                SERVER_RPC));
        rows.add(aggregate("Client navigation — " + CLIENT_NAVIGATION,
                CLIENT_NAVIGATION));
        rows.add(aggregate("Client LCP — " + CLIENT_LCP, CLIENT_LCP));
        rows.add(aggregate("Client FCP — " + CLIENT_FCP, CLIENT_FCP));

        registry.find(ACTION).timers().stream()
                .sorted(Comparator
                        .comparing(t -> t.getId().getTag("action") == null ? ""
                                : t.getId().getTag("action")))
                .forEach(t -> rows.add(new Row(
                        "action: " + t.getId().getTag("action"), t.count(),
                        t.totalTime(TimeUnit.MILLISECONDS) / Math.max(1,
                                t.count()),
                        t.max(TimeUnit.MILLISECONDS))));

        snapshot.set(new Snapshot(rows));
    }

    private Row aggregate(String label, String name) {
        Collection<Timer> timers = registry.find(name).timers();
        long count = 0;
        double total = 0;
        double max = 0;
        for (Timer t : timers) {
            count += t.count();
            total += t.totalTime(TimeUnit.MILLISECONDS);
            max = Math.max(max, t.max(TimeUnit.MILLISECONDS));
        }
        double mean = count > 0 ? total / count : Double.NaN;
        return new Row(label, count, mean, max);
    }

    private static Details gapsCallout() {
        UnorderedList list = new UnorderedList(
                new ListItem("No client round-trip: the browser collector emits "
                        + "navigation timing, web vitals and errors, but no "
                        + "per-RPC duration (vaadin.client.rpc.duration is "
                        + "server-side only), so a single click's client/network "
                        + "share can't be read (gap #2)."),
                new ListItem("Per-interaction split: client and server samples "
                        + "share no correlation id, so a slow click can't be "
                        + "attributed to client vs. network vs. server (gap #3)."),
                new ListItem("@Push updates: the client collector covers XHR "
                        + "navigation, not server-pushed changes, so push isn't "
                        + "timed client-side (gap #4)."));
        Details details = new Details(
                "What this can't show yet (and why)", list);
        details.add(new Anchor(
                "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md",
                "See API-GAPS.md"));
        return details;
    }

    private static String millis(double value) {
        return Double.isNaN(value) ? "—" : String.format("%.1f", value);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
