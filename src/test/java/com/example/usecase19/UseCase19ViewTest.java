package com.example.usecase19;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase19View.class)
@WithMockUser
class UseCase19ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSixDataCards() {
        navigate(UseCase19View.class);
        runPendingSignalsTasks();

        assertEquals(6, $view(Card.class).all().size());
    }

    @Test
    void loadButtonPresent() {
        navigate(UseCase19View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Load All Items".equals(b.getText())));
    }

    @Test
    void errorCheckboxPresent() {
        navigate(UseCase19View.class);

        assertTrue($view(Checkbox.class).all().stream()
                .anyMatch(c -> "Simulate Random Errors".equals(c.getLabel())));
    }
}
