package com.example.usecase09;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase09View.class)
@WithMockUser
class UseCase09ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithAllFormFields() {
        navigate(UseCase09View.class);

        assertEquals(1, $view(TextField.class).all().size());
        assertEquals(1, $view(EmailField.class).all().size());
        assertEquals(2, $view(PasswordField.class).all().size());
        assertEquals(1, $view(ComboBox.class).all().size());
        assertEquals(1, $view(IntegerField.class).all().size());
        assertEquals(1, $view(Button.class).all().size());
    }

    @Test
    void submitButtonDisabledInitially() {
        navigate(UseCase09View.class);
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void submitButtonDisabledWithShortUsername() {
        navigate(UseCase09View.class);
        fillValidForm();

        // Override username with short value
        test($view(TextField.class).single()).setValue("ab");
        runPendingSignalsTasks();

        assertFalse($view(Button.class).single().isEnabled());
    }

    @Test
    void submitButtonDisabledWithInvalidEmail() {
        navigate(UseCase09View.class);
        fillValidForm();

        test($view(EmailField.class).single()).setValue("notanemail");
        runPendingSignalsTasks();

        assertFalse($view(Button.class).single().isEnabled());
    }

    @Test
    void submitButtonDisabledWithShortPassword() {
        navigate(UseCase09View.class);
        fillValidForm();

        test($view(PasswordField.class).atIndex(1)).setValue("short");
        test($view(PasswordField.class).atIndex(2)).setValue("short");
        runPendingSignalsTasks();

        assertFalse($view(Button.class).single().isEnabled());
    }

    @Test
    void submitButtonDisabledWhenPasswordsDontMatch() {
        navigate(UseCase09View.class);
        fillValidForm();

        test($view(PasswordField.class).atIndex(2)).setValue("different1");
        runPendingSignalsTasks();

        assertFalse($view(Button.class).single().isEnabled());
    }

    @SuppressWarnings("unchecked")
    @Test
    void submitButtonDisabledWhenAgeInvalidForBusiness() {
        navigate(UseCase09View.class);
        fillValidForm();

        // Switch to BUSINESS account
        ComboBox<AccountType> accountType = $view(ComboBox.class).single();
        test(accountType).selectItem("BUSINESS");
        // Age 15 is valid for PERSONAL but not BUSINESS
        test($view(IntegerField.class).single()).setValue(15);
        runPendingSignalsTasks();

        assertFalse($view(Button.class).single().isEnabled());
    }

    @Test
    void submitButtonEnabledWithValidForm() {
        navigate(UseCase09View.class);
        fillValidForm();

        assertTrue($view(Button.class).single().isEnabled());
    }

    @Test
    void statusLabelShowsValidWhenFormValid() {
        navigate(UseCase09View.class);
        fillValidForm();

        Span statusLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().contains("Form is valid")
                        || s.getText().contains("Please complete"))
                .findFirst().orElseThrow();
        assertEquals("Form is valid - Ready to submit",
                statusLabel.getText());
    }

    @Test
    void statusLabelShowsInvalidWhenFormInvalid() {
        navigate(UseCase09View.class);
        runPendingSignalsTasks();

        Span statusLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText().contains("Form is valid")
                        || s.getText().contains("Please complete"))
                .findFirst().orElseThrow();
        assertEquals("Please complete all required fields correctly",
                statusLabel.getText());
    }

    @SuppressWarnings("unchecked")
    private void fillValidForm() {
        test($view(TextField.class).single()).setValue("johndoe");
        test($view(EmailField.class).single()).setValue("john@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("password123");
        ComboBox<AccountType> accountType = $view(ComboBox.class).single();
        test(accountType).selectItem("PERSONAL");
        test($view(IntegerField.class).single()).setValue(25);
        runPendingSignalsTasks();
    }
}
