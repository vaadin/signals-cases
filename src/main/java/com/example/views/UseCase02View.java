package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-02", layout = MainLayout.class)
@PageTitle("Use Case 2: Form Field Synchronization")
@Menu(order = 11, title = "UC 2: Form Field Synchronization")
public class UseCase02View extends FormLayout {

    public UseCase02View() {
        // Create signals for input fields
        WritableSignal<String> firstNameSignal = new ValueSignal<>("");
        WritableSignal<String> lastNameSignal = new ValueSignal<>("");

        // Input fields with two-way binding
        TextField firstNameField = new TextField("First Name");
        MissingAPI.bindValue(firstNameField, firstNameSignal);

        TextField lastNameField = new TextField("Last Name");
        MissingAPI.bindValue(lastNameField, lastNameSignal);

        // Computed signal for username
        Signal<String> usernameSignal = Signal.computed(() -> {
            String first = firstNameSignal.value();
            String last = lastNameSignal.value();
            if (first.isEmpty() || last.isEmpty()) {
                return "";
            }
            return (first + "." + last).toLowerCase();
        });

        // Multiple displays bound to the same computed signal
        H3 welcomeHeader = new H3();
        MissingAPI.bindText(welcomeHeader, usernameSignal.map(u -> "Welcome, " + u));

        Span profilePreview = new Span();
        MissingAPI.bindText(profilePreview, usernameSignal.map(u -> "Your public profile: " + u));

        Span confirmation = new Span();
        MissingAPI.bindText(confirmation, usernameSignal.map(u -> "Account will be created for: " + u));

        add(firstNameField, lastNameField,
            welcomeHeader, profilePreview, confirmation);
    }
}
