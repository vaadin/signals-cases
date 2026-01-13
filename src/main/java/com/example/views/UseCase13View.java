package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-13", layout = MainLayout.class)
@PageTitle("Use Case 13: Master-Detail Invoice View")
@Menu(order = 52, title = "UC 13: Master-Detail Invoice")
public class UseCase13View extends VerticalLayout {

    public UseCase13View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 13: Master-Detail Invoice View");

        Paragraph scenario = new Paragraph(
            "An invoices list where selecting an invoice displays its line items, customer " +
            "information, and payment status. The detail view updates reactively when a different " +
            "invoice is selected."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
