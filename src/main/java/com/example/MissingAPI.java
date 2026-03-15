package com.example;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;

/**
 * Temporary helper class providing static methods for Signal-based component
 * bindings until the official API is implemented in Vaadin.
 */
public class MissingAPI {

    /**
     * Binds the browser document title to a Signal. The UI is used to get the
     * page and execute JavaScript to update document.title.
     */
    public static void bindBrowserTitle(com.vaadin.flow.component.UI ui,
            Signal<String> signal) {
        Signal.effect(ui, () -> {
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
     * @param writeCallback
     *            A callback invoked to write the new selected index back to the
     *            signal when the user changes the tab in the UI. Must not be
     *            {@code null}.
     */
    public static void tabsSyncSelectedIndex(TabSheet tabs,
            Signal<Integer> numberSignal, SerializableConsumer<Integer> writeCallback) {
        Signal.effect(tabs, () -> {
            Integer index = numberSignal.get();
            if (index != null) {
                tabs.setSelectedIndex(index);
            }
        });
        tabs.addSelectedChangeListener(event -> {
            writeCallback.accept(event.getSource().getSelectedIndex());
        });
    }

    /**
     * Extracts the current values from a {@link ListSignal} as a stream.
     * <p>
     * Each entry in the list signal is itself a signal; this method unwraps
     * them by calling {@link Signal#get()} on each entry.
     *
     * @param <T>
     *            the type of values in the list signal
     * @param signal
     *            the list signal to extract values from. Must not be
     *            {@code null}.
     * @return a stream of the current values
     */
    public static <T> Stream<T> getValues(ListSignal<T> signal) {
        return signal.get().stream().map(Signal::get);
    }
}
