package com.example.usecase26;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase26View.class)
@WithMockUser
class UseCase26ViewTest extends SpringBrowserlessTest {

    @SuppressWarnings("unchecked")
    private ComboBox<Country> countryCombo() {
        return (ComboBox<Country>) $view(ComboBox.class).all().stream()
                .filter(c -> "Country".equals(c.getLabel())).findFirst()
                .orElseThrow();
    }

    private void selectCountry(String country) {
        test(countryCombo()).selectItem(country);
        runPendingSignalsTasks();
    }

    @Test
    void noAddressFormShownInitially() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        // Only the country ComboBox should be visible, no address TextFields
        assertEquals(1, $view(ComboBox.class).all().size());
        assertEquals(0, $view(TextField.class).all().size());
    }

    @Test
    void selectingUSCreatesUSForm() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        selectCountry("US");

        // US fields should appear
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Street".equals(f.getLabel())));
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "ZIP".equals(f.getLabel())));
        // State ComboBox should appear
        assertTrue($view(ComboBox.class).all().stream()
                .anyMatch(c -> "State".equals(c.getLabel())));
    }

    @Test
    void selectingGermanyCreatesGermanForm() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        selectCountry("GERMANY");

        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Straße".equals(f.getLabel())));
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "PLZ".equals(f.getLabel())));
        assertTrue($view(ComboBox.class).all().stream()
                .anyMatch(c -> "Bundesland".equals(c.getLabel())));
    }

    @Test
    void selectingJapanCreatesJapanForm() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        selectCountry("JAPAN");

        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Postal Code".equals(f.getLabel())));
        assertTrue($view(ComboBox.class).all().stream()
                .anyMatch(c -> "Prefecture".equals(c.getLabel())));
    }

    @Test
    void switchingCountryHidesPreviousForm() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        selectCountry("US");
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Street".equals(f.getLabel())));

        selectCountry("GERMANY");
        // US fields hidden
        assertFalse($view(TextField.class).all().stream()
                .anyMatch(f -> "Street".equals(f.getLabel())));
        // German fields visible
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Straße".equals(f.getLabel())));
    }

    @Test
    void createOncePatternKeepsComponents() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        // US -> Germany -> US
        selectCountry("US");
        selectCountry("GERMANY");
        selectCountry("US");

        // Only 1 "Created US" log entry (create-once pattern)
        long usCreationCount = $view(Span.class).all().stream()
                .filter(s -> "Created US address form".equals(s.getText()))
                .count();
        assertEquals(1, usCreationCount);
    }

    @Test
    void destroyPatternRecreatesComponents() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        // Japan -> US -> Japan
        selectCountry("JAPAN");
        selectCountry("US");
        selectCountry("JAPAN");

        // 2 creation events + 1 destroy event for Japan
        long jpCreationCount = $view(Span.class).all().stream()
                .filter(s -> "Created Japan address form".equals(s.getText()))
                .count();
        assertEquals(2, jpCreationCount);

        long jpDestroyCount = $view(Span.class).all().stream()
                .filter(s -> "Destroyed Japan address form".equals(s.getText()))
                .count();
        assertEquals(1, jpDestroyCount);
    }

    @Test
    void creationLogUpdatesOnCountryChange() {
        navigate(UseCase26View.class);
        runPendingSignalsTasks();

        // Log starts empty (no Spans in log area besides headers)
        long logEntries = $view(Span.class).all().stream().filter(
                s -> s.getText() != null && s.getText().startsWith("Created"))
                .count();
        assertEquals(0, logEntries);

        // Select a country -> log gains an entry
        selectCountry("UK");

        logEntries = $view(Span.class).all().stream().filter(
                s -> s.getText() != null && s.getText().startsWith("Created"))
                .count();
        assertEquals(1, logEntries);
    }
}
