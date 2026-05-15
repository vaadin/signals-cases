package com.example.uc3;

import com.example.views.MainLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.trigger.ClickTrigger;
import com.vaadin.flow.component.trigger.ClipboardCopyAction;
import com.vaadin.flow.component.trigger.Output;
import com.vaadin.flow.component.trigger.PropertyOutput;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC3 — Copy the currently-selected option's value from a native
 * {@code <select>}.
 * <p>
 * Uses the raw {@link Element} API to build the select so the demo stays
 * close to the plain DOM the {@link PropertyOutput} talks to. Reading
 * {@code <select>.value} returns the value of whichever option is currently
 * selected, exactly like any other JS property.
 */
@Route(value = "uc3", layout = MainLayout.class)
@PageTitle("UC3 — Copy current select value")
@Menu(order = 3, title = "UC3 — Copy current select value")
public class CopySelectValueView extends VerticalLayout {

    public CopySelectValueView() {
        add(new H1("UC3 — Copy the currently selected option"));
        add(new Paragraph(
                "PropertyOutput reads the <select> element's value property, "
                        + "which is the value of whichever option is currently "
                        + "selected. Picking a different option changes what "
                        + "lands on the clipboard the next time the button is "
                        + "clicked."));

        Element select = new Element("select");
        select.setAttribute("id", "fruit");
        select.appendChild(option("apple", "Apple"));
        select.appendChild(option("banana", "Banana"));
        select.appendChild(option("cherry", "Cherry"));

        Button copy = new Button("Copy selection");
        copy.setId("copy");

        Output<String> value = new PropertyOutput<>(select, "value",
                String.class);
        new ClickTrigger(copy).triggers(new ClipboardCopyAction(value));

        HorizontalLayout row = new HorizontalLayout();
        row.getElement().appendChild(select);
        row.add(copy);
        add(row);
    }

    private static Element option(String value, String label) {
        Element option = new Element("option");
        option.setAttribute("value", value);
        option.setText(label);
        return option;
    }
}
