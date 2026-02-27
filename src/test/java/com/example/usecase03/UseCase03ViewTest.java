package com.example.usecase03;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.slider.Slider;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
