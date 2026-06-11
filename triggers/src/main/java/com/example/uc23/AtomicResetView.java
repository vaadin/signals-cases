package com.example.uc23;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.SetPropertyAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC23 — Atomic multi-target reset.
 * <p>
 * A small form with three fields, plus a Reset button. The Reset button's
 * {@link ClickTrigger} wires three {@link SetPropertyAction}s — one per
 * field, each writing a literal default. All three fields update in the
 * same browser frame with no server round-trip and no callback; the server
 * sees the synced values when their change events fire.
 * <p>
 * Demonstrates that one trigger can fan out to many independent targets
 * declaratively. No custom multi-target action is needed.
 */
@Route(value = "uc23", layout = MainLayout.class)
@PageTitle("UC23 — Atomic reset")
@Menu(order = 23, title = "UC23 — Atomic reset")
@StyleSheet("uc23.css")
public class AtomicResetView extends VerticalLayout {

    public AtomicResetView() {
        addClassName("uc23-view");
        add(new H1("UC23 — Atomic reset"));
        add(new Paragraph(
                "Edit the fields, then click Reset. Three SetPropertyAction "
                        + "instances chained to one ClickTrigger fan out to "
                        + "three different targets — each writes a literal "
                        + "default, all in the same browser frame. No "
                        + "callback runs."));

        TextField name = new TextField("Name");
        name.setId("name");
        name.setValue("Ada Lovelace");

        TextField email = new TextField("Email");
        email.setId("email");
        email.setValue("ada@example.com");

        Checkbox subscribe = new Checkbox("Subscribe");
        subscribe.setId("subscribe");
        subscribe.setValue(true);

        Button reset = new Button("Reset");
        reset.setId("reset");

        new ClickTrigger(reset).triggers(
                new SetPropertyAction<>(name, "value", ""),
                new SetPropertyAction<>(email, "value", ""),
                new SetPropertyAction<>(subscribe, "checked", false));

        HorizontalLayout fields = new HorizontalLayout(name, email, subscribe);
        fields.addClassName("fields");
        add(fields, reset);
    }
}
