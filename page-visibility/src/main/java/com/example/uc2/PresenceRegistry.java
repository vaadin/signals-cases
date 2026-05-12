package com.example.uc2;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.PageVisibility;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Cross-UI registry that tracks every connected presence and its current
 * {@link PageVisibility}. Backed by a {@link SharedListSignal} that is kept
 * private so callers cannot bypass {@link #join}, {@link #leave} and
 * {@link #updateState} — they instead bind through {@link #bindTo} or query
 * with {@link #size}.
 */
@Component
public class PresenceRegistry {

    public record Presence(String id, String name, String color,
            PageVisibility state) implements Serializable {
        public Presence withState(PageVisibility newState) {
            return new Presence(id, name, color, newState);
        }
    }

    private final SharedListSignal<Presence> signal = new SharedListSignal<>(
            Presence.class);
    private final ConcurrentMap<String, SharedValueSignal<Presence>> entries = new ConcurrentHashMap<>();

    public void join(Presence presence) {
        SharedValueSignal<Presence> entry = signal.insertLast(presence)
                .signal();
        entries.put(presence.id(), entry);
    }

    public void leave(String id) {
        SharedValueSignal<Presence> entry = entries.remove(id);
        if (entry != null) {
            signal.remove(entry);
        }
    }

    public void updateState(String id, PageVisibility state) {
        SharedValueSignal<Presence> entry = entries.get(id);
        if (entry != null) {
            // update() applies the transition atomically; returning the same
            // reference when the state hasn't changed is a no-op.
            entry.update(current -> current == null || current.state() == state
                    ? current
                    : current.withState(state));
        }
    }

    /**
     * Binds the given container's children to this registry, rendering each
     * presence with the supplied factory. The container re-renders whenever any
     * UI joins, leaves or changes visibility state.
     */
    public void bindTo(Div container,
            SerializableFunction<SharedValueSignal<Presence>, com.vaadin.flow.component.Component> renderer) {
        container.bindChildren(signal, renderer);
    }

    /** Snapshot count of currently registered presences. */
    public long size() {
        return signal.peekValues().count();
    }
}
