package com.example.uc6;

import java.time.Duration;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationError;
import com.vaadin.flow.component.geolocation.GeolocationOptions;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

/**
 * UC6 — Tuning precision, freshness and battery.
 * <p>
 * Shows four common {@link GeolocationOptions} profiles and runs each against
 * the same button-click entry point. The response time and the reported
 * accuracy will differ between profiles.
 */
@Route(value = "uc6", layout = MainLayout.class)
@Menu(order = 6, title = "UC6 — Tuning options")
public class OptionsView extends VerticalLayout {

    public OptionsView() {
        add(new H1("UC6 — Tuning precision, freshness and battery"));
        add(new Paragraph(
                "Each button runs the same request with different options. "
                        + "Compare the response time and reported accuracy."));

        add(profile("A — News site: cached city-level reading is fine",
                GeolocationOptions.builder().timeout(Duration.ofSeconds(5))
                        .maximumAge(Duration.ofMinutes(5)).build()));

        add(profile("B — Turn-by-turn navigation: highest accuracy",
                GeolocationOptions.builder().highAccuracy(true).build()));

        add(profile("C — Check-in: must be fresh and precise",
                GeolocationOptions.builder().highAccuracy(true)
                        .timeout(Duration.ofSeconds(10))
                        .maximumAge(Duration.ZERO).build()));

        add(profile("D — Address form: give up quickly", GeolocationOptions
                .builder().timeout(Duration.ofSeconds(3)).build()));
    }

    private Div profile(String label, GeolocationOptions options) {
        Div wrapper = new Div();
        wrapper.add(new H2(label));
        Span result = new Span("(not fetched yet)");
        Button run = new Button("Run", e -> {
            long started = System.currentTimeMillis();
            getUI().orElseThrow().getGeolocation().get(options, value -> {
                long elapsed = System.currentTimeMillis() - started;
                switch (value) {
                case GeolocationPosition pos ->
                    result.setText("%.5f, %.5f (±%.0f m) — %d ms".formatted(
                            pos.coords().latitude(), pos.coords().longitude(),
                            pos.coords().accuracy(), elapsed));
                case GeolocationError err -> result.setText(
                        "Error: " + err.message() + " (" + elapsed + " ms)");
                }
            });
        });
        wrapper.add(new HorizontalLayout(run, result));
        return wrapper;
    }
}
