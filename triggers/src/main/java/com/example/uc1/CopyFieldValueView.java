package com.example.uc1;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.PropertyInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC1 — Copy a text field's current value to the clipboard.
 * <p>
 * Wires the canonical trio: a {@link ClickTrigger} on a button, a
 * {@link PropertyInput} reading the field's {@code value} property, and a
 * {@link WriteToClipboardAction}. The whole chain runs in the click handler
 * on the client, so {@code navigator.clipboard.write} sees a real user
 * gesture and the browser allows the write — no server round-trip needed.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("UC1 — Copy field value")
@Menu(order = 1, title = "UC1 — Copy field value")
public class CopyFieldValueView extends VerticalLayout {

    public CopyFieldValueView() {
        add(new H1("UC1 — Copy field value on click"));
        add(new Paragraph(
                "Type something and press \"Copy\". The button's ClickTrigger "
                        + "reads the field's value via a PropertyInput and feeds it "
                        + "to a WriteToClipboardAction — all in the click handler so "
                        + "the browser allows the clipboard write."));

        TextField field = new TextField("Text to copy");
        field.setId("source");
        field.setValue("Hello clipboard");
        field.setWidth("22rem");

        Button copy = new Button("Copy");
        copy.setId("copy");

        Action.Input<String> value = new PropertyInput<>(field, "value",
                String.class);
        new ClickTrigger(copy)
                .triggers(new WriteToClipboardAction(value, null));

        add(new HorizontalLayout(field, copy));
    }
}
