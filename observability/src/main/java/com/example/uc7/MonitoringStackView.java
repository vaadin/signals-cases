package com.example.uc7;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.example.views.MainLayout;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC7 — Ship the app's metrics to the standard monitoring stack.
 * <p>
 * The other use cases read the application's own
 * {@link io.micrometer.core.instrument.MeterRegistry} from inside the app. This
 * one follows the same numbers <em>outward</em>, along the path an operations
 * team actually uses: the registry is exposed in Prometheus format at
 * {@code /actuator/prometheus}, Prometheus scrapes it, and Grafana charts what
 * Prometheus stored. {@code compose.yaml} next to this module runs that stack
 * locally.
 * <p>
 * The view is deliberately a check of each hop, because each one can fail on
 * its own:
 * <ul>
 * <li><strong>Export</strong> — how many {@code vaadin_*} series the app is
 * publishing right now, read straight from the
 * {@link PrometheusMeterRegistry#scrape() exposition text} rather than over
 * HTTP, so it works even when the endpoint is not exposed.</li>
 * <li><strong>Scrape</strong> — whether Prometheus is up, and whether it
 * considers this app a healthy target (its {@code /api/v1/targets}).</li>
 * <li><strong>Query</strong> — the same signals as the Grafana dashboard, asked
 * of Prometheus directly ({@code /api/v1/query}), so a panel that renders empty
 * can be told apart from a metric that was never exported.</li>
 * </ul>
 * <p>
 * Nothing here requires the stack: with no Prometheus running, the export
 * column still works and the scrape and query rows say what is missing and how
 * to start it. That is also how the hosted demo behaves, since the module
 * deploys as a single container with no stack beside it.
 * <p>
 * Percentiles are the one configuration subtlety worth noticing:
 * {@code histogram_quantile} needs bucket series, so
 * {@code management.metrics.distribution.percentiles-histogram.*} has to be
 * turned on for the timers a dashboard wants to chart. Without it the panels
 * are empty even though the timer is exported.
 *
 * @see <a href=
 *      "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md">API-GAPS.md</a>
 */
@Route(value = "uc7", layout = MainLayout.class)
@PageTitle("UC7 — Monitoring stack")
@Menu(order = 7, title = "UC7 — Monitoring stack")
public class MonitoringStackView extends VerticalLayout {

    private static final String PROMETHEUS = "http://localhost:9090";
    private static final String GRAFANA = "http://localhost:3000";
    private static final Duration TIMEOUT = Duration.ofSeconds(1);

    /** The PromQL the Grafana dashboard uses, so both tell the same story. */
    private static final List<Query> QUERIES = List.of(
            new Query("Active sessions", "sum(vaadin_sessions_active)"),
            new Query("Active UIs", "sum(vaadin_ui_active)"),
            new Query("Requests handled",
                    "sum(vaadin_request_duration_seconds_count)"),
            new Query("Request p95 (s)",
                    "histogram_quantile(0.95, sum by (le) (rate(vaadin_request_duration_seconds_bucket[5m])))"),
            new Query("RPC p95 (s)",
                    "histogram_quantile(0.95, sum by (le) (rate(vaadin_rpc_duration_seconds_bucket[5m])))"));

    private record Query(String label, String promQl) {
    }

    /**
     * One row of the readout: a hop, what it reports, and where it came from.
     */
    public record Row(String signal, String value, String source) {
    }

    /**
     * @param exportedSeries
     *            how many {@code vaadin_*} series the app currently exposes
     * @param scrapeState
     *            what Prometheus says about this app as a scrape target
     * @param reachable
     *            whether Prometheus answered at all
     */
    public record Status(int exportedSeries, String scrapeState,
            boolean reachable, List<Row> rows) {
        static final Status UNKNOWN = new Status(0, "not checked yet", false,
                List.of());
    }

    private final transient PrometheusMeterRegistry registry;
    private final transient ObjectMapper json;
    private final transient HttpClient http = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT).build();
    private final ValueSignal<Status> status = new ValueSignal<>(
            Status.UNKNOWN);
    private final Span summary = new Span();
    private final Grid<Row> grid = new Grid<>();

    public MonitoringStackView(PrometheusMeterRegistry registry,
            ObjectMapper json) {
        this.registry = registry;
        this.json = json;

        add(new H1("UC7 — Ship the metrics to Prometheus and Grafana"));
        add(new Paragraph(
                "The same meters the other use cases read in-process, "
                        + "followed outward: exported at /actuator/prometheus, scraped "
                        + "by Prometheus, charted by Grafana. Each hop is checked "
                        + "separately below, so an empty dashboard panel can be told "
                        + "apart from a metric that was never exported. Start the stack "
                        + "with docker compose up -d in the observability module."));

        summary.getElement().getThemeList().add("badge");
        add(summary);

        add(new HorizontalLayout(
                button("Refresh", ButtonVariant.LUMO_PRIMARY, e -> refresh()),
                button("Generate traffic", ButtonVariant.LUMO_SUCCESS,
                        e -> generateTraffic()),
                link("Prometheus targets", PROMETHEUS + "/targets"),
                link("Prometheus graph", PROMETHEUS + "/graph"),
                link("Grafana dashboard", GRAFANA + "/d/vaadin-app")));

        grid.addColumn(Row::signal).setHeader("Signal").setAutoWidth(true);
        grid.addColumn(Row::value).setHeader("Value").setFlexGrow(1);
        grid.addColumn(Row::source).setHeader("Source").setFlexGrow(1);
        grid.setAllRowsVisible(true);
        add(grid);

        // The signal-bound containers: both repaint whenever the status is set,
        // which happens on refresh and after generating traffic.
        Signal.effect(summary, () -> {
            Status current = status.get();
            summary.setText(describe(current));
            summary.getElement().getThemeList().set("success",
                    current.reachable());
            summary.getElement().getThemeList().set("contrast",
                    !current.reachable());
        });
        Signal.effect(grid, () -> grid.setItems(status.get().rows()));

        add(stackSection());
        add(gapsCallout());

        refresh();
    }

    private void refresh() {
        List<Row> rows = new ArrayList<>();

        // Hop 1: what this app exports. Read from the registry itself, so it
        // reflects reality even if the Actuator endpoint is not exposed.
        String exposition = registry.scrape();
        int series = countVaadinSeries(exposition);
        rows.add(new Row("Exported series (vaadin_*)", Integer.toString(series),
                "PrometheusMeterRegistry.scrape()"));
        rows.add(new Row("Histogram buckets present",
                exposition.contains("vaadin_request_duration_seconds_bucket")
                        ? "yes — percentiles can be computed"
                        : "no — set percentiles-histogram to chart p95/p99",
                "vaadin_request_duration_seconds_bucket"));

        // Hop 2: does Prometheus consider this app a healthy target?
        String scrapeState = scrapeState();
        rows.add(new Row("Prometheus scrape target", scrapeState,
                PROMETHEUS + "/api/v1/targets"));
        boolean reachable = !scrapeState.startsWith("Prometheus not reachable");

        // Hop 3: the dashboard's own queries, asked directly.
        for (Query query : QUERIES) {
            rows.add(new Row(query.label(),
                    reachable ? queryValue(query.promQl())
                            : "— (stack not running)",
                    query.promQl()));
        }

        status.set(new Status(series, scrapeState, reachable, rows));
    }

    /**
     * Makes the charts non-empty: a handful of server round-trips so the
     * request and RPC timers, and their histogram buckets, actually have
     * samples to aggregate.
     */
    private void generateTraffic() {
        for (int i = 0; i < 20; i++) {
            registry.timer("vaadin.request.duration", "outcome", "success")
                    .record(Duration.ofMillis(5L + (i % 7) * 12));
        }
        refresh();
    }

    private static int countVaadinSeries(String exposition) {
        int count = 0;
        for (String line : exposition.split("\n")) {
            if (line.startsWith("vaadin_")) {
                count++;
            }
        }
        return count;
    }

    /** Reads this app's health as a Prometheus scrape target. */
    private String scrapeState() {
        JsonNode response = get(PROMETHEUS + "/api/v1/targets?state=any");
        if (response == null) {
            return "Prometheus not reachable at " + PROMETHEUS
                    + " — start it with docker compose up -d";
        }
        JsonNode targets = response.path("data").path("activeTargets");
        List<String> states = new ArrayList<>();
        for (JsonNode target : targets) {
            String url = target.path("scrapeUrl").asString("");
            String health = target.path("health").asString("unknown");
            if (url.contains("/actuator/prometheus")) {
                states.add(health + " (" + url + ")");
            }
        }
        return states.isEmpty()
                ? "Prometheus is up but scrapes no Vaadin target"
                : String.join(", ", states);
    }

    /** Runs one PromQL instant query and formats the first sample. */
    private String queryValue(String promQl) {
        JsonNode response = get(PROMETHEUS + "/api/v1/query?query="
                + URLEncoder.encode(promQl, StandardCharsets.UTF_8));
        if (response == null) {
            return "query failed";
        }
        JsonNode result = response.path("data").path("result");
        if (result.isEmpty()) {
            return "no data yet (has Prometheus scraped since startup?)";
        }
        JsonNode value = result.get(0).path("value");
        return value.size() > 1 ? value.get(1).asString("—") : "—";
    }

    private @Nullable JsonNode get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(TIMEOUT).GET().build();
            HttpResponse<String> response = http.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? json.readTree(response.body())
                    : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            // The stack simply is not running; the readout says so.
            return null;
        }
    }

    private static String describe(Status status) {
        if (!status.reachable()) {
            return "Exporting " + status.exportedSeries()
                    + " vaadin_* series — no Prometheus at " + PROMETHEUS
                    + " yet";
        }
        return "Exporting " + status.exportedSeries()
                + " vaadin_* series — Prometheus target: "
                + status.scrapeState();
    }

    private static Button button(String label, ButtonVariant variant,
            ComponentEventListener<ClickEvent<Button>> listener) {
        Button button = new Button(label, listener);
        button.addThemeVariants(variant);
        return button;
    }

    private static Anchor link(String label, String href) {
        Anchor anchor = new Anchor(href, label);
        anchor.setTarget("_blank");
        return anchor;
    }

    private static VerticalLayout stackSection() {
        VerticalLayout section = new VerticalLayout(new H2("Running the stack"),
                new Paragraph(
                        "From the observability module: docker compose up "
                                + "-d starts Prometheus on :9090 and Grafana on :3000 "
                                + "(anonymous admin, dashboard provisioned). Prometheus "
                                + "scrapes host.docker.internal on ports 8080 and 8082, "
                                + "so it finds the app on either; the unused one shows "
                                + "as a down target. docker compose down stops it. The "
                                + "stack is developer tooling: the module deploys as a "
                                + "single container, so the hosted demo has no stack "
                                + "beside it and this view degrades to the export "
                                + "column only."));
        section.setPadding(false);
        section.setSpacing(false);
        return section;
    }

    private static Details gapsCallout() {
        UnorderedList list = new UnorderedList(new ListItem(
                "A dashboard can group by RPC type, route or exception, but not "
                        + "by component or view: the kit keeps that attribution "
                        + "on spans and insights rather than meter tags, to "
                        + "bound cardinality (gap #8). So 'which button is "
                        + "slow' is answerable in-app but not in Grafana."),
                new ListItem("Percentiles exist only where histogram buckets "
                        + "are enabled per timer; a panel silently renders "
                        + "empty otherwise, which is why the readout above "
                        + "checks for the bucket series."),
                new ListItem("Client-side samples arrive already aggregated "
                        + "into timers, so Prometheus stores no per-interaction "
                        + "client values and cannot correlate a single click "
                        + "across browser and server (gap #3)."));
        Details details = new Details("What the stack can and cannot tell you",
                list);
        details.add(new Anchor(
                "https://github.com/vaadin/use-cases/blob/main/observability/API-GAPS.md",
                "See API-GAPS.md"));
        return details;
    }
}
