package com.example.views;

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

    private final String name;

    public ColoredAvatar(String name, String color, int sizePx) {
        this.name = name;
        addClassName("presence-avatar");

        Span initial = new Span(
                name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase());
        initial.getStyle().set("font-weight", "600")
                .set("font-size", (sizePx / 2.5) + "px").set("color", "white");
        add(initial);

        getElement().setAttribute("title", name);
        getStyle().set("width", sizePx + "px").set("height", sizePx + "px")
                .set("border-radius", "50%").set("border", "3px solid " + color)
                .set("background-color", color).set("display", "flex")
                .set("align-items", "center").set("justify-content", "center")
                .set("flex-shrink", "0").set("box-sizing", "border-box")
                .set("transition", "filter 120ms, opacity 120ms");
    }

    public ColoredAvatar withState(PageVisibility state) {
        getElement().getClassList()
                .removeAll(java.util.List.of("presence-visible",
                        "presence-blurred", "presence-hidden",
                        "presence-unknown"));
        switch (state) {
        case VISIBLE -> addClassName("presence-visible");
        case VISIBLE_NOT_FOCUSED -> addClassName("presence-blurred");
        case HIDDEN -> addClassName("presence-hidden");
        case UNKNOWN -> addClassName("presence-unknown");
        }
        getElement().setAttribute("title",
                name + " — " + state.name().toLowerCase().replace('_', ' '));
        return this;
    }
}
