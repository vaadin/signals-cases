package com.example.usecase01;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase01View.class)
@WithMockUser
class UseCase01ViewTest extends SpringBrowserlessTest {

    @Test
    void formRendersWithAllComponents() {
        navigate(UseCase01View.class);

        assertEquals(1, $view(EmailField.class).all().size());
        assertEquals(2, $view(PasswordField.class).all().size());
        assertEquals(1, $view(Button.class).all().size());
    }

    @Test
    void submitButtonDisabledWhenFormIsEmpty() {
        navigate(UseCase01View.class);
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void submitButtonDisabledWithInvalidEmail() {
        navigate(UseCase01View.class);

        test($view(EmailField.class).single()).setValue("notanemail");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("password123");
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void submitButtonDisabledWithShortPassword() {
        navigate(UseCase01View.class);

        test($view(EmailField.class).single()).setValue("user@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("short");
        test($view(PasswordField.class).atIndex(2)).setValue("short");
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void submitButtonDisabledWhenPasswordsDontMatch() {
        navigate(UseCase01View.class);

        test($view(EmailField.class).single()).setValue("user@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("different123");
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void submitButtonEnabledWhenFormIsValid() {
        navigate(UseCase01View.class);

        test($view(EmailField.class).single()).setValue("user@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("password123");
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertTrue(submitButton.isEnabled());
    }

    @Test
    void buttonTextIsCreateAccountInitially() {
        navigate(UseCase01View.class);
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        assertEquals("Create Account", submitButton.getText());
    }

    @Test
    void clickingSubmitChangesButtonTextAndDisablesButton() {
        navigate(UseCase01View.class);

        test($view(EmailField.class).single()).setValue("user@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("password123");
        runPendingSignalsTasks();

        Button submitButton = $view(Button.class).single();
        test(submitButton).click();
        runPendingSignalsTasks();

        assertEquals("Creating...", submitButton.getText());
        assertFalse(submitButton.isEnabled());
    }

    @Test
    void buttonReEnabledAfterFillingValidFormAgain() {
        navigate(UseCase01View.class);

        // Fill valid form
        test($view(EmailField.class).single()).setValue("user@example.com");
        test($view(PasswordField.class).atIndex(1)).setValue("password123");
        test($view(PasswordField.class).atIndex(2)).setValue("password123");
        runPendingSignalsTasks();
        assertTrue($view(Button.class).single().isEnabled());

        // Break form (clear email)
        test($view(EmailField.class).single()).setValue("");
        runPendingSignalsTasks();
        assertFalse($view(Button.class).single().isEnabled());

        // Fix form again
        test($view(EmailField.class).single()).setValue("other@example.com");
        runPendingSignalsTasks();
        assertTrue($view(Button.class).single().isEnabled());
    }
}
