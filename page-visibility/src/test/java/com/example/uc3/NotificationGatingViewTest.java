package com.example.uc3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.button.Button;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = NotificationGatingView.class)
class NotificationGatingViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSubscribeAndSendButtons() {
        navigate(NotificationGatingView.class);

        assertTrue($view(Button.class).all().stream().anyMatch(
                b -> "Enable browser notifications".equals(b.getText())));
        assertTrue($view(Button.class).all().stream().anyMatch(b -> "Send me a notification in 5 seconds"
                .equals(b.getText())));
    }
}
