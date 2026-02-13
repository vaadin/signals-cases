package com.example.usecase08;

import jakarta.annotation.security.PermitAll;

import java.util.List;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.function.SignalMapper;
import com.vaadin.flow.signals.local.ValueSignal;

@Route(value = "use-case-08", layout = MainLayout.class)
@PageTitle("Use Case 8: Multi-Step Wizard with Validation")
@Menu(order = 8, title = "UC 8: Multi-Step Wizard")
@PermitAll
public class UseCase08View extends VerticalLayout {

    public UseCase08View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 8: Multi-Step Wizard with Validation");

        Paragraph description = new Paragraph(
                "This use case demonstrates a multi-step wizard form with progressive validation. "
                        + "Complete each step to unlock the next one. "
                        + "Each step has its own validation rules, and the Next button is enabled only when the current step is valid. "
                        + "Review your information in the final step before submitting.");

        // Create signals for current step and form data
        var currentStepSignal = new ValueSignal<>(Step.PERSONAL_INFO);
        var firstNameSignal = new ValueSignal<>("");
        var lastNameSignal = new ValueSignal<>("");
        var emailSignal = new ValueSignal<>("");
        var companyNameSignal = new ValueSignal<>("");
        var companySizeSignal = new ValueSignal<>((String) null);
        var industrySignal = new ValueSignal<>((String) null);
        var planSignal = new ValueSignal<>(Plan.STARTER);

        // Step 1: Personal Info
        VerticalLayout step1Layout = new VerticalLayout();
        step1Layout.add(new H3("Step 1: Personal Information"));

        TextField firstNameField = new TextField("First Name");
        firstNameField.bindValue(firstNameSignal, firstNameSignal::value);
        firstNameField.setRequired(true);
        firstNameField.setMinLength(2);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.bindValue(lastNameSignal, lastNameSignal::value);
        lastNameField.setRequired(true);
        lastNameField.setMinLength(2);

        EmailField emailField = new EmailField("Email");
        emailField.bindValue(emailSignal, emailSignal::value);
        emailField.setRequired(true);

        step1Layout.add(firstNameField, lastNameField, emailField);
        step1Layout.bindVisible(
                currentStepSignal.map(step -> step == Step.PERSONAL_INFO));

        // Step 2: Company Info
        VerticalLayout step2Layout = new VerticalLayout();
        step2Layout.add(new H3("Step 2: Company Information"));

        TextField companyNameField = new TextField("Company Name");
        companyNameField.bindValue(companyNameSignal, companyNameSignal::value);
        companyNameField.setRequired(true);

        ComboBox<String> companySizeSelect = new ComboBox<>("Company Size",
                List.of("1-10", "11-50", "51-200", "201-1000", "1000+"));
        companySizeSelect.bindValue(companySizeSignal, companySizeSignal::value);
        companySizeSelect.setRequired(true);

        ComboBox<String> industrySelect = new ComboBox<>("Industry",
                List.of("Technology", "Healthcare", "Finance", "Retail",
                        "Manufacturing", "Other"));
        industrySelect.bindValue(industrySignal, industrySignal::value);
        industrySelect.setRequired(true);

        step2Layout.add(companyNameField, companySizeSelect, industrySelect);
        step2Layout.bindVisible(
                currentStepSignal.map(step -> step == Step.COMPANY_INFO));

        // Step 3: Plan Selection
        VerticalLayout step3Layout = new VerticalLayout();
        step3Layout.add(new H3("Step 3: Select Your Plan"));

        ComboBox<Plan> planSelect = new ComboBox<>("Plan", Plan.values());
        planSelect.bindValue(planSignal, planSignal::value);
        planSelect.setRequired(true);

        Span planDescription = new Span();
        planDescription.bindText(planSignal.map(plan -> switch (plan) {
        case STARTER -> "Perfect for small teams - $29/month";
        case PROFESSIONAL -> "For growing businesses - $99/month";
        case ENTERPRISE -> "Custom solutions - Contact sales";
        }));

        step3Layout.add(planSelect, planDescription);
        step3Layout.bindVisible(
                currentStepSignal.map(step -> step == Step.PLAN_SELECTION));

        // Step 4: Review
        VerticalLayout step4Layout = new VerticalLayout();
        step4Layout.add(new H3("Step 4: Review Your Information"));

        Div reviewDiv = new Div();
        reviewDiv.bindText(Signal.computed(() -> "Name: "
                + firstNameSignal.value() + " " + lastNameSignal.value() + "\n"
                + "Email: " + emailSignal.value() + "\n" + "Company: "
                + companyNameSignal.value() + "\n" + "Size: "
                + companySizeSignal.value() + "\n" + "Industry: "
                + industrySignal.value() + "\n" + "Plan: "
                + planSignal.value()));

        step4Layout.add(reviewDiv);
        step4Layout.bindVisible(
                currentStepSignal.map(step -> step == Step.REVIEW));

        Signal<Boolean> step1ValidSignal = Signal.computed(() -> {
            boolean firstNameValid = firstNameSignal
                    .map(isValid(firstNameField)).value();
            boolean lastNameValid = lastNameSignal.map(isValid(lastNameField))
                    .value();
            boolean emailValid = emailSignal.map(isValid(emailField)).value();
            return firstNameValid && lastNameValid && emailValid;
        });

        Signal<Boolean> step2ValidSignal = Signal.computed(() -> {
            boolean companyNameValid = companyNameSignal
                    .map(isValid(companyNameField)).value();
            boolean companySizeValid = companySizeSignal
                    .map(isValid(companySizeSelect)).value();
            boolean industryValid = industrySignal.map(isValid(industrySelect))
                    .value();
            return companyNameValid && companySizeValid && industryValid;
        });

        Signal<Boolean> step3ValidSignal = Signal
                .computed(() -> planSignal.map(isValid(planSelect)).value());

        // Navigation buttons
        HorizontalLayout navigationLayout = new HorizontalLayout();

        Button previousButton = new Button("Previous", e -> {
            Step current = currentStepSignal.value();
            switch (current) {
            case COMPANY_INFO -> currentStepSignal.value(Step.PERSONAL_INFO);
            case PLAN_SELECTION -> currentStepSignal.value(Step.COMPANY_INFO);
            case REVIEW -> currentStepSignal.value(Step.PLAN_SELECTION);
            }
        });
        previousButton.bindVisible(
                currentStepSignal.map(step -> step != Step.PERSONAL_INFO));

        Button nextButton = new Button("Next", e -> {
            Step current = currentStepSignal.value();
            switch (current) {
            case PERSONAL_INFO -> currentStepSignal.value(Step.COMPANY_INFO);
            case COMPANY_INFO -> currentStepSignal.value(Step.PLAN_SELECTION);
            case PLAN_SELECTION -> currentStepSignal.value(Step.REVIEW);
            }
        });
        nextButton.bindVisible(
                currentStepSignal.map(step -> step != Step.REVIEW));
        nextButton.bindEnabled(Signal.computed(() -> {
            Step current = currentStepSignal.value();
            return switch (current) {
            case PERSONAL_INFO -> step1ValidSignal.value();
            case COMPANY_INFO -> step2ValidSignal.value();
            case PLAN_SELECTION -> step3ValidSignal.value();
            case REVIEW -> false;
            };
        }));

        Button submitButton = new Button("Submit", e -> {
            // Handle form submission
            System.out.println("Form submitted!");
        });
        submitButton.bindVisible(
                currentStepSignal.map(step -> step == Step.REVIEW));

        navigationLayout.add(previousButton, nextButton, submitButton);

        // Progress indicator
        Span progressIndicator = new Span();
        progressIndicator.bindText(currentStepSignal.map(step -> {
            int stepNumber = switch (step) {
            case PERSONAL_INFO -> 1;
            case COMPANY_INFO -> 2;
            case PLAN_SELECTION -> 3;
            case REVIEW -> 4;
            };
            return "Step " + stepNumber + " of 4";
        }));
        progressIndicator.getStyle().set("font-weight", "bold");

        add(title, description, progressIndicator, step1Layout, step2Layout,
                step3Layout, step4Layout, navigationLayout);
    }

    private <T> SignalMapper<T, Boolean> isValid(HasValidator<T> field) {
        return value -> {
            var validator = field.getDefaultValidator();
            return !validator.apply(value, null).isError();
        };
    }
}
