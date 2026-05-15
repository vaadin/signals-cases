package com.example.uc5;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC5 — Run an application-defined action on click.
 * <p>
 * Demonstrates the extension SPI: a custom {@link FlashAction} that ships
 * with this view briefly flashes the target's background when clicked. The
 * server side is a 30-line subclass of
 * {@link com.vaadin.flow.component.trigger.AbstractAction}; the client side
 * is a small TS module ({@code flash-action.ts}) that registers a factory
 * against {@code window.Vaadin.Flow.triggers} under the same type id.
 */
@Route(value = "uc5", layout = MainLayout.class)
@PageTitle("UC5 — Custom action via @JsModule")
@Menu(order = 5, title = "UC5 — Custom action")
@JsModule("./flash-action.ts")
@StyleSheet("uc5.css")
public class CustomActionView extends VerticalLayout {

    public CustomActionView() {
        addClassName("uc5-view");
        add(new H1("UC5 — Custom action via @JsModule"));
        add(new Paragraph(
                "Click \"Flash\" to fire a FlashAction defined in this app. "
                        + "FlashAction is a com.vaadin.flow.component.trigger."
                        + "AbstractAction with type id \"demo:flash\"; "
                        + "flash-action.ts registers the matching client factory."));

        Div target = new Div("Flash me");
        target.setId("target");
        target.addClassName("flash-target");

        Button trigger = new Button("Flash");
        trigger.setId("trigger");

        new ClickTrigger(trigger).triggers(new FlashAction(target));

        add(target, trigger);
    }
}
