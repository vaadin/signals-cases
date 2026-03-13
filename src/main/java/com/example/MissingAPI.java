package com.example;

import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

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
     */
    public static void tabsSyncSelectedIndex(Tabs tabs,
            ValueSignal<Integer> numberSignal) {
        Signal.effect(tabs, () -> {
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
