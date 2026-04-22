package com.example.uc7;

import java.time.Duration;
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
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

/**
 * UC7 — Capturing a location as part of a form.
 * <p>
 * Vertical pothole-reporting form. "Pin my location" runs a fresh request and
 * stores the result on a bean field; submit is only enabled when both a
 * description and a pinned location are present. Submitted reports appear in
 * the grid below.
 */
@Route(value = "uc7", layout = MainLayout.class)
@Menu(order = 7, title = "UC7 — Form field")
public class FormFieldView extends VerticalLayout {

    private static final double MAX_ACCURACY_METRES = 50;

    private final List<PotholeReport> reports = new ArrayList<>();

    private final TextField description = new TextField("Description");
    private final Button pin = new Button("Pin my location");
    private final Span pinLabel = new Span("No location pinned yet");
    private final Button submit = new Button("Report pothole");
    private final Grid<PotholeReport> grid = new Grid<>(PotholeReport.class,
            false);

    private @Nullable GeolocationPosition pinned;

    public FormFieldView() {
        add(new H1("UC7 — Location as part of a form"));
        add(new Paragraph(
                "Fill in a description and pin your current location. "
                        + "Submit is disabled until both are ready. "
                        + "Submitted reports appear in the grid below."));

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        description.setRequired(true);
        description.setWidthFull();
        description.addValueChangeListener(e -> refreshSubmitState());

        pin.addClickListener(e -> capture());
        submit.setEnabled(false);
        submit.addClickListener(e -> submit());

        form.add(description, pin, pinLabel, submit);
        add(form);

        add(new H2("Reported potholes"));
        configureGrid();
        add(grid);
    }

    private void configureGrid() {
        grid.addColumn(PotholeReport::description).setHeader("Description")
                .setAutoWidth(true);
        grid.addColumn(r -> "%.5f".formatted(r.latitude()))
                .setHeader("Latitude").setAutoWidth(true);
        grid.addColumn(r -> "%.5f".formatted(r.longitude()))
                .setHeader("Longitude").setAutoWidth(true);
        grid.addColumn(r -> "%.0f m".formatted(r.accuracy()))
                .setHeader("Accuracy").setAutoWidth(true);
        grid.setAllRowsVisible(true);
        grid.setItems(reports);
    }

    private void capture() {
        GeolocationOptions opts = GeolocationOptions.builder()
                .highAccuracy(true).timeout(Duration.ofSeconds(10))
                .maximumAge(Duration.ZERO).build();
        UI.getCurrent().getGeolocation().get(opts, value -> {
            switch (value) {
            case GeolocationPending p -> {
                // get() never delivers Pending; required for exhaustiveness
            }
            case GeolocationPosition pos -> {
                if (pos.coords().accuracy() > MAX_ACCURACY_METRES) {
                    Notification.show(
                            "Location too imprecise (±%.0f m), please try again."
                                    .formatted(pos.coords().accuracy()));
                    return;
                }
                pinned = pos;
                pinLabel.setText("Pinned at %.5f, %.5f (±%.0f m)".formatted(
                        pos.coords().latitude(), pos.coords().longitude(),
                        pos.coords().accuracy()));
                refreshSubmitState();
            }
            case GeolocationError err ->
                Notification.show("Could not pin location: " + err.message());
            }
        });
    }

    private void submit() {
        GeolocationPosition p = pinned;
        if (p == null) {
            return;
        }
        reports.add(
                new PotholeReport(description.getValue(), p.coords().latitude(),
                        p.coords().longitude(), p.coords().accuracy()));
        grid.getDataProvider().refreshAll();
        Notification.show("Report filed");

        description.clear();
        pinned = null;
        pinLabel.setText("No location pinned yet");
        refreshSubmitState();
    }

    private void refreshSubmitState() {
        submit.setEnabled(!description.isEmpty() && pinned != null);
    }

    private record PotholeReport(String description, double latitude,
            double longitude, double accuracy) {
    }
}
