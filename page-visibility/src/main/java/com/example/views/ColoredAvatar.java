package com.example.views;

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.page.PageVisibility;

/**
 * Colored circular avatar showing the initial of a participant's name. The
 * border ring is the participant's assigned color and the
 * {@link PageVisibility} state controls a CSS class that desaturates / dims the
 * avatar.
 */
public class ColoredAvatar extends Div {

    private static final List<String> STATE_CLASSES = List.of(
            "presence-visible", "presence-blurred", "presence-hidden",
            "presence-unknown");

    private final String name;

    public ColoredAvatar(String name, String color, int sizePx) {
        this.name = name;
        addClassName("presence-avatar");

        Span initial = new Span(name.isEmpty() ? "?"
                : name.substring(0, 1).toUpperCase(Locale.ROOT));
        initial.addClassName("presence-avatar-initial");
        add(initial);

        getElement().setAttribute("title", name);
        // Per-instance color and size are dynamic, so they're set as CSS
        // custom properties consumed by the .presence-avatar rules.
        getStyle().set("--avatar-color", color);
        getStyle().set("--avatar-size", sizePx + "px");
    }

    public ColoredAvatar withState(PageVisibility state) {
        getElement().getClassList().removeAll(STATE_CLASSES);
        switch (state) {
        case VISIBLE -> addClassName("presence-visible");
        case VISIBLE_NOT_FOCUSED -> addClassName("presence-blurred");
        case HIDDEN -> addClassName("presence-hidden");
        case UNKNOWN -> addClassName("presence-unknown");
        }
        getElement().setAttribute("title", name + " — "
                + state.name().toLowerCase(Locale.ROOT).replace('_', ' '));
        return this;
    }
}
