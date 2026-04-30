package com.example.uc2;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.geolocation.GeolocationSimulator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationErrorCode;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ViewPackages(classes = TrackingView.class)
class TrackingViewTest extends BrowserlessTest {

    @Test
    void startTracking_pushSequenceOfPositions_growsHistoryAndUpdatesStatus() {
        GeolocationSimulator simulator = GeolocationSimulator
                .of(UI.getCurrent());
        navigate(TrackingView.class);

        // Click to start tracking — tracker.resume() is called
        Button toggle = $(Button.class).withText("Start tracking").single();
        test(toggle).click();

        assertEquals(1, simulator.activeTrackers().size(),
                "Exactly one tracker session should be active after starting");

        simulator.pushPosition(60.1699, 24.9384, 15.0);

        Span status = $(Span.class).withText("Update #1").single();
        assertEquals("Update #1", status.getText(),
                "Status should show update count 1");

        simulator.pushPosition(60.1700, 24.9385, 12.0);

        status = $(Span.class).withText("Update #2").single();
        assertEquals("Update #2", status.getText(),
                "Status should show update count 2");
    }

    @Test
    void pushError_displaysErrorStatus() {
        GeolocationSimulator simulator = GeolocationSimulator
                .of(UI.getCurrent());
        navigate(TrackingView.class);

        Button toggle = $(Button.class).withText("Start tracking").single();
        test(toggle).click();

        simulator.pushError(GeolocationErrorCode.POSITION_UNAVAILABLE,
                "position unavailable");

        Span status = $(Span.class).withTextContaining("Error").single();
        assertTrue(status.getText().contains("2"),
                "Status should show error code 2, was: " + status.getText());
    }

}
