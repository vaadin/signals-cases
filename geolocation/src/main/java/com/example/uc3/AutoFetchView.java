package com.example.uc3;

import java.time.Duration;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.Geolocation;
import com.vaadin.flow.component.geolocation.GeolocationAvailability;
import com.vaadin.flow.component.geolocation.GeolocationError;
import com.vaadin.flow.component.geolocation.GeolocationOptions;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

/**
 * UC3 — Auto-fetch on view load, gated on permission.
 * <p>
 * If the user has previously granted permission for this origin, fetch the
 * location silently on attach (so returning visitors see location-sensitive
 * content immediately). First-time visitors see the page without a surprise
 * prompt — they trigger the request by clicking the explicit button.
 */
@Route(value = "uc3", layout = MainLayout.class)
@Menu(order = 3, title = "UC3 — Auto-fetch on attach")
public class AutoFetchView extends VerticalLayout {

    private final Span localContent = new Span("(no local content yet)");
    private final Span permissionHint = new Span();
    private final Button locate = new Button("Use my location");

    public AutoFetchView() {
        add(new H1("UC3 — Auto-fetch on view load, gated on permission"));
        add(new Paragraph(
                "If permission was already granted on a previous visit, "
                        + "this view fetches the location automatically. "
                        + "Otherwise the explicit button is used."));

        locate.addClickListener(e -> fetchAndPopulate());
        add(permissionHint, locate, localContent);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Geolocation geo = attachEvent.getUI().getGeolocation();
        GeolocationAvailability availability = geo.availabilitySignal().peek();
        permissionHint.setText("Availability on attach: " + availability);
        if (availability == GeolocationAvailability.GRANTED) {
            fetchAndPopulate();
        }
        // PROMPT / DENIED / UNKNOWN / UNSUPPORTED: do nothing automatic.
    }

    private void fetchAndPopulate() {
        GeolocationOptions opts = GeolocationOptions.builder()
                .timeout(Duration.ofSeconds(5))
                .maximumAge(Duration.ofMinutes(5)).build();
        getUI().orElseThrow().getGeolocation().get(opts, result -> {
            switch (result) {
            case GeolocationPosition pos -> localContent
                    .setText("Local content for lat=%.4f, lon=%.4f".formatted(
                            pos.coords().latitude(), pos.coords().longitude()));
            case GeolocationError err ->
                localContent.setText("Could not auto-fetch: " + err.message());
            }
        });
    }
}
