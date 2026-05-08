package com.example.uc1;

import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC1 — Select all on focus.
 * <p>
 * Each time the user focuses the field, the current value is selected so that
 * typing immediately overwrites it. Demonstrates a server-driven
 * {@code selectAll()} call from a focus listener — different from the
 * {@code autoselect} attribute, because the server stays in control and can
 * decide per-event whether to select.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("UC1 — Select all on focus")
@Menu(order = 1, title = "UC1 — Select all on focus")
public class SelectAllOnFocusView extends VerticalLayout {

    public SelectAllOnFocusView() {
        add(new H1("UC1 — Select all on focus"));
        add(new Paragraph(
                "Click into the quantity field. The current value is selected, "
                        + "so typing replaces it without first deleting. "
                        + "Tab away and back: the value re-selects every time."));

        TextField quantity = new TextField("Quantity");
        quantity.setValue("10");
        quantity.setHelperText("Type to overwrite — no need to clear first");

        Span focusCount = new Span("0");
        int[] focuses = { 0 };
        quantity.addFocusListener(e -> {
            focuses[0]++;
            focusCount.setText(Integer.toString(focuses[0]));
            quantity.selectAll();
        });

        HorizontalLayout counterRow = new HorizontalLayout(
                new Span("Focus events:"), focusCount);
        counterRow.setAlignItems(Alignment.BASELINE);

        add(quantity, counterRow);
    }
}
