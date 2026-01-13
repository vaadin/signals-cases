package com.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Temporary helper class providing static methods for Signal-based component bindings
 * until the official API is implemented in Vaadin.
 */
public class MissingAPI {

    // WeakHashMap to store effect cleanup runnables per component
    private static final WeakHashMap<Component, List<Runnable>> EFFECT_CLEANUPS = new WeakHashMap<>();

    /**
     * Stores an effect cleanup runnable with the component so it doesn't get garbage collected
     * and can be cleaned up when the component is detached.
     */
    private static void registerEffectCleanup(Component component, Runnable cleanup) {
        List<Runnable> cleanups = EFFECT_CLEANUPS.get(component);
        if (cleanups == null) {
            cleanups = new ArrayList<>();
            EFFECT_CLEANUPS.put(component, cleanups);

            // Register detach listener to clean up all effects when component is removed
            component.addDetachListener(event -> {
                List<Runnable> toClean = EFFECT_CLEANUPS.get(event.getSource());
                if (toClean != null) {
                    toClean.forEach(Runnable::run);
                    toClean.clear();
                    EFFECT_CLEANUPS.remove(event.getSource());
                }
            });
        }
        cleanups.add(cleanup);
    }

    /**
     * Binds a component's value to a WritableSignal with two-way synchronization.
     */
    public static <T, C extends HasValue<?, T>> void bindValue(C component, WritableSignal<T> signal) {
        UI ui = UI.getCurrent();

        // Set initial value from signal to component
        component.setValue(signal.value());

        // Component -> Signal
        component.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                signal.value(event.getValue());
            }
        });

        // Signal -> Component
        Signal.effect(() -> {
            T value = signal.value();
            ui.access(() -> {
                if (!java.util.Objects.equals(component.getValue(), value)) {
                    component.setValue(value);
                }
            });
        });
    }

    /**
     * Binds a component's visibility to a Signal.
     */
    public static void bindVisible(Component component, Signal<Boolean> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            boolean visible = signal.value();
            ui.access(() -> component.setVisible(visible));
        });
    }

    /**
     * Binds a component's enabled state to a Signal.
     */
    public static void bindEnabled(HasEnabled component, Signal<Boolean> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            boolean enabled = signal.value();
            ui.access(() -> component.setEnabled(enabled));
        });
    }

    /**
     * Binds a component's text content to a Signal.
     */
    public static void bindText(HasText component, Signal<String> signal) {
        UI ui = UI.getCurrent();
        Runnable cleanup = Signal.effect(() -> {
            String text = signal.value();
            ui.access(() -> component.setText(text));
        });
        if (component instanceof Component) {
            registerEffectCleanup((Component) component, cleanup);
        }
    }

    /**
     * Binds a component's theme name to a Signal.
     */
    public static void bindThemeName(Component component, Signal<String> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            String themeName = signal.value();
            ui.access(() -> component.getElement().setAttribute("theme", themeName != null ? themeName : ""));
        });
    }

    /**
     * Binds an HTML attribute to a Signal.
     */
    public static void bindAttribute(Component component, String attributeName, Signal<String> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            String value = signal.value();
            ui.access(() -> {
                if (value != null) {
                    component.getElement().setAttribute(attributeName, value);
                } else {
                    component.getElement().removeAttribute(attributeName);
                }
            });
        });
    }

    /**
     * Binds a component's CSS class name based on a boolean Signal.
     */
    public static void bindClassName(Component component, String className, Signal<Boolean> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            boolean add = signal.value();
            ui.access(() -> {
                if (add) {
                    component.addClassName(className);
                } else {
                    component.removeClassName(className);
                }
            });
        });
    }

    /**
     * Binds a HasValue component's required state to a Signal.
     */
    public static <T, C extends HasValue<?, T>> void bindRequired(C component, Signal<Boolean> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            boolean required = signal.value();
            ui.access(() -> component.setRequiredIndicatorVisible(required));
        });
    }

    /**
     * Binds a component's helper text to a Signal (for components that support it).
     */
    public static void bindHelperText(Component component, Signal<String> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            String helperText = signal.value();
            ui.access(() -> component.getElement().setProperty("helperText", helperText != null ? helperText : ""));
        });
    }

    /**
     * Binds a Grid's items to a Signal containing a List.
     */
    public static <T> void bindItems(Grid<T> grid, Signal<List<T>> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            List<T> items = signal.value();
            ui.access(() -> {
                if (items != null) {
                    grid.setItems(items);
                } else {
                    grid.setItems(List.of());
                }
            });
        });
    }

    /**
     * Binds a ComboBox's items to a Signal containing a List.
     */
    public static <T> void bindItems(com.vaadin.flow.component.combobox.ComboBox<T> comboBox, Signal<List<T>> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            List<T> items = signal.value();
            ui.access(() -> {
                if (items != null) {
                    comboBox.setItems(items);
                } else {
                    comboBox.setItems(List.of());
                }
            });
        });
    }

    /**
     * Binds a component's children dynamically based on a Signal containing a List.
     */
    public static <T> void bindChildren(Component container, Signal<List<T>> signal, Function<T, Component> mapper) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            List<T> items = signal.value();
            ui.access(() -> {
                container.getElement().removeAllChildren();
                if (items != null) {
                    items.stream()
                        .map(mapper)
                        .forEach(child -> container.getElement().appendChild(child.getElement()));
                }
            });
        });
    }

    /**
     * Binds a component's children dynamically when the signal already contains Component instances.
     */
    public static <T extends Component> void bindChildren(Component container, Signal<List<T>> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            List<T> items = signal.value();
            ui.access(() -> {
                container.getElement().removeAllChildren();
                if (items != null) {
                    items.forEach(child -> container.getElement().appendChild(child.getElement()));
                }
            });
        });
    }

    /**
     * Binds a component's children using ComponentProvider pattern (for advanced use cases).
     */
    public static <T> void bindComponentChildren(Component container, Signal<List<T>> signal, Function<T, Component> mapper) {
        bindChildren(container, signal, mapper);
    }

    /**
     * Binds a Grid column's editable state to a Signal.
     */
    public static <T> void bindEditable(Grid.Column<T> column, Signal<Boolean> signal) {
        Signal.effect(() -> {
            // Simplified implementation - full implementation would set up editor components based on signal value
            boolean editable = signal.value();
            // Note: Grid column editability would be set up here in a real implementation
        });
    }

    /**
     * Binds Grid row selection capability based on a Signal.
     */
    public static <T> void bindRowSelectable(Grid<T> grid, Signal<Function<T, Boolean>> signal) {
        // Simplified implementation - full implementation would override selection logic
        Signal.effect(() -> {
            // In a real implementation, this would set up a selection predicate
            Function<T, Boolean> predicate = signal.value();
            // For now, this is a placeholder
        });
    }

    /**
     * Binds Grid drag capability to a Signal.
     */
    public static <T> void bindDragEnabled(Grid<T> grid, Signal<Boolean> signal) {
        UI ui = UI.getCurrent();
        Signal.effect(() -> {
            boolean draggable = signal.value();
            ui.access(() -> grid.setRowsDraggable(draggable));
        });
    }
}
