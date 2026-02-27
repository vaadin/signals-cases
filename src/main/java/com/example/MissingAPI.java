package com.example;

import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.signals.Signal;
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
     * Binds a VirtualList's items to a Signal containing a List.
     */
    public static <T> void bindItems(VirtualList<T> virtualList,
            Signal<List<T>> signal) {
        Signal.effect(virtualList, () -> {
            List<T> items = signal.get();
            if (items != null) {
                virtualList.setItems(items);
            } else {
                virtualList.setItems(List.of());
            }
        });
    }

    /**
     * Binds a VirtualList's items to a SharedListSignal. Registers dependencies
     * on all individual ValueSignals within the ListSignal by reading each one,
     * so the VirtualList updates when any item changes.
     */
    public static <T> void bindItems(VirtualList<T> virtualList,
            SharedListSignal<T> listSignal) {
        Signal.effect(virtualList, () -> {
            List<SharedValueSignal<T>> signals = listSignal.get();
            // Read each individual signal to register dependency
            List<T> items = signals.stream().map(SharedValueSignal::get)
                    .toList();
            virtualList.setItems(items);
        });
    }

    /**
     * Binds a VirtualList's items to a local ListSignal. Registers dependencies
     * on all individual ValueSignals within the ListSignal by reading each one,
     * so the VirtualList updates when any item changes.
     */
    public static <T> void bindItems(VirtualList<T> virtualList,
            ListSignal<T> listSignal) {
        Signal.effect(virtualList, () -> {
            List<ValueSignal<T>> signals = listSignal.get();
            // Read each individual signal to register dependency
            List<T> items = signals.stream().map(ValueSignal::get).toList();
            virtualList.setItems(items);
        });
    }

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
