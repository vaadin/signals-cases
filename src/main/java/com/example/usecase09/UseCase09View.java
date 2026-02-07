package com.example.usecase09;

import jakarta.annotation.security.PermitAll;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

@Route(value = "use-case-09", layout = MainLayout.class)
@PageTitle("Use Case 9: Form with Binder Integration and Signal Validation")
@Menu(order = 9, title = "UC 9: Binder Integration")
@PermitAll
public class UseCase09View extends VerticalLayout {

    public UseCase09View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2(
                "Use Case 9: Form with Binder Integration and Signal Validation");

        Paragraph description = new Paragraph(
                "This use case demonstrates Vaadin Binder integration with reactive signal-based validation. "
                        + "The form validates in real-time as you type, showing specific error messages for each field. "
                        + "Note how the age requirement changes based on account type (Personal: 13+, Business: 18+). "
                        + "The submit button is enabled only when all fields pass validation.");

        // Create binder
        Binder<UserRegistration> binder = new Binder<>(UserRegistration.class);

        // Create form fields
        TextField usernameField = new TextField("Username");
        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Password");
        PasswordField confirmPasswordField = new PasswordField(
                "Confirm Password");
        ComboBox<AccountType> accountTypeSelect = new ComboBox<>("Account Type",
                AccountType.values());
        IntegerField ageField = new IntegerField("Age");

        ValueSignal<AccountType> accountTypeSignal = new ValueSignal<>(
                AccountType.PERSONAL);
        ValueSignal<Integer> ageSignal = new ValueSignal<>(0);

        accountTypeSelect.bindValue(accountTypeSignal);
        ageField.bindValue(ageSignal);

        Signal<Boolean> ageValidSignal = Signal.computed(() -> {
            Integer age = ageSignal.value();
            AccountType accountType = accountTypeSignal.value();
            if (age == null)
                return false;
            // Business accounts require age >= 18
            if (accountType == AccountType.BUSINESS) {
                return age >= 18;
            }
            // Personal accounts require age >= 13
            return age >= 13;
        });

        // binder bindings
        binder.forField(usernameField)
                .withValidator(value -> value != null && value.length() >= 3,
                        "Username must be at least 3 characters")
                .bind("username");
        binder.forField(emailField)
                .withValidator(
                        value -> value != null && value.contains("@")
                                && value.contains("."),
                        "Please enter a valid email address")
                .bind("email");

        Binder.Binding<UserRegistration, String> pwBinding = binder
                .forField(passwordField)
                .withValidator(value -> value != null && value.length() >= 8,
                        "Password must be at least 8 characters")
                .bind("password");
        // cross-field validation using Binder.Binding.value()
        binder.forField(confirmPasswordField).withValidator(
                value -> value != null && value.equals(pwBinding.value()),
                "Passwords do not match").bind("confirmPassword");

        binder.forField(accountTypeSelect).bind("accountType");
        // another cross-field validation, this one uses signals directly
        binder.forField(ageField)
                .withValidator(value -> ageValidSignal.value(), value -> {
                    AccountType accountType = accountTypeSignal.value();
                    if (accountType == AccountType.BUSINESS) {
                        return "Business accounts require age 18 or older";
                    }
                    return "Personal accounts require age 13 or older";
                }).bind("age");

        binder.readBean(
                new UserRegistration("", "", "", "", AccountType.PERSONAL, 0));

        // Submit button
        Button submitButton = new Button("Register", e -> {
            // Handle registration
            if (binder.isValid()) {
                UserRegistration userRegistration = new UserRegistration();
                binder.writeBeanIfValid(userRegistration);
                Notification.show("Registration submitted!");

                binder.readBean(new UserRegistration("", "", "", "",
                        AccountType.PERSONAL, 0));
            } else {
                Notification.show(
                        "Please fix validation errors before submitting.");
            }
        });
        submitButton.bindEnabled(
                binder.getValidationStatus().map(BinderValidationStatus::isOk));

        // Form status
        Div statusDiv = new Div();
        Span statusLabel = new Span();
        statusLabel.bindText(binder.getValidationStatus()
                .map(status -> status.isOk() ? "Form is valid - Ready to submit"
                        : "Please complete all required fields correctly"));
        statusLabel.getStyle().bind("color", binder.getValidationStatus()
                .map(status -> status.isOk() ? "green" : "orange"));
        statusLabel.getStyle().bind("font-weight", binder.getValidationStatus()
                .map(status -> status.isOk() ? "bold" : "normal"));
        statusDiv.add(statusLabel);

        add(title, description, usernameField, emailField, passwordField,
                confirmPasswordField, accountTypeSelect, ageField, statusDiv,
                submitButton);
    }
}
