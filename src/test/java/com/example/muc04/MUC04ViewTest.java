package com.example.muc04;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = MUC04View.class)
@WithMockUser
class MUC04ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithThreeEditableFields() {
        navigate(MUC04View.class);

        // Company Name, Address, Phone Number
        assertEquals(3, $view(TextField.class).all().size());
    }

    @Test
    void fieldsAreEnabled() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // All fields should be enabled (no other user editing)
        $view(TextField.class).all()
                .forEach(f -> assertTrue(f.isEnabled()));
    }

    @Test
    void saveButtonPresent() {
        navigate(MUC04View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Save Changes".equals(b.getText())));
    }

    @Test
    void fieldLabelsCorrect() {
        navigate(MUC04View.class);

        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Company Name".equals(f.getLabel())));
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Address".equals(f.getLabel())));
        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Phone Number".equals(f.getLabel())));
    }
}
