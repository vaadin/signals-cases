package com.example.muc01;

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
@ViewPackages(classes = MUC01View.class)
@WithMockUser
class MUC01ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithMessageInput() {
        navigate(MUC01View.class);

        assertEquals(1, $view(TextField.class).all().size());
    }

    @Test
    void viewRendersWithButtons() {
        navigate(MUC01View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Send Message".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Clear All Messages".equals(b.getText())));
    }
}
