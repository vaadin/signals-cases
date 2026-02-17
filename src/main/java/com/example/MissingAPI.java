package com.example;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.impl.Effect;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Temporary helper class providing static methods for Signal-based component
 * bindings until the official API is implemented in Vaadin.
 */
public class MissingAPI {

    /**
     * Binds a Grid's items to a Signal containing a List.
     */
    public static <T> void bindItems(Grid<T> grid, Signal<List<T>> signal) {
        Effect.effect(grid, () -> {
            List<T> items = signal.get();
            if (items != null) {
                grid.setItems(items);
            } else {
                grid.setItems(List.of());
            }
        });
    }

    /**
     * Binds a Grid's items to a SharedListSignal. Registers dependencies on all
     * individual ValueSignals within the ListSignal by reading each one, so the
     * Grid updates when any item changes.
     */
    public static <T> void bindItems(Grid<T> grid,
            SharedListSignal<T> listSignal) {
        Effect.effect(grid, () -> {
            List<SharedValueSignal<T>> signals = listSignal.get();
            // Read each individual signal to register dependency
            List<T> items = signals.stream().map(SharedValueSignal::get)
                    .toList();
            grid.setItems(items);
        });
    }

    /**
     * Binds a Grid's items to a local ListSignal. Registers dependencies on all
     * individual ValueSignals within the ListSignal by reading each one, so the
     * Grid updates when any item changes.
     */
    public static <T> void bindItems(Grid<T> grid, ListSignal<T> listSignal) {
        Effect.effect(grid, () -> {
            List<ValueSignal<T>> signals = listSignal.get();
            // Read each individual signal to register dependency
            List<T> items = signals.stream().map(ValueSignal::get).toList();
            grid.setItems(items);
        });
    }

    /**
     * Binds a ComboBox's items to a Signal containing a List.
     */
    public static <T> void bindItems(
            com.vaadin.flow.component.combobox.ComboBox<T> comboBox,
            Signal<List<T>> signal) {
        Effect.effect(comboBox, () -> {
            List<T> items = signal.get();
            if (items != null) {
                comboBox.setItems(items);
            } else {
                comboBox.setItems(List.of());
            }
        });
    }

    /**
     * Binds the browser document title to a Signal. The UI is used to get the
     * page and execute JavaScript to update document.title.
     */
    public static void bindBrowserTitle(com.vaadin.flow.component.UI ui,
            Signal<String> signal) {
        Effect.effect(ui, () -> {
            String title = signal.get();
            if (title != null) {
                ui.getPage().setTitle(title);
            }
        });
    }

    /**
     * Synchronizes the selected index between a Tabs component and a writable
     * signal.
     * <p>
     * This method establishes bidirectional binding between a {@code Tabs}
     * instance and a writable integer signal. Changes to the selected tab in
     * the UI will update the signal's value, and changes to the signal's value
     * will update the selected tab in the UI.
     *
     * @param tabs
     *            The Tabs component whose selected index should be synchronized
     *            with the signal. Must not be {@code null}.
     * @param numberSignal
     *            A writable integer signal that will receive the current
     *            selected index of the Tabs and can also update it. Must not be
     *            {@code null}.
     */
    public static void tabsSyncSelectedIndex(Tabs tabs,
            ValueSignal<Integer> numberSignal) {
        Effect.effect(tabs, () -> {
            Integer index = numberSignal.get();
            if (index != null) {
                tabs.setSelectedIndex(index);
            }
        });
        tabs.addSelectedChangeListener(event -> {
            numberSignal.set(event.getSource().getSelectedIndex());
        });
    }
}
