package com.example.usecase22;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase22View.class)
@WithMockUser
class UseCase22ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithPersonalAndAddressFields() {
        navigate(UseCase22View.class);

        // Personal: First Name, Last Name, Email + Address: Street, City, ZIP, Country = 7
        assertTrue($view(TextField.class).all().size() >= 7);
        assertEquals(1, $view(IntegerField.class).all().size());
    }

    @Test
    void initialValuesPopulated() {
        navigate(UseCase22View.class);
        runPendingSignalsTasks();

        TextField firstNameField = $view(TextField.class).all().stream()
                .filter(f -> "First Name".equals(f.getLabel())).findFirst()
                .orElseThrow();
        TextField lastNameField = $view(TextField.class).all().stream()
                .filter(f -> "Last Name".equals(f.getLabel())).findFirst()
                .orElseThrow();

        assertEquals("John", firstNameField.getValue());
        assertEquals("Doe", lastNameField.getValue());
    }

    @Test
    void changingFirstNameUpdatesFullName() {
        navigate(UseCase22View.class);

        TextField firstNameField = $view(TextField.class).all().stream()
                .filter(f -> "First Name".equals(f.getLabel())).findFirst()
                .orElseThrow();
        test(firstNameField).setValue("Jane");
        runPendingSignalsTasks();

        Span fullNameLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().startsWith("Full name:"))
                .findFirst().orElseThrow();
        assertEquals("Full name: Jane Doe", fullNameLabel.getText());
    }

    @Test
    void changingLastNameUpdatesFullName() {
        navigate(UseCase22View.class);

        TextField lastNameField = $view(TextField.class).all().stream()
                .filter(f -> "Last Name".equals(f.getLabel())).findFirst()
                .orElseThrow();
        test(lastNameField).setValue("Smith");
        runPendingSignalsTasks();

        Span fullNameLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().startsWith("Full name:"))
                .findFirst().orElseThrow();
        assertEquals("Full name: John Smith", fullNameLabel.getText());
    }

    @Test
    void changingCityUpdatesFormattedAddress() {
        navigate(UseCase22View.class);

        TextField cityField = $view(TextField.class).all().stream()
                .filter(f -> "City".equals(f.getLabel())).findFirst()
                .orElseThrow();
        test(cityField).setValue("Portland");
        runPendingSignalsTasks();

        Span addressLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().startsWith("Formatted:"))
                .findFirst().orElseThrow();
        assertTrue(addressLabel.getText().contains("Portland"));
    }

    @Test
    void changingStreetUpdatesFormattedAddress() {
        navigate(UseCase22View.class);

        TextField streetField = $view(TextField.class).all().stream()
                .filter(f -> "Street".equals(f.getLabel())).findFirst()
                .orElseThrow();
        test(streetField).setValue("456 Oak Ave");
        runPendingSignalsTasks();

        Span addressLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().startsWith("Formatted:"))
                .findFirst().orElseThrow();
        assertTrue(addressLabel.getText().contains("456 Oak Ave"));
    }

    @Test
    void ageFieldUpdatesState() {
        navigate(UseCase22View.class);

        IntegerField ageField = $view(IntegerField.class).single();
        test(ageField).setValue(25);
        runPendingSignalsTasks();

        Pre stateDisplay = $view(Pre.class).atIndex(1);
        assertTrue(stateDisplay.getText().contains("\"age\": 25"));
    }

    @Test
    void stateDisplayShowsAllFields() {
        navigate(UseCase22View.class);
        runPendingSignalsTasks();

        Pre stateDisplay = $view(Pre.class).atIndex(1);
        String text = stateDisplay.getText();
        assertTrue(text.contains("\"firstName\": \"John\""));
        assertTrue(text.contains("\"lastName\": \"Doe\""));
        assertTrue(text.contains("\"email\": \"john.doe@example.com\""));
        assertTrue(text.contains("\"age\": 30"));
        assertTrue(text.contains("\"street\": \"123 Main St\""));
        assertTrue(text.contains("\"city\": \"Springfield\""));
    }
}
