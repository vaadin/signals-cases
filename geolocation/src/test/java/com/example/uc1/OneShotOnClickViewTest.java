package com.example.uc1;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.geolocation.GeolocationSimulator;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ViewPackages(classes = OneShotOnClickView.class)
class OneShotOnClickViewTest extends BrowserlessTest {

    @Test
    void clickingLocate_resolvesPosition_updatesResultSpan() {
        GeolocationSimulator geolocation = GeolocationSimulator.current();
        geolocation.grantPermission();
        geolocation.setLocation(60.1699, 24.9384, 25.0);
        navigate(OneShotOnClickView.class);

        Button locate = $(Button.class).withText("Use my location").single();
        test(locate).click();

        Span result = $(Span.class).withTextContaining("lat=").single();
        assertTrue(result.getText().contains("60.16990"),
                "Result span should show latitude, was: " + result.getText());
        assertTrue(result.getText().contains("24.93840"),
                "Result span should show longitude, was: " + result.getText());
    }

    @Test
    void clickingLocate_permissionDenied_showsErrorMessage() {
        GeolocationSimulator geolocation = GeolocationSimulator.current();
        geolocation.denyPermission();
        navigate(OneShotOnClickView.class);

        Button locate = $(Button.class).withText("Use my location").single();
        test(locate).click();

        Span result = $(Span.class).withTextContaining("Error").single();
        assertTrue(result.getText().contains("1"),
                "Result span should show error code 1, was: "
                        + result.getText());
    }
}
