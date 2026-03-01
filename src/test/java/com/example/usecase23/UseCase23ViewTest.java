package com.example.usecase23;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase23View.class)
@WithMockUser
class UseCase23ViewTest extends SpringBrowserlessTest {

    @SuppressWarnings("deprecation")
    @Test
    void viewRendersWithBoard() {
        navigate(UseCase23View.class);

        assertEquals(1, $view(Board.class).all().size());
    }

    @Test
    void highlightCardsPresent() {
        navigate(UseCase23View.class);
        runPendingSignalsTasks();

        // 4 highlight cards with H2 titles: "Current users", "View events",
        // "Conversion rate", "Custom metric"
        assertTrue($view(H2.class).all().stream()
                .anyMatch(h -> "Current users".equals(h.getText())));
        assertTrue($view(H2.class).all().stream()
                .anyMatch(h -> "View events".equals(h.getText())));
    }

    @Test
    void serviceHealthGridRendered() {
        navigate(UseCase23View.class);
        runPendingSignalsTasks();

        // Grid for service health should be present
        assertTrue($view(Grid.class).all().size() >= 1);
    }
}
