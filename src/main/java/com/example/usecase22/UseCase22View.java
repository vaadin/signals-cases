package com.example.usecase22;

import jakarta.annotation.security.PermitAll;

import com.example.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.local.ValueSignal;

@Route(value = "use-case-22", layout = MainLayout.class)
@PageTitle("Use Case 22: Two-Way Mapped Signals")
@Menu(order = 22, title = "UC 22: Two-Way Mapped Signals")
@PermitAll
public class UseCase22View extends VerticalLayout {

    public UseCase22View() {
        setSpacing(true);
        setPadding(true);

        var title = new H2("Use Case 22: Two-Way Mapped Signals");

        var description = new Paragraph(
                "This use case demonstrates the WritableSignal.map(getter, merger) API for two-way computed signals. "
                        + "Each form field is bound to a mapped signal that reads from and writes to the parent record. "
                        + "Changes propagate bidirectionally: editing a field updates the record, and the record state is always consistent.");

        // Main person signal - the single source of truth
        var personSignal = new ValueSignal<>(new Person(
                "John", "Doe", "john.doe@example.com", 30,
                new Address("123 Main St", "Springfield", "12345", "USA")));

        // Create the form sections
        var formLayout = new HorizontalLayout();
        formLayout.setWidthFull();
        formLayout.setSpacing(true);

        // Personal info section
        var personalSection = createPersonalInfoSection(personSignal);

        // Address section with nested mapping
        var addressSection = createAddressSection(personSignal);

        formLayout.add(personalSection, addressSection);
        formLayout.setFlexGrow(1, personalSection, addressSection);

        // Live state display
        var stateSection = createStateDisplay(personSignal);

        // Pattern explanation
        var patternSection = createPatternExplanation();

        add(title, description, formLayout, stateSection, patternSection);
    }

    private VerticalLayout createPersonalInfoSection(WritableSignal<Person> personSignal) {
        var section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getStyle()
                .set("background-color", "#e3f2fd")
                .set("border-radius", "8px");

        var sectionTitle = new H3("Personal Information");
        sectionTitle.getStyle().set("margin-top", "0");

        // First name - direct field mapping
        var firstNameField = new TextField("First Name");
        firstNameField.setWidthFull();
        WritableSignal<String> firstNameSignal = personSignal.map(
                Person::firstName,
                Person::withFirstName
        );
        firstNameField.bindValue(firstNameSignal);

        // Last name - direct field mapping
        var lastNameField = new TextField("Last Name");
        lastNameField.setWidthFull();
        WritableSignal<String> lastNameSignal = personSignal.map(
                Person::lastName,
                Person::withLastName
        );
        lastNameField.bindValue(lastNameSignal);

        // Email - direct field mapping
        var emailField = new TextField("Email");
        emailField.setWidthFull();
        WritableSignal<String> emailSignal = personSignal.map(
                Person::email,
                Person::withEmail
        );
        emailField.bindValue(emailSignal);

        // Age - integer field mapping
        var ageField = new IntegerField("Age");
        ageField.setWidthFull();
        ageField.setMin(0);
        ageField.setMax(150);
        ageField.setStepButtonsVisible(true);
        WritableSignal<Integer> ageSignal = personSignal.map(
                Person::age,
                Person::withAge
        );
        ageField.bindValue(ageSignal);

        // Computed full name display
        var fullNameLabel = new Span();
        fullNameLabel.bindText(Signal.computed(() -> {
            Person p = personSignal.value();
            return "Full name: " + p.firstName() + " " + p.lastName();
        }));
        fullNameLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        section.add(sectionTitle, firstNameField, lastNameField, emailField, ageField, fullNameLabel);
        return section;
    }

    private VerticalLayout createAddressSection(WritableSignal<Person> personSignal) {
        var section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.getStyle()
                .set("background-color", "#fff3e0")
                .set("border-radius", "8px");

        var sectionTitle = new H3("Address (Nested Mapping)");
        sectionTitle.getStyle().set("margin-top", "0");

        // First, map Person -> Address
        WritableSignal<Address> addressSignal = personSignal.map(
                Person::address,
                Person::withAddress
        );

        // Then map Address -> individual fields (nested two-way mapping)
        var streetField = new TextField("Street");
        streetField.setWidthFull();
        WritableSignal<String> streetSignal = addressSignal.map(
                Address::street,
                Address::withStreet
        );
        streetField.bindValue(streetSignal);

        var cityField = new TextField("City");
        cityField.setWidthFull();
        WritableSignal<String> citySignal = addressSignal.map(
                Address::city,
                Address::withCity
        );
        cityField.bindValue(citySignal);

        var zipCodeField = new TextField("ZIP Code");
        zipCodeField.setWidthFull();
        WritableSignal<String> zipCodeSignal = addressSignal.map(
                Address::zipCode,
                Address::withZipCode
        );
        zipCodeField.bindValue(zipCodeSignal);

        var countryField = new TextField("Country");
        countryField.setWidthFull();
        WritableSignal<String> countrySignal = addressSignal.map(
                Address::country,
                Address::withCountry
        );
        countryField.bindValue(countrySignal);

        // Computed formatted address
        var formattedAddressLabel = new Span();
        formattedAddressLabel.bindText(Signal.computed(() -> {
            Address addr = addressSignal.value();
            return "Formatted: " + addr.street() + ", " + addr.city() + " " + addr.zipCode() + ", " + addr.country();
        }));
        formattedAddressLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)")
                .set("word-break", "break-word");

        section.add(sectionTitle, streetField, cityField, zipCodeField, countryField, formattedAddressLabel);
        return section;
    }

    private Div createStateDisplay(WritableSignal<Person> personSignal) {
        var stateBox = new Div();
        stateBox.getStyle()
                .set("background-color", "#e8f5e9")
                .set("padding", "1.5em")
                .set("border-radius", "8px")
                .set("margin-top", "1em");

        var stateTitle = new H3("Live State (Single Source of Truth)");
        stateTitle.getStyle().set("margin-top", "0");

        var stateDisplay = new Pre();
        stateDisplay.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "1em")
                .set("border-radius", "4px")
                .set("overflow-x", "auto")
                .set("font-size", "0.9em");
        stateDisplay.bindText(Signal.computed(() -> formatPersonAsJson(personSignal.value())));

        stateBox.add(stateTitle, stateDisplay);
        return stateBox;
    }

    private Div createPatternExplanation() {
        var infoBox = new Div();
        infoBox.getStyle()
                .set("background-color", "#e0f7fa")
                .set("padding", "1.5em")
                .set("border-radius", "8px")
                .set("margin-top", "1em");

        var patternTitle = new H3("Pattern: Two-Way Mapped Signals");
        patternTitle.getStyle().set("margin-top", "0");

        var codeExample = new Pre();
        codeExample.getStyle()
                .set("background-color", "#263238")
                .set("color", "#aed581")
                .set("padding", "1em")
                .set("border-radius", "4px")
                .set("overflow-x", "auto")
                .set("font-size", "0.85em");
        codeExample.setText("""
// Direct field mapping
WritableSignal<String> firstNameSignal = personSignal.map(
    Person::firstName,      // getter: Person -> String
    Person::withFirstName   // merger: (Person, String) -> Person
);
textField.bindValue(firstNameSignal);

// Nested mapping (Person -> Address -> City)
WritableSignal<Address> addressSignal = personSignal.map(
    Person::address,
    Person::withAddress
);
WritableSignal<String> citySignal = addressSignal.map(
    Address::city,
    Address::withCity
);
cityField.bindValue(citySignal);""");

        var explanation = new Paragraph(
                "The two-way mapped signal pattern uses WritableSignal.map(getter, merger) to create "
                        + "derived signals that can both read AND write. When the field changes, it calls the merger "
                        + "function to create a new immutable record, which updates the parent signal. "
                        + "This eliminates manual synchronization code and keeps your data model immutable.");

        var whenToUse = new H3("When to Use");
        whenToUse.getStyle().set("margin-bottom", "0.5em");

        var useCases = new Paragraph(
                "Use two-way mapped signals for simple field-to-record binding without validation. "
                        + "For complex validation requirements, continue using Vaadin Binder (see UC09).");

        infoBox.add(patternTitle, codeExample, explanation, whenToUse, useCases);
        return infoBox;
    }

    private String formatPersonAsJson(Person person) {
        return """
{
  "firstName": "%s",
  "lastName": "%s",
  "email": "%s",
  "age": %d,
  "address": {
    "street": "%s",
    "city": "%s",
    "zipCode": "%s",
    "country": "%s"
  }
}""".formatted(
                person.firstName(),
                person.lastName(),
                person.email(),
                person.age(),
                person.address().street(),
                person.address().city(),
                person.address().zipCode(),
                person.address().country()
        );
    }
}
