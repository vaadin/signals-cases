package com.example.uc7;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.geolocation.GeolocationSimulator;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ViewPackages(classes = FormFieldView.class)
class FormFieldViewTest extends BrowserlessTest {

    @Test
    void pinningLocationWithGoodAccuracy_enablesSubmit_andFilesReport() {
        GeolocationSimulator controller = GeolocationSimulator
                .of(UI.getCurrent());
        navigate(FormFieldView.class);

        // Submit starts disabled
        Button submit = $(Button.class).withText("Report pothole").single();
        assertFalse(submit.isEnabled(),
                "Submit should be disabled before description and location are set");

        // Fill description
        TextField description = $(TextField.class)
                .withCaption("Description").single();
        test(description).setValue("Cracked pavement near bus stop");

        // Pin location — accuracy 20 m (well within the 50 m threshold)
        Button pin = $(Button.class).withText("Pin my location").single();
        test(pin).click();
        controller.respondWithPosition(51.5074, -0.1278, 20.0);

        // Submit should now be enabled
        assertTrue(submit.isEnabled(),
                "Submit should be enabled after both description and good-accuracy location are set");

        // Pin label should reflect the captured position
        Span pinLabel = $(Span.class).withTextContaining("Pinned at").single();
        assertTrue(pinLabel.getText().contains("51.50740"),
                "Pin label should show latitude, was: " + pinLabel.getText());

        // Click submit — report should be filed and form reset
        test(submit).click();

        // After submit the pin label resets
        Span resetLabel = $(Span.class)
                .withText("No location pinned yet").single();
        assertTrue(resetLabel.isVisible(),
                "Pin label should reset to default after submission");

        // Submit button should be disabled again after reset
        assertFalse(submit.isEnabled(),
                "Submit should be disabled again after successful submission");
    }

    @Test
    void pinningLocationWithPoorAccuracy_keepsSubmitDisabled() {
        GeolocationSimulator controller = GeolocationSimulator
                .of(UI.getCurrent());
        navigate(FormFieldView.class);

        TextField description = $(TextField.class)
                .withCaption("Description").single();
        test(description).setValue("Pothole on main road");

        // Poor accuracy — 100 m exceeds the 50 m threshold
        Button pin = $(Button.class).withText("Pin my location").single();
        test(pin).click();
        controller.respondWithPosition(51.5074, -0.1278, 100.0);

        // Submit should remain disabled since the accuracy was too poor
        Button submit = $(Button.class).withText("Report pothole").single();
        assertFalse(submit.isEnabled(),
                "Submit should remain disabled when accuracy exceeds the threshold");

        // Pin label should still say no location pinned
        Span pinLabel = $(Span.class).withText("No location pinned yet").single();
        assertTrue(pinLabel.isVisible(),
                "Pin label should stay at default when accuracy is too poor");
    }
}
