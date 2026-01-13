package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-01", layout = MainLayout.class)
@PageTitle("Use Case 1: User Profile Settings Panel")
@Menu(order = 10, title = "UC 1: User Profile Settings")
public class UseCase01View extends VerticalLayout {

    enum LogLevel { DEBUG, INFO, WARN, ERROR }

    public UseCase01View() {
        // Create signal for advanced mode state
        WritableSignal<Boolean> advancedModeSignal = new ValueSignal<>(false);

        // Advanced mode checkbox
        Checkbox advancedModeCheckbox = new Checkbox("Enable Advanced Mode");
        MissingAPI.bindValue(advancedModeCheckbox, advancedModeSignal);

        // Advanced settings panel
        VerticalLayout advancedPanel = new VerticalLayout();
        advancedPanel.add(
            new TextField("Custom Theme Path"),
            new Select<>("Log Level", LogLevel.values()),
            new Checkbox("Enable Debug Features")
        );

        // Bind panel visibility to the signal
        advancedPanel.bindVisible(advancedModeSignal);

        add(advancedModeCheckbox, advancedPanel);
    }
}
