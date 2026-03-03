package com.example.usecase03;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase03View.class)
@WithMockUser
class UseCase03ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithTabs() {
        navigate(UseCase03View.class);
        assertEquals(1, $view(Tabs.class).all().size());
    }

    @Test
    void viewRendersWithSliders() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Rectangle tab is shown by default with its sliders
        assertTrue($view(Slider.class).all().size() >= 5);
    }

    @Test
    void viewRendersWithResetButton() {
        navigate(UseCase03View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Reset to Defaults".equals(b.getText())));
    }

    @Test
    void viewRendersWithColorPickers() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Rectangle controls: Fill and Stroke ComboBoxes
        assertTrue($view(ComboBox.class).all().size() >= 2);
    }

    @Test
    void tabSwitchingTogglesControlVisibility() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Rectangle tab (index 0) is selected by default
        // Rectangle controls should have a "Width" slider visible
        assertTrue($view(Slider.class).all().stream()
                .anyMatch(s -> "Width".equals(s.getLabel())));

        // Switch to Star tab (index 1)
        Tabs tabs = $view(Tabs.class).single();
        Tab starTab = (Tab) tabs.getComponentAt(1);
        tabs.setSelectedTab(starTab);
        runPendingSignalsTasks();

        // Star controls should now show "Points" slider
        assertTrue($view(Slider.class).all().stream()
                .anyMatch(s -> "Points".equals(s.getLabel())));
    }

    @Test
    void resetButtonRevertsSliderToDefault() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Find the Width slider and change its value (default is 150)
        Slider widthSlider = $view(Slider.class).all().stream()
                .filter(s -> "Width".equals(s.getLabel())).findFirst()
                .orElseThrow();
        widthSlider.setValue(200.0);
        runPendingSignalsTasks();
        assertEquals(200.0, widthSlider.getValue());

        // Click Reset to Defaults
        Button resetButton = $view(Button.class).all().stream()
                .filter(b -> "Reset to Defaults".equals(b.getText()))
                .findFirst().orElseThrow();
        test(resetButton).click();
        runPendingSignalsTasks();

        // Width slider should revert to default value of 150
        assertEquals(150.0, widthSlider.getValue());
    }

    @Test
    void sliderValueChangeUpdatesSignal() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Find the X position slider and set a value
        Slider xSlider = $view(Slider.class).all().stream()
                .filter(s -> "X".equals(s.getLabel())).findFirst()
                .orElseThrow();
        xSlider.setValue(250.0);
        runPendingSignalsTasks();

        assertEquals(250.0, xSlider.getValue());
    }

    @Test
    void rectangleControlsVisibleByDefault() {
        navigate(UseCase03View.class);
        runPendingSignalsTasks();

        // Rectangle-specific sliders should be present
        assertTrue($view(Slider.class).all().stream()
                .anyMatch(s -> "Corner Radius".equals(s.getLabel())));
        assertTrue($view(Slider.class).all().stream()
                .anyMatch(s -> "Height".equals(s.getLabel())));
    }
}
