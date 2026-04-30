package com.example.uc3;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.geolocation.GeolocationSimulator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationAvailability;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ViewPackages(classes = AutoFetchView.class)
class AutoFetchViewTest extends BrowserlessTest {

    @Test
    void attachWithGrantedAvailability_autoFetchesPosition() {
        // Install controller and set availability BEFORE navigation so that
        // onAttach reads GRANTED and fires fetchAndPopulate() automatically.
        GeolocationSimulator controller = GeolocationSimulator
                .of(UI.getCurrent());
        controller.setAvailability(GeolocationAvailability.GRANTED);

        navigate(AutoFetchView.class);

        // onAttach saw GRANTED and called get() — resolve it now
        controller.respondWithPosition(60.1699, 24.9384, 50.0);

        Span localContent = $(Span.class).withTextContaining("Local content")
                .single();
        assertTrue(localContent.getText().contains("60.1699"),
                "Local content should show latitude, was: "
                        + localContent.getText());
    }

    @Test
    void attachWithDeniedAvailability_doesNotAutoFetch_showsButton() {
        GeolocationSimulator controller = GeolocationSimulator
                .of(UI.getCurrent());
        controller.setAvailability(GeolocationAvailability.DENIED);

        navigate(AutoFetchView.class);

        // No auto-fetch — the pending queue should be empty
        assertTrue(controller.lastRequest().isEmpty(),
                "No get() request should have been made for DENIED availability");

        // The explicit button should be present
        Button locate = $(Button.class).withText("Use my location").single();
        assertTrue(locate.isVisible(),
                "The explicit locate button should be visible for DENIED");

        // Verify the hint text reflects DENIED availability
        Span hint = $(Span.class)
                .withTextContaining("Availability on attach: DENIED").single();
        assertEquals("Availability on attach: DENIED", hint.getText());
    }
}
