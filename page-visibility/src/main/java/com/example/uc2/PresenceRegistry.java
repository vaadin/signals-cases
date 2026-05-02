package com.example.uc2;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.vaadin.flow.component.page.PageVisibility;
import com.vaadin.flow.signals.shared.SharedListSignal;
import com.vaadin.flow.signals.shared.SharedValueSignal;

/**
 * Cross-UI registry that tracks every connected presence and its current
 * {@link PageVisibility}. Backed by a {@link SharedListSignal}, so any UI
 * binding the signal automatically re-renders when participants join, leave or
 * change visibility state — regardless of which {@code VaadinSession} they
 * belong to.
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

    public SharedListSignal<Presence> signal() {
        return signal;
    }

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
            Presence current = entry.peek();
            if (current != null && current.state() != state) {
                entry.set(current.withState(state));
            }
        }
    }
}
