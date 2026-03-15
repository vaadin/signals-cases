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
     * Binds a Grid's items to a Signal containing a List.
     */
    public static <T> void bindItems(Grid<T> grid, Signal<List<T>> signal) {
        Signal.effect(grid, () -> {
            List<T> items = signal.get();
            if (items != null) {
                grid.setItems(items);
            } else {
                grid.setItems(List.of());
            }
        });
    }

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
     * Binds a ComboBox's items to a Signal containing a List.
     */
    public static <T> void bindItems(
            com.vaadin.flow.component.combobox.ComboBox<T> comboBox,
            Signal<List<T>> signal) {
        Signal.effect(comboBox, () -> {
            List<T> items = signal.get();
            if (items != null) {
                comboBox.setItems(items);
            } else {
                comboBox.setItems(List.of());
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

    public static <T> Stream<T> getValues(ListSignal<T> signal) {
        return signal.get().stream().map(Signal::get);
    }
}
