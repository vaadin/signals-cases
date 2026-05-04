package com.example.usecase26;

import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.signals.Signal;

/**
 * Helpers for lazily populating a container based on a visibility signal.
 */
public class LazyPopulate {

    /**
     * Lazily populates a container the first time it becomes visible
     * (create-once-keep pattern). The populator is called only once; on
     * subsequent visibility changes the existing children are kept.
     */
    public static <C extends Component & HasComponents> void once(C container,
            Signal<Boolean> visible, SerializableConsumer<C> populator) {
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
    public static <C extends Component & HasComponents> void recreating(
            C container, Signal<Boolean> visible,
            SerializableConsumer<C> populator) {
        recreating(container, visible, populator, null);
    }

    /**
     * Lazily populates a container each time it becomes visible and removes all
     * children when it becomes invisible (create-and-destroy pattern). The
     * optional {@code onDestroy} callback is invoked after {@code removeAll()}.
     */
    public static <C extends Component & HasComponents> void recreating(
            C container, Signal<Boolean> visible,
            SerializableConsumer<C> populator,
            @Nullable SerializableConsumer<C> onDestroy) {
        container.bindVisible(visible).onChange(context -> {
            if (context.getNewValue()) {
                populator.accept(container);
            } else if (!context.isInitialRun()
                    && container.getElement().getChildCount() > 0) {
                container.removeAll();
                if (onDestroy != null) {
                    onDestroy.accept(container);
                }
            }
        });
    }
}
