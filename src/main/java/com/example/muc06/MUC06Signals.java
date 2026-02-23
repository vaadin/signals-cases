package com.example.muc06;

import jakarta.annotation.PostConstruct;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.vaadin.flow.signals.shared.SharedListSignal;

/**
 * Application-scoped signals for MUC06: Shared Task List
 */
@Component
public class MUC06Signals {

    public record Task(String id, String title, boolean completed,
            java.time.LocalDate dueDate) {
    }

    private final SharedListSignal<Task> tasksSignal = new SharedListSignal<>(
            Task.class);

    public SharedListSignal<Task> getTasksSignal() {
        return tasksSignal;
    }

    @PostConstruct
    public void initializeSampleTasks() {
        if (tasksSignal.peek().isEmpty()) {
            tasksSignal.insertLast(new Task("task-1", "Review pull requests",
                    false, LocalDate.now()));
            tasksSignal.insertLast(new Task("task-2", "Update documentation",
                    true, LocalDate.now().plusDays(1)));
            tasksSignal.insertLast(new Task("task-3", "Fix bug #123", false,
                    LocalDate.now().plusDays(2)));
            tasksSignal.insertLast(new Task("task-4", "Prepare demo", false,
                    LocalDate.now().plusWeeks(1)));
        }
    }
}
