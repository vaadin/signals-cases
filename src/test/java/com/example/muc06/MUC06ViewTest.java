package com.example.muc06;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = MUC06View.class)
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MUC06ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithAddButton() {
        navigate(MUC06View.class);

        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Add Task".equals(b.getText())));
    }

    @Test
    void initialTaskCount() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        // Statistics panel shows "Total: 4" (4 seed tasks)
        assertTrue($view(Span.class).all().stream().anyMatch(
                s -> s.getText() != null && s.getText().contains("Total: 4")));
    }

    @Test
    void addTaskIncreasesCount() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        Button addButton = $view(Button.class).all().stream()
                .filter(b -> "Add Task".equals(b.getText())).findFirst()
                .orElseThrow();
        test(addButton).click();
        runPendingSignalsTasks();

        assertTrue($view(Span.class).all().stream().anyMatch(
                s -> s.getText() != null && s.getText().contains("Total: 5")));
    }
}
