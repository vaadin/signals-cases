package com.example;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * Temporary helper class providing static methods for Signal-based component
 * bindings until the official API is implemented in Vaadin.
 */
public class MissingAPI {

    /**
     * Binds a Grid's items to a Signal containing a List.
     * <p>
     * Creates a reactive effect that updates the grid's items whenever the
     * signal value changes. If the signal value is {@code null}, the grid is
     * cleared.
     *
     * @param <T>
     *            the type of items in the grid
     * @param grid
     *            the grid to bind. Must not be {@code null}.
     * @param signal
     *            a signal containing the list of items. Must not be
     *            {@code null}.
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
     * <p>
     * Creates a reactive effect that updates the virtual list's items whenever
     * the signal value changes. If the signal value is {@code null}, the list
     * is cleared.
     *
     * @param <T>
     *            the type of items in the virtual list
     * @param virtualList
     *            the virtual list to bind. Must not be {@code null}.
     * @param signal
     *            a signal containing the list of items. Must not be
     *            {@code null}.
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
     * <p>
     * Creates a reactive effect that updates the combo box's items whenever
     * the signal value changes. If the signal value is {@code null}, the combo
     * box is cleared.
     *
     * @param <T>
     *            the type of items in the combo box
     * @param comboBox
     *            the combo box to bind. Must not be {@code null}.
     * @param signal
     *            a signal containing the list of items. Must not be
     *            {@code null}.
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

    /**
     * A component's size in pixels, mirroring the future
     * {@code Component.Size} API from vaadin/flow#23618.
     */
    public record ComponentSize(int width, int height) implements Serializable {
    }

    private static final String RESIZE_EVENT = "component-resize";

    private static final String SETUP_RESIZE_OBSERVER_JS = """
            const target = $0;
            const resizeObserver = new ResizeObserver(entries => {
                for (let entry of entries) {
                    const width = Math.floor(entry.contentRect.width);
                    const height = Math.floor(entry.contentRect.height);
                    const event = new CustomEvent('%s', {
                        detail: { width: width, height: height }
                    });
                    target.dispatchEvent(event);
                }
            });
            resizeObserver.observe(target);
            target._resizeObserver = resizeObserver;
            const rect = target.getBoundingClientRect();
            const event = new CustomEvent('%s', {
                detail: { width: Math.floor(rect.width), height: Math.floor(rect.height) }
            });
            target.dispatchEvent(event);
            """.formatted(RESIZE_EVENT, RESIZE_EVENT);

    private static final String CLEANUP_RESIZE_OBSERVER_JS = """
            const target = $0;
            if (target && target._resizeObserver) {
                target._resizeObserver.disconnect();
                delete target._resizeObserver;
            }
            """;

    /**
     * Creates a read-only signal that tracks the size of the given component's
     * element using a ResizeObserver.
     * <p>
     * This mirrors the future {@code Component.sizeSignal()} API from
     * vaadin/flow#23618. When that PR merges, replace
     * {@code MissingAPI.sizeSignal(component)} with
     * {@code component.sizeSignal()}.
     */
    public static Signal<ComponentSize> sizeSignal(Component component) {
        ValueSignal<ComponentSize> signal = new ValueSignal<>(
                new ComponentSize(0, 0));

        component.addAttachListener(attachEvent -> {
            DomListenerRegistration reg = component.getElement()
                    .addEventListener(RESIZE_EVENT, event -> {
                        int width = (int) event.getEventData()
                                .get("event.detail.width").asDouble();
                        int height = (int) event.getEventData()
                                .get("event.detail.height").asDouble();
                        signal.set(new ComponentSize(width, height));
                    }).addEventData("event.detail.width")
                    .addEventData("event.detail.height");

            component.getElement().executeJs(SETUP_RESIZE_OBSERVER_JS,
                    component.getElement());

            component.addDetachListener(detachEvent -> {
                reg.remove();
                component.getElement().executeJs(
                        CLEANUP_RESIZE_OBSERVER_JS, component.getElement());
            });
        });

        return signal.asReadonly();
    }
}
