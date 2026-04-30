package com.example.uc4;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.geolocation.GeolocationSimulator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.geolocation.GeolocationAvailability;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ViewPackages(classes = DenialView.class)
class DenialViewTest extends BrowserlessTest {

    @Test
    void availabilityCard_setsDeniedAvailability_showsPostcodeField() {
        GeolocationSimulator.of(UI.getCurrent());
        navigate(DenialView.class);

        // The availability card has a Select<GeolocationAvailability>; it is
        // the first Select in the view. Set it to DENIED to trigger
        // applyAvailability(DENIED).
        @SuppressWarnings("unchecked")
        Select<GeolocationAvailability> select = $(Select.class)
                .withCaption("Browser-reported availability").single();
        test(select).selectItem("DENIED");

        // applyAvailability(DENIED) makes the postcode field visible
        TextField postcode = $(TextField.class)
                .withCaption("Postcode").single();
        assertTrue(postcode.isVisible(),
                "Postcode field should be visible when availability is DENIED");

        // The hint span should mention the denied state
        Span hint = $(Span.class)
                .withTextContaining("Location is blocked").single();
        assertTrue(hint.isVisible(),
                "Hint span should be visible when availability is DENIED");
    }
}
