package com.example.usecase01;

import jakarta.annotation.security.PermitAll;

import java.util.concurrent.CompletableFuture;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

@Route(value = "use-case-01", layout = MainLayout.class)
@PageTitle("Use Case 1: Dynamic Button State")
@Menu(order = 1, title = "UC 1: Dynamic Button State")
@PermitAll
public class UseCase01View extends VerticalLayout {

    /**
     * Simple data class for form binding
     */
    public static class AccountData {
        private String email = "";
        private String password = "";
        private String confirmPassword = "";

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    public UseCase01View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 1: Dynamic Button State");

        Paragraph description = new Paragraph(
                "This use case demonstrates Binder integration with signal-based dynamic button states. "
                        + "The form uses Vaadin Binder for proper field binding and validation. "
                        + "The submit button is enabled only when Binder validation passes. "
                        + "The button text reactively changes during form submission using signals.");

        // Create binder and bean
        Binder<AccountData> binder = new Binder<>(AccountData.class);
        AccountData data = new AccountData();

        // Signal for submission state (drives button text and theme)
        ValueSignal<SubmissionState> submissionStateSignal = new ValueSignal<>(
                SubmissionState.IDLE);

        // Form fields
        EmailField emailField = new EmailField("Email");
        emailField.setHelperText("Valid email address required");

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setHelperText("Minimum 8 characters required");

        PasswordField confirmField = new PasswordField("Confirm Password");
        confirmField.setHelperText("Must match password");

        // Binder field bindings with validators
        binder.forField(emailField)
                .withValidator(new EmailValidator(
                        "Please enter a valid email address"))
                .bind(AccountData::getEmail, AccountData::setEmail);

        var passwordBinding = binder.forField(passwordField)
                .withValidator(value -> value != null && value.length() >= 8,
                        "Password must be at least 8 characters")
                .bind(AccountData::getPassword, AccountData::setPassword);

        // Cross-field validation for password confirmation
        binder.forField(confirmField).withValidator(
                value -> value != null
                        && value.equals(passwordBinding.valueSignal().get()),
                "Passwords do not match").bind(AccountData::getConfirmPassword,
                        AccountData::setConfirmPassword);

        binder.setBean(data);

        // Submit button with signal-based text and theme
        Button submitButton = new Button();

        // Bind enabled state using Binder's validationStatusSignal combined
        // with submission state
        Signal<Boolean> isFormValidSignal = binder.validationStatusSignal()
                .map(BinderValidationStatus::isOk);
        submitButton.bindEnabled(Signal.computed(() -> isFormValidSignal.get()
                && submissionStateSignal.get() != SubmissionState.SUBMITTING));

        // Bind button text based on submission state
        submitButton
                .bindText(submissionStateSignal.map(state -> switch (state) {
                case IDLE -> "Create Account";
                case SUBMITTING -> "Creating...";
                case SUCCESS -> "Success!";
                case ERROR -> "Retry";
                }));

        // Bind theme variant
        submitButton.bindThemeVariant(ButtonVariant.LUMO_SUCCESS,
                submissionStateSignal.map(SubmissionState.SUCCESS::equals));
        submitButton.bindThemeVariant(ButtonVariant.LUMO_PRIMARY,
                submissionStateSignal
                        .map(state -> state != SubmissionState.SUCCESS));

        submitButton.addClickListener(e -> {
            if (!binder.isValid()) {
                return;
            }

            submissionStateSignal.set(SubmissionState.SUBMITTING);

            // Simulate async submission
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    submissionStateSignal.set(SubmissionState.SUCCESS);

                    // Reset form using Binder
                    binder.setBean(new AccountData());

                    // Reset submission state back to IDLE
                    submissionStateSignal.set(SubmissionState.IDLE);

                    // Show success notification (imperative UI - needs
                    // ui.access)
                    getUI().ifPresent(ui -> ui.access(() -> {
                        Notification notification = Notification
                                .show("Account created successfully!");
                        notification.addThemeVariants(
                                NotificationVariant.LUMO_SUCCESS);
                        notification.setDuration(3000);
                    }));
                } catch (Exception ex) {
                    submissionStateSignal.set(SubmissionState.ERROR);
                }
            });
        });

        add(title, description, emailField, passwordField, confirmField,
                submitButton);
    }
}
