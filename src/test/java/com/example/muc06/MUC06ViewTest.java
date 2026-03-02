package com.example.muc06;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MUC06Signals muc06Signals;

    private String getStatText(String prefix) {
        return $view(Span.class).all().stream()
                .filter(s -> s.getText() != null
                        && s.getText().startsWith(prefix))
                .map(Span::getText).findFirst().orElse("");
    }

    @Test
    void initialTaskCount() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        assertTrue(getStatText("Total:").contains("4"),
                "Should have 4 seed tasks");
    }

    @Test
    void userAddsTaskViaButton() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        Button addButton = $view(Button.class).all().stream()
                .filter(b -> "Add Task".equals(b.getText())).findFirst()
                .orElseThrow();
        test(addButton).click();
        runPendingSignalsTasks();

        assertTrue(getStatText("Total:").contains("5"),
                "Count should increase to 5 after adding");
    }

    @Test
    void otherUserAddingTaskUpdatesView() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        assertTrue(getStatText("Total:").contains("4"));

        // Simulate User B adding a task via the shared signal
        muc06Signals.getTasksSignal()
                .insertLast(new MUC06Signals.Task("task-userB",
                        "User B's task", false, LocalDate.now()));
        runPendingSignalsTasks();

        // User A's view should reflect the new task
        assertTrue(getStatText("Total:").contains("5"),
                "User A should see task added by User B");
    }

    @Test
    void bothUsersAddTasksConcurrently() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        // User A adds a task via the UI
        Button addButton = $view(Button.class).all().stream()
                .filter(b -> "Add Task".equals(b.getText())).findFirst()
                .orElseThrow();
        test(addButton).click();
        runPendingSignalsTasks();

        // User B adds a task via the shared signal
        muc06Signals.getTasksSignal()
                .insertLast(new MUC06Signals.Task("task-userB",
                        "User B's task", false, LocalDate.now()));
        runPendingSignalsTasks();

        // Both tasks should be visible
        assertTrue(getStatText("Total:").contains("6"),
                "Should show 6 tasks (4 seed + 1 from A + 1 from B)");
    }

    @Test
    void otherUserCompletingTaskUpdatesStatistics() {
        navigate(MUC06View.class);
        runPendingSignalsTasks();

        // Initially 1 completed (task-2 from seed data)
        assertTrue(getStatText("Completed:").contains("1"));

        // Simulate User B completing another task via the shared signal
        var tasks = muc06Signals.getTasksSignal().peek();
        // Find an incomplete task and mark it complete
        for (var taskSignal : tasks) {
            MUC06Signals.Task task = taskSignal.get();
            if (!task.completed()) {
                taskSignal.set(new MUC06Signals.Task(task.id(), task.title(),
                        true, task.dueDate()));
                break;
            }
        }
        runPendingSignalsTasks();

        // User A should see the updated completed count
        assertTrue(getStatText("Completed:").contains("2"),
                "Completed count should increase when other user completes a task");
    }
}
