package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "use-case-10", layout = MainLayout.class)
@PageTitle("Use Case 10: Dynamic Task List with Real-time Updates")
@Menu(order = 41, title = "UC 10: Task List")
public class UseCase10View extends VerticalLayout {

    record Task(String id, String title, boolean completed) {}

    public UseCase10View() {
        // Create signal for task list
        WritableSignal<List<Task>> tasksSignal = new ValueSignal<>(new ArrayList<>(List.of(
            new Task("1", "Complete project proposal", false),
            new Task("2", "Review code changes", true),
            new Task("3", "Update documentation", false)
        )));

        // Computed signals for task statistics
        Signal<Integer> totalCountSignal = Signal.computed(() -> tasksSignal.value().size());

        Signal<Integer> completedCountSignal = Signal.computed(() ->
            (int) tasksSignal.value().stream().filter(Task::completed).count()
        );

        Signal<Integer> pendingCountSignal = Signal.computed(() ->
            totalCountSignal.value() - completedCountSignal.value()
        );

        Signal<Double> progressSignal = Signal.computed(() -> {
            int total = totalCountSignal.value();
            if (total == 0) return 0.0;
            return (double) completedCountSignal.value() / total;
        });

        // Add task form
        TextField newTaskField = new TextField("New Task");
        newTaskField.setPlaceholder("Enter task title");

        Button addButton = new Button("Add Task", e -> {
            String title = newTaskField.getValue();
            if (!title.isEmpty()) {
                List<Task> currentTasks = new ArrayList<>(tasksSignal.value());
                currentTasks.add(new Task(String.valueOf(System.currentTimeMillis()), title, false));
                tasksSignal.value(currentTasks);
                newTaskField.clear();
            }
        });

        HorizontalLayout addTaskLayout = new HorizontalLayout(newTaskField, addButton);

        // Statistics display
        Div statsDiv = new Div();
        Span totalLabel = new Span();
        MissingAPI.bindText(totalLabel, totalCountSignal.map(count -> "Total: " + count));

        Span completedLabel = new Span();
        MissingAPI.bindText(completedLabel, completedCountSignal.map(count -> "Completed: " + count));

        Span pendingLabel = new Span();
        MissingAPI.bindText(pendingLabel, pendingCountSignal.map(count -> "Pending: " + count));

        ProgressBar progressBar = new ProgressBar();
        // Bind progress bar value manually since it's read-only
        com.vaadin.flow.component.UI ui = com.vaadin.flow.component.UI.getCurrent();
        Signal.effect(() -> {
            double progress = progressSignal.value();
            ui.access(() -> progressBar.setValue(progress));
        });

        statsDiv.add(new H3("Task Statistics"), totalLabel, completedLabel, pendingLabel, progressBar);

        // Task lists
        VerticalLayout pendingTasksLayout = new VerticalLayout();
        pendingTasksLayout.add(new H3("Pending Tasks"));
        MissingAPI.bindChildren(pendingTasksLayout, tasksSignal.map(tasks ->
            tasks.stream()
                .filter(task -> !task.completed())
                .map(task -> createTaskItem(task, tasksSignal))
                .toList()
        ));

        VerticalLayout completedTasksLayout = new VerticalLayout();
        completedTasksLayout.add(new H3("Completed Tasks"));
        MissingAPI.bindChildren(completedTasksLayout, tasksSignal.map(tasks ->
            tasks.stream()
                .filter(Task::completed)
                .map(task -> createTaskItem(task, tasksSignal))
                .toList()
        ));

        add(addTaskLayout, statsDiv, pendingTasksLayout, completedTasksLayout);
    }

    private HorizontalLayout createTaskItem(Task task, WritableSignal<List<Task>> tasksSignal) {
        Checkbox checkbox = new Checkbox(task.title(), task.completed());
        checkbox.addValueChangeListener(e -> {
            List<Task> currentTasks = new ArrayList<>(tasksSignal.value());
            int index = currentTasks.indexOf(task);
            if (index >= 0) {
                currentTasks.set(index, new Task(task.id(), task.title(), e.getValue()));
                tasksSignal.value(currentTasks);
            }
        });

        Button removeButton = new Button("Remove", e -> {
            List<Task> currentTasks = new ArrayList<>(tasksSignal.value());
            currentTasks.remove(task);
            tasksSignal.value(currentTasks);
        });
        removeButton.addThemeName("error");
        removeButton.addThemeName("small");

        return new HorizontalLayout(checkbox, removeButton);
    }
}
