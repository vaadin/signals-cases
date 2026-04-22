package com.example.uc4;

import com.example.views.MainLayout;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationAvailability;
import com.vaadin.flow.component.geolocation.GeolocationError;
import com.vaadin.flow.component.geolocation.GeolocationErrorCode;
import com.vaadin.flow.component.geolocation.GeolocationPending;
import com.vaadin.flow.component.geolocation.GeolocationPosition;
import com.vaadin.flow.component.geolocation.GeolocationResult;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

/**
 * UC4 — Handling denial, failure and unavailability.
 * <p>
 * Each scenario is in its own card so you can tell what you are testing. The
 * two simulated scenarios bypass the browser entirely so you can exercise every
 * branch without fiddling with site permissions; the "real browser" card calls
 * the actual API.
 */
@Route(value = "uc4", layout = MainLayout.class)
@Menu(order = 4, title = "UC4 — Denial & unavailability")
public class DenialView extends VerticalLayout {

    public DenialView() {
        add(new H1("UC4 — Denial, failure and unavailability"));
        add(new Paragraph(
                "The app adapts to two different kinds of failure: an "
                        + "availability that says the feature is not "
                        + "usable at all (or usable but denied), and an "
                        + "error returned from a single location request."));

        add(new AvailabilityCard());
        add(new RequestOutcomeCard());
        add(new RealBrowserCard());
    }

    // ------------------------------------------------------------------
    // Shared styling
    // ------------------------------------------------------------------

    private static VerticalLayout card() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-l)");
        card.getStyle().set("padding", "var(--lumo-space-m)");
        card.getStyle().set("margin-bottom", "var(--lumo-space-m)");
        return card;
    }

    // ------------------------------------------------------------------
    // Card 1 — availability-driven rendering
    // ------------------------------------------------------------------

    /**
     * Demonstrates how the app renders based on the current
     * {@link GeolocationAvailability} reported by the browser.
     */
    private static class AvailabilityCard extends VerticalLayout {

        private final Button locate = new Button("Find stores near me");
        private final TextField postcode = new TextField("Postcode");
        private final Span hint = new Span();

        AvailabilityCard() {
            VerticalLayout card = card();
            card.add(new H2("Availability → rendering"));
            card.add(new Paragraph("Pick what the browser reports as the "
                    + "current availability. The preview below shows how "
                    + "the store-finder view would render in that case."));

            Select<GeolocationAvailability> choose = new Select<>();
            choose.setLabel("Browser-reported availability");
            choose.setItems(GeolocationAvailability.values());
            choose.addValueChangeListener(e -> applyAvailability(e.getValue()));
            card.add(choose);

            card.add(new H2("Preview"));
            postcode.setVisible(false);
            card.add(hint, locate, postcode);

            // Default to PROMPT so the preview shows the "normal" branch.
            choose.setValue(GeolocationAvailability.PROMPT);

            add(card);
        }

        private void applyAvailability(GeolocationAvailability a) {
            locate.setVisible(true);
            postcode.setVisible(false);
            switch (a) {
            case GRANTED -> hint.setText(
                    "Permission already granted — clicking the button is silent.");
            case PROMPT -> hint.setText(
                    "Permission will be requested the first time the button is clicked.");
            case UNKNOWN -> hint.setText(
                    "Browser does not report the state — wait for explicit user action.");
            case DENIED -> {
                postcode.setVisible(true);
                hint.setText(
                        "Location is blocked for this site. Click the padlock "
                                + "in the address bar to re-enable, or enter a "
                                + "postcode below.");
            }
            case UNSUPPORTED -> {
                locate.setVisible(false);
                postcode.setVisible(true);
                hint.setText("Geolocation is not available in this context. "
                        + "Enter a postcode to find nearby stores.");
            }
            }
        }

    }

    // ------------------------------------------------------------------
    // Card 2 — request-error handling
    // ------------------------------------------------------------------

    /**
     * Demonstrates how the app reacts to each possible outcome of a single
     * {@code get()} request. The selector picks what the client would return;
     * the button runs the handler.
     */
    private static class RequestOutcomeCard extends VerticalLayout {

        private enum Outcome {
            SUCCESS("A position (59.437, 24.7535)"),
            PERMISSION_DENIED("Error — permission denied"),
            POSITION_UNAVAILABLE("Error — position unavailable"),
            TIMEOUT("Error — request timed out"),
            UNKNOWN_CODE("Error — unknown future code (99)");

            private final String label;

            Outcome(String label) {
                this.label = label;
            }

            @Override
            public String toString() {
                return label;
            }
        }

        private final Span output = new Span();

        RequestOutcomeCard() {
            VerticalLayout card = card();
            card.add(new H2("Request outcome → rendering"));
            card.add(new Paragraph(
                    "Pick the result the browser should return, then click "
                            + "Run request. The handler writes the "
                            + "user-facing message below."));

            Select<Outcome> choose = new Select<>();
            choose.setLabel("What the request returns");
            choose.setItems(Outcome.values());
            choose.setValue(Outcome.PERMISSION_DENIED);
            card.add(choose);

            Button run = new Button("Run request",
                    e -> handle(synthesize(choose.getValue())));
            card.add(run);

            card.add(new H2("Message shown to the user"));
            card.add(output);

            add(card);
        }

        private GeolocationResult synthesize(Outcome outcome) {
            return switch (outcome) {
            case SUCCESS -> new GeolocationPosition(
                    new com.vaadin.flow.component.geolocation.GeolocationCoordinates(
                            59.437, 24.7535, 10.0, null, null, null, null),
                    System.currentTimeMillis());
            case PERMISSION_DENIED -> new GeolocationError(
                    GeolocationErrorCode.PERMISSION_DENIED.code(),
                    "Simulated: user denied geolocation");
            case POSITION_UNAVAILABLE -> new GeolocationError(
                    GeolocationErrorCode.POSITION_UNAVAILABLE.code(),
                    "Simulated: position unavailable");
            case TIMEOUT ->
                new GeolocationError(GeolocationErrorCode.TIMEOUT.code(),
                        "Simulated: request timed out");
            case UNKNOWN_CODE ->
                new GeolocationError(99, "Simulated: unknown future code");
            };
        }

        private void handle(GeolocationResult value) {
            switch (value) {
            case GeolocationPending p -> {
                // get() never delivers Pending; required for exhaustiveness
            }
            case GeolocationPosition pos ->
                output.setText("Stores near lat=%.4f, lon=%.4f".formatted(
                        pos.coords().latitude(), pos.coords().longitude()));
            case GeolocationError err ->
                output.setText(switch (err.errorCode()) {
                case PERMISSION_DENIED ->
                    "Location not shared. Please enter a postcode.";
                case POSITION_UNAVAILABLE ->
                    "We couldn't determine your location.";
                case TIMEOUT -> "Location request timed out. Please try again.";
                case UNKNOWN -> err.message();
                });
            }
        }

    }

    // ------------------------------------------------------------------
    // Card 3 — real browser request
    // ------------------------------------------------------------------

    /**
     * Runs the real {@code Geolocation.get()} against the real browser so you
     * can verify end-to-end behaviour once the simulations look right.
     */
    private static class RealBrowserCard extends VerticalLayout {

        private final Span availabilityLabel = new Span();
        private final Span output = new Span("(no request run yet)");

        RealBrowserCard() {
            VerticalLayout card = card();
            card.add(new H2("Real browser request"));
            card.add(new Paragraph(
                    "Calls the real API. The outcome depends on your "
                            + "browser's current permission state."));

            Div row = new Div();
            row.add(availabilityLabel);
            card.add(row);

            Button locate = new Button("Use my location", e -> runReal());
            card.add(locate);

            card.add(new H2("Response"));
            card.add(output);

            add(card);
        }

        @Override
        protected void onAttach(com.vaadin.flow.component.AttachEvent e) {
            super.onAttach(e);
            availabilityLabel.setText("Current availability: "
                    + e.getUI().getGeolocation().getAvailability());
        }

        private void runReal() {
            UI.getCurrent().getGeolocation().get(value -> {
                switch (value) {
                case GeolocationPending p -> {
                    // get() never delivers Pending; required for exhaustiveness
                }
                case GeolocationPosition pos ->
                    output.setText("Position: lat=%.5f, lon=%.5f (±%.0f m)"
                            .formatted(pos.coords().latitude(),
                                    pos.coords().longitude(),
                                    pos.coords().accuracy()));
                case GeolocationError err -> output.setText(
                        "Error (" + err.errorCode() + "): " + err.message());
                }
            });
        }

    }
}
