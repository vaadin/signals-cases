package com.example;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

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
    /**
     * Lazily populates a container the first time it becomes visible
     * (create-once-keep pattern). The populator is called only once; on
     * subsequent visibility changes the existing children are kept.
     */
    public static <C extends Component & HasComponents> void lazyPopulate(
            C container, Signal<Boolean> visible,
            SerializableConsumer<C> populator) {
        container.bindVisible(visible).onChange(context -> {
            if (context.getNewValue()
                    && container.getElement().getChildCount() == 0) {
                populator.accept(container);
            }
        });
    }

    /**
     * Lazily populates a container each time it becomes visible and removes all
     * children when it becomes invisible (create-and-destroy pattern).
     */
    public static <C extends Component & HasComponents> void lazyPopulateRecreating(
            C container, Signal<Boolean> visible,
            SerializableConsumer<C> populator) {
        lazyPopulateRecreating(container, visible, populator, null);
    }

    /**
     * Lazily populates a container each time it becomes visible and removes all
     * children when it becomes invisible (create-and-destroy pattern). The
     * optional {@code onDestroy} callback is invoked after {@code removeAll()}.
     */
    public static <C extends Component & HasComponents> void lazyPopulateRecreating(
            C container, Signal<Boolean> visible,
            SerializableConsumer<C> populator,
            @Nullable SerializableConsumer<C> onDestroy) {
        container.bindVisible(visible).onChange(context -> {
            if (context.getNewValue()) {
                populator.accept(container);
            } else if (!context.isInitialRun()) {
                container.removeAll();
                if (onDestroy != null) {
                    onDestroy.accept(container);
                }
            }
        });
    }

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
