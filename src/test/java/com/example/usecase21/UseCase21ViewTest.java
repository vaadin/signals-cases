package com.example.usecase21;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase21View.class)
@WithMockUser
class UseCase21ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithFormFields() {
        navigate(UseCase21View.class);

        assertEquals(1, $view(TextField.class).all().size());
        assertEquals(1, $view(EmailField.class).all().size());
    }

    @Test
    void viewRendersWithButtons() {
        navigate(UseCase21View.class);
        runPendingSignalsTasks();

        // Submit and Cancel buttons (text from i18n signals)
        assertEquals(2, $view(Button.class).all().size());
    }
}
