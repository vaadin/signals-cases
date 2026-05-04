package com.example.uc2;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationError;
import com.vaadin.flow.component.geolocation.GeolocationOptions;
import com.vaadin.flow.component.geolocation.GeolocationPending;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.geolocation.GeolocationTracker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.map.Map;
import com.vaadin.flow.component.map.configuration.Coordinate;
import com.vaadin.flow.component.map.configuration.feature.LineStringFeature;
import com.vaadin.flow.component.map.configuration.feature.MarkerFeature;
import com.vaadin.flow.component.map.configuration.style.Stroke;
import com.vaadin.flow.component.map.configuration.style.Style;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC2 — Continuous tracking with reactive signal.
 * <p>
 * A Start/Stop toggle drives a single {@link GeolocationTracker} via
 * {@link GeolocationTracker#resume()} and {@link GeolocationTracker#stop()}.
 * Two effects subscribe to the tracker's signals: one appends new readings to
 * the grid and extends the path on the map, the other binds the toggle button's
 * label to {@link GeolocationTracker#activeSignal()}. Resuming does not clear
 * the history — the grid and the map line keep growing.
 */
@Route(value = "uc2", layout = MainLayout.class)
@PageTitle("UC2 — Continuous tracking with reactive signal")
@Menu(order = 2, title = "UC2 — Tracking")
public class TrackingView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final Span status = new Span("Idle");
    private final Button toggle = new Button("Start tracking");
    private final Grid<TrackPoint> history = new Grid<>(TrackPoint.class,
            false);
    private final List<TrackPoint> points = new ArrayList<>();

    private final Map map = new Map();
    private final List<Coordinate> pathCoords = new ArrayList<>();
    private @Nullable LineStringFeature pathLine;
    private @Nullable MarkerFeature startMarker;

    private final GeolocationTracker tracker;
    private int updateCount;

    public TrackingView() {
        add(new H1("UC2 — Continuous tracking with reactive signal"));
        add(new Paragraph("Start the tracker to receive live updates "
                + "whenever the browser reports a new position. Each "
                + "reading is appended to the history and extends the "
                + "path on the map. Resuming after Stop keeps the "
                + "accumulated history."));

        GeolocationOptions options = GeolocationOptions.builder()
                .highAccuracy(true).maximumAge(Duration.ZERO).build();
        tracker = UI.getCurrent().getGeolocation().track(this, options);
        tracker.stop();

        toggle.addClickListener(e -> toggleTracking());
        add(toggle, status);

        map.setHeight("400px");
        map.setWidthFull();
        add(map);

        add(new H2("Position history"));
        configureGrid();
        add(history);

        Signal.effect(this, () -> {
            switch (tracker.valueSignal().get()) {
            case GeolocationPending p ->
                status.setText("Waiting for first reading…");
            case GeolocationPosition pos -> appendPosition(pos);
            case GeolocationError err ->
                status.setText("Error " + err.code() + ": " + err.message());
            }
        });
        Signal.effect(this, () -> {
            boolean active = tracker.activeSignal().get();
            toggle.setText(active ? "Stop tracking"
                    : (updateCount > 0 ? "Resume tracking" : "Start tracking"));
            if (!active && updateCount > 0) {
                status.setText("Stopped after " + updateCount + " updates");
            }
        });
    }

    private void configureGrid() {
        history.addColumn(TrackPoint::index).setHeader("#").setAutoWidth(true);
        history.addColumn(p -> TIME.format(Instant.ofEpochMilli(p.timestamp())))
                .setHeader("Time").setAutoWidth(true);
        history.addColumn(p -> "%.5f".formatted(p.latitude()))
                .setHeader("Latitude").setAutoWidth(true);
        history.addColumn(p -> "%.5f".formatted(p.longitude()))
                .setHeader("Longitude").setAutoWidth(true);
        history.addColumn(p -> "%.0f m".formatted(p.accuracy()))
                .setHeader("Accuracy").setAutoWidth(true);
        history.setItems(points);
        history.setAllRowsVisible(true);
    }

    private void toggleTracking() {
        if (tracker.activeSignal().peek()) {
            tracker.stop();
        } else {
            tracker.resume();
        }
    }

    private void appendPosition(GeolocationPosition pos) {
        updateCount++;
        status.setText("Update #" + updateCount);
        points.add(new TrackPoint(updateCount, pos.timestamp(),
                pos.coords().latitude(), pos.coords().longitude(),
                pos.coords().accuracy()));
        history.getDataProvider().refreshAll();

        Coordinate c = new Coordinate(pos.coords().longitude(),
                pos.coords().latitude());
        pathCoords.add(c);
        map.setCenter(c);
        if (pathCoords.size() == 1) {
            map.setZoom(15);
            startMarker = new MarkerFeature(c);
            map.getFeatureLayer().addFeature(startMarker);
        } else if (pathLine == null) {
            pathLine = new LineStringFeature(
                    pathCoords.toArray(Coordinate[]::new));
            Style lineStyle = new Style();
            lineStyle.setStroke(new Stroke("#1976d2", 4));
            pathLine.setStyle(lineStyle);
            map.getFeatureLayer().addFeature(pathLine);
        } else {
            pathLine.setCoordinates(pathCoords.toArray(Coordinate[]::new));
        }
    }

    private record TrackPoint(int index, long timestamp, double latitude,
            double longitude, double accuracy) {
    }
}
