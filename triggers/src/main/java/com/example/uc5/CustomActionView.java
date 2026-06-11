package com.example.uc5;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC5 — Run an application-defined action on click.
 * <p>
 * Demonstrates the extension SPI: a custom {@link FlashAction} subclasses
 * {@code Action} and emits the JS that runs when the trigger fires. No
 * {@code @JsModule} or client-side factory registration — the JS body is
 * declared next to the server-side logic and ends up inlined in the trigger's
 * handler function.
 */
@Route(value = "uc5", layout = MainLayout.class)
@PageTitle("UC5 — Custom action")
@Menu(order = 5, title = "UC5 — Custom action")
@StyleSheet("uc5.css")
public class CustomActionView extends VerticalLayout {

    public CustomActionView() {
        addClassName("uc5-view");
        add(new H1("UC5 — Custom action"));
        add(new Paragraph(
                "Click \"Flash\" to fire a FlashAction defined in this app. "
                        + "FlashAction subclasses com.vaadin.flow.component."
                        + "trigger.internal.Action and writes the flash JS into "
                        + "the trigger's handler via appendStatement — no "
                        + "@JsModule, no client-side registry."));

        Div target = new Div("Flash me");
        target.setId("target");
        target.addClassName("flash-target");

        Button trigger = new Button("Flash");
        trigger.setId("trigger");

        new ClickTrigger(trigger).triggers(new FlashAction(target));

        add(target, trigger);
    }
}
