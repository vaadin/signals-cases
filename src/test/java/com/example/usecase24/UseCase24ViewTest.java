package com.example.usecase24;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase24View.class)
@WithMockUser
class UseCase24ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithFilters() {
        navigate(UseCase24View.class);

        // Type filter and Status filter
        assertEquals(2, $view(ComboBox.class).all().size());
    }

    @Test
    void viewRendersWithActionButtons() {
        navigate(UseCase24View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Mark All Read".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Clear All".equals(b.getText())));
    }

    @Test
    void viewRendersWithAddButtons() {
        navigate(UseCase24View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Info".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Warning".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Error".equals(b.getText())));
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Success".equals(b.getText())));
    }

    @Test
    void initialNotificationCount() {
        navigate(UseCase24View.class);
        runPendingSignalsTasks();

        // 8 seed notifications, 5 unread
        Span countLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().contains("Showing"))
                .findFirst().orElseThrow();
        assertTrue(countLabel.getText().contains("8 notification"));
        assertTrue(countLabel.getText().contains("5 unread"));
    }

    @Test
    void unreadBadgePresent() {
        navigate(UseCase24View.class);
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream().anyMatch(
                s -> s.getText() != null && s.getText().contains("unread")));
    }

    @Test
    void addInfoIncreasesCount() {
        navigate(UseCase24View.class);
        runPendingSignalsTasks();

        Button addInfo = $view(Button.class).all().stream()
                .filter(b -> "Add Info".equals(b.getText())).findFirst()
                .orElseThrow();
        test(addInfo).click();
        runPendingSignalsTasks();

        Span countLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().contains("Showing"))
                .findFirst().orElseThrow();
        assertTrue(countLabel.getText().contains("9 notification"));
    }

    @Test
    void clearAllRemovesNotifications() {
        navigate(UseCase24View.class);
        runPendingSignalsTasks();

        Button clearAll = $view(Button.class).all().stream()
                .filter(b -> "Clear All".equals(b.getText())).findFirst()
                .orElseThrow();
        test(clearAll).click();
        runPendingSignalsTasks();

        Span countLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().contains("Showing"))
                .findFirst().orElseThrow();
        assertTrue(countLabel.getText().contains("0 notification"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void filterByTypeNarrowsResults() {
        navigate(UseCase24View.class);
        runPendingSignalsTasks();

        ComboBox<String> typeFilter = (ComboBox<String>) $view(ComboBox.class)
                .all().stream()
                .filter(c -> "Type".equals(c.getLabel())).findFirst()
                .orElseThrow();
        test(typeFilter).selectItem("ERROR");
        runPendingSignalsTasks();

        Span countLabel = $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().contains("Showing"))
                .findFirst().orElseThrow();
        assertTrue(countLabel.getText().contains("2 notification"));
    }
}
