package com.example.uc6;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.observability.micrometer.ObservabilityKit;
import com.vaadin.observability.spring.boot.VaadinObservabilityEndpoint;

/**
 * UC6 — Keep an eye on failures.
 * <p>
 * Break something, then read what the developer would actually get: not a stack
 * trace in a log, but a grouped insight naming <em>where</em> the failure
 * happened — the route, the component the user interacted with, the event that
 * triggered it, and the first application stack frame. Interactions that
 * succeed but exceed the UX latency budget surface the same way.
 * <p>
 * The data comes from Observability Kit's interaction insights. The kit hooks
 * Flow's RPC invocation listener, keeps a bounded buffer of the interesting
 * interactions (failed ones, and ones slower than its UX budget), and groups
 * them so that N users hitting the same broken button produce one insight with
 * an occurrence count.
 * <p>
 * Rather than re-derive that data, this view injects the kit's Actuator
 * endpoint — {@link VaadinObservabilityEndpoint} is a Spring bean the kit's
 * auto-configuration registers — and renders exactly what
 * {@code GET /actuator/vaadin/observability} returns: a table of the insights,
 * and the payload itself, serialized with the application's own
 * {@link ObjectMapper}. That payload is the contract an AI coding agent reads
 * to jump straight to the offending line.
 * <p>
 * The readout is bound to a {@link ValueSignal} and refreshed once per click,
 * with no polling. The refresh cannot simply run at the end of the listener:
 * the kit records a failure from {@code invocationFailed} and an over-budget
 * interaction from {@code invocationEnded}, both of which happen after the
 * listener body — and a failing listener never reaches its own last line
 * anyway. Each action therefore schedules a {@code beforeClientResponse}
 * callback <em>before</em> doing its work, which runs after all invocation
 * handling and still lands in the same response.
 * <p>
 * The actions deliberately let their exception propagate: the kit records a
 * failed interaction only when the invocation actually fails, so catching it in
 * the listener would erase the very thing this use case demonstrates. A session
 * {@link ErrorHandler} installed while this view is attached turns the failure
 * into the notification a real application would show.
 *
 * @see <a href=
 *      "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md">API-GAPS.md</a>
 */
@Route(value = "uc6", layout = MainLayout.class)
@PageTitle("UC6 — Failure insights")
@Menu(order = 6, title = "UC6 — Failure insights")
public class FailureInsightsView extends VerticalLayout {

    /** The endpoint's selector, i.e. {@code /actuator/vaadin/observability}. */
    private static final String SECTION = "observability";

    /** Above the kit's UX budget (1 s), so the interaction is retained. */
    private static final long SLOW_MILLIS = 1500;

    /** One insight, flattened for display. */
    public record Row(String severity, String summary, String component,
            String event, long occurrences, String where) {
    }

    /**
     * The readout at a point in time: the flattened rows plus the endpoint
     * payload verbatim. {@code active} is {@code false} when the kit registered
     * no instrumentation — in development mode that happens without a license
     * key, and then there is nothing to report.
     */
    public record Snapshot(List<Row> rows, String json, boolean active) {
        static final Snapshot INACTIVE = new Snapshot(List.of(), "", false);
    }

    private final transient VaadinObservabilityEndpoint endpoint;
    private final transient ObjectMapper json;
    private final ValueSignal<Snapshot> snapshot = new ValueSignal<>(
            Snapshot.INACTIVE);
    private final Grid<Row> grid = new Grid<>();
    private final Span status = new Span();
    private final Pre payload = new Pre();
    private @Nullable ErrorHandler previousErrorHandler;

    /**
     * @param endpoint
     *            the kit's insights endpoint bean, so this view shows the very
     *            same payload the HTTP endpoint serves
     * @param json
     *            the application's own Jackson mapper, so the payload is
     *            serialized the way the endpoint serializes it
     */
    public FailureInsightsView(VaadinObservabilityEndpoint endpoint,
            ObjectMapper json) {
        this.endpoint = endpoint;
        this.json = json;

        add(new H1("UC6 — Keep an eye on failures"));
        add(new Paragraph("Trigger a failure, or an interaction that is merely "
                + "too slow, and watch it show up below as an insight that names "
                + "the component, the event and the line of application code "
                + "responsible. Repeat a failure and it groups into one entry "
                + "with an occurrence count instead of a wall of stack traces. "
                + "Both the table and the raw payload come from the kit's "
                + "Actuator endpoint, injected here as a Spring bean."));

        // Colour-coded by the severity each action produces: red buttons yield
        // an "error" insight, the amber one a "warning", green none at all.
        add(new HorizontalLayout(
                action("Fail now", ButtonVariant.LUMO_ERROR, () -> {
                    throw new IllegalStateException(
                            "Report template 'summary' not found");
                }), action("Fail differently", ButtonVariant.LUMO_ERROR, () -> {
                    throw new IllegalArgumentException(
                            "Customer id must not be blank");
                }),
                action("Slow call (1.5 s)", ButtonVariant.LUMO_WARNING,
                        () -> sleep(SLOW_MILLIS)),
                action("Succeed", ButtonVariant.LUMO_SUCCESS,
                        () -> Notification.show("Done"))));

        status.getStyle().set("font-style", "italic");
        add(status);

        grid.addColumn(Row::severity).setHeader("Severity").setAutoWidth(true);
        grid.addColumn(Row::summary).setHeader("Insight").setFlexGrow(1);
        grid.addColumn(Row::component).setHeader("Component")
                .setAutoWidth(true);
        grid.addColumn(Row::event).setHeader("Event").setAutoWidth(true);
        grid.addColumn(Row::occurrences).setHeader("Count").setAutoWidth(true);
        // Both text columns flex, so the summary is not squeezed out by the
        // (much longer) stack frame.
        grid.addColumn(Row::where).setHeader("Application frame")
                .setFlexGrow(1);
        grid.setAllRowsVisible(true);
        add(grid);

        // The primary signal-bound container: re-runs whenever the snapshot is
        // set, on each poll. Grid, status line and raw payload all repaint from
        // the same snapshot, so the JSON stays in step with the table.
        Signal.effect(grid, () -> {
            Snapshot current = snapshot.get();
            grid.setItems(current.rows());
            status.setText(statusText(current));
            payload.setText(current.json().isEmpty() ? "// nothing recorded yet"
                    : current.json());
        });

        add(callout());
        add(payloadPanel());

        recompute();
    }

    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        UI ui = event.getUI();

        // Surface failures the way an application would, without swallowing
        // them: the exception has already propagated through Flow (and been
        // recorded by the kit) by the time the handler runs.
        VaadinSession session = ui.getSession();
        if (session != null) {
            previousErrorHandler = session.getErrorHandler();
            session.setErrorHandler(errorEvent -> ui.access(() -> {
                Notification notification = Notification.show(
                        "Something went wrong. It is now in the insights below.");
                notification.setPosition(Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.ERROR);
            }));
        }
    }

    @Override
    protected void onDetach(DetachEvent event) {
        UI ui = event.getUI();
        VaadinSession session = ui.getSession();
        if (session != null && previousErrorHandler != null) {
            session.setErrorHandler(previousErrorHandler);
            previousErrorHandler = null;
        }
        super.onDetach(event);
    }

    private void recompute() {
        Map<String, Object> current = endpoint.section(SECTION);
        if (current == null) {
            current = Map.of();
        }
        List<Row> rows = new ArrayList<>();
        for (Map<String, Object> insight : insightsOf(current)) {
            Map<String, Object> evidence = evidenceOf(insight);
            rows.add(new Row(text(insight.get("severity")),
                    text(insight.get("summary")),
                    simpleName(text(evidence.get("component"))),
                    text(evidence.get("event")),
                    count(evidence.get("occurrences")),
                    text(evidence.get("applicationFrame"))));
        }
        snapshot.set(new Snapshot(rows, pretty(current),
                ObservabilityKit.getRecentInteractions() != null));
    }

    /**
     * Builds one action button. The refresh is scheduled <em>before</em> the
     * work runs, so it happens even when the work throws and the listener never
     * returns.
     */
    private Button action(String label, ButtonVariant variant, Runnable work) {
        Button button = new Button(label, e -> {
            refreshWithThisResponse();
            work.run();
        });
        button.addThemeVariants(variant, ButtonVariant.LUMO_PRIMARY);
        return button;
    }

    /**
     * Recomputes the readout while this request's response is being written.
     * <p>
     * That timing matters. The kit records a failed interaction from
     * {@code invocationFailed} and an over-budget one from
     * {@code invocationEnded}, both of which run after the click listener body
     * — so refreshing inside the listener would miss the very insight the click
     * just produced. A {@code beforeClientResponse} callback runs after all
     * invocation handling, and still lands in the same response, which is why
     * this view needs no polling.
     */
    private void refreshWithThisResponse() {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.beforeClientResponse(this, context -> recompute());
        }
    }

    /**
     * Shows the endpoint payload verbatim, refreshed by the same poll as the
     * grid: this is what {@code curl /actuator/vaadin/observability} returns
     * and what an agent consumes.
     */
    private Details payloadPanel() {
        payload.getStyle().set("margin", "0").set("font-size", "0.7rem")
                .set("line-height", "1.35").set("white-space", "pre-wrap")
                .set("word-break", "break-word");
        // A viewport-relative cap keeps the payload scrolling inside its own
        // box: a flex height would not propagate through the Details shadow
        // DOM, and the panel would overflow onto what follows it.
        Div scroller = new Div(payload);
        scroller.getStyle().set("max-height", "55vh").set("overflow", "auto")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)").set("width", "100%");
        Details details = new Details(
                "Endpoint payload — GET /actuator/vaadin/observability",
                scroller);
        details.setOpened(true);
        details.setWidthFull();
        return details;
    }

    private String pretty(Map<String, Object> current) {
        if (current.isEmpty()) {
            return "";
        }
        try {
            return json.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(current);
        } catch (RuntimeException e) {
            return "// could not serialize the payload: " + e;
        }
    }

    private static String statusText(Snapshot snapshot) {
        if (!snapshot.active()) {
            return "Observability Kit registered no instrumentation, so there "
                    + "is nothing to report. In development mode this means no "
                    + "license key was found.";
        }
        return snapshot.rows().isEmpty()
                ? "No failed or over-budget interactions recorded yet."
                : snapshot.rows().size() + " insight(s) recorded.";
    }

    private static Details callout() {
        UnorderedList list = new UnorderedList(new ListItem(
                "An insight names the component and event, plus the first "
                        + "non-framework stack frame — the likely bug location. "
                        + "That closes gap #8, which asked for interaction "
                        + "attribution beyond the RPC type."),
                new ListItem("The attribution lives on insights and spans, not "
                        + "on meter tags: latency and error metrics are still "
                        + "tagged by RPC type and exception only, so a "
                        + "dashboard cannot group by component."),
                new ListItem("Capture happens on the RPC invocation hook, "
                        + "which only fires for a real UIDL request. Browserless "
                        + "tests call listeners directly, so they cannot "
                        + "exercise the capture — this view's insights need a "
                        + "browser."));
        Details details = new Details(
                "How this works, and what it still can't do", list);
        details.add(new Anchor("/actuator/vaadin/observability",
                "GET /actuator/vaadin/observability"));
        return details;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> insightsOf(
            Map<String, Object> payload) {
        return payload.get("insights") instanceof List<?> list
                ? (List<Map<String, Object>>) list
                : List.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> evidenceOf(Map<String, Object> insight) {
        return insight.get("evidence") instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
    }

    private static String text(@Nullable Object value) {
        return value == null ? "—" : value.toString();
    }

    private static long count(@Nullable Object value) {
        return value instanceof Number number ? number.longValue() : 0;
    }

    private static String simpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return lastDot >= 0 ? className.substring(lastDot + 1) : className;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
