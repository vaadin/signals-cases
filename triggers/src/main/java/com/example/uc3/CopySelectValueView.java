package com.example.uc3;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.internal.Action;
import com.vaadin.flow.component.trigger.internal.ClickTrigger;
import com.vaadin.flow.component.trigger.internal.PropertyInput;
import com.vaadin.flow.component.trigger.internal.WriteToClipboardAction;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC3 — Copy the currently-selected option's value from a native
 * {@code <select>}.
 * <p>
 * Reading {@code <select>.value} returns the value of whichever option is
 * currently selected, exactly like any other JS property. Demonstrates
 * PropertyInput on a different element type than the form fields in UC1/UC2.
 */
@Route(value = "uc3", layout = MainLayout.class)
@PageTitle("UC3 — Copy current select value")
@Menu(order = 3, title = "UC3 — Copy current select value")
public class CopySelectValueView extends VerticalLayout {

    public CopySelectValueView() {
        add(new H1("UC3 — Copy the currently selected option"));
        add(new Paragraph(
                "PropertyInput reads the <select> element's value property, "
                        + "which is the value of whichever option is currently "
                        + "selected. Picking a different option changes what "
                        + "lands on the clipboard the next time the button is "
                        + "clicked."));

        NativeSelect select = new NativeSelect()
                .addOption("apple", "Apple")
                .addOption("banana", "Banana")
                .addOption("cherry", "Cherry");
        select.setId("fruit");

        Button copy = new Button("Copy selection");
        copy.setId("copy");

        Action.Input<String> value = new PropertyInput<>(select, "value",
                String.class);
        new ClickTrigger(copy)
                .triggers(new WriteToClipboardAction(value, null));

        add(new HorizontalLayout(select, copy));
    }
}
