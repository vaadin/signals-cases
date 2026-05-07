package com.example.views;

import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.PageVisibility;
import com.vaadin.flow.signals.Signal;

/**
 * Colored circular avatar showing the initial of a participant's name. The
 * border ring is the participant's assigned color and the
 * {@link PageVisibility} state controls a CSS class that desaturates / dims the
 * avatar.
 */
public class ColoredAvatar extends Div {

    private final String name;

    public ColoredAvatar(String name, String color, int sizePx) {
        this.name = name;
        addClassName("presence-avatar");

        Span initial = new Span(name.isEmpty() ? "?"
                : name.substring(0, 1).toUpperCase(Locale.ROOT));
        initial.addClassName("presence-avatar-initial");
        add(initial);

        // Per-instance color and size are dynamic, so they're set as CSS
        // custom properties consumed by the .presence-avatar rules.
        getStyle().set("--avatar-color", color);
        getStyle().set("--avatar-size", sizePx + "px");
    }

    public ColoredAvatar bindState(Signal<PageVisibility> state) {
        bindClassName("presence-visible",
                state.map(s -> s == PageVisibility.VISIBLE));
        bindClassName("presence-blurred",
                state.map(s -> s == PageVisibility.VISIBLE_NOT_FOCUSED));
        bindClassName("presence-hidden",
                state.map(s -> s == PageVisibility.HIDDEN));
        bindClassName("presence-unknown",
                state.map(s -> s == PageVisibility.UNKNOWN));
        // No higher-level Component API for the native title attribute, so
        // this stays at the Element level.
        getElement().bindAttribute("title", state.map(s -> name + " — "
                + s.name().toLowerCase(Locale.ROOT).replace('_', ' ')));
        return this;
    }
}
