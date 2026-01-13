package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-11", layout = MainLayout.class)
@PageTitle("Use Case 11: Cascading Location Selector")
@Menu(order = 50, title = "UC 11: Cascading Selector")
public class UseCase11View extends VerticalLayout {

    public UseCase11View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 11: Cascading Location Selector");

        Paragraph scenario = new Paragraph(
            "A shipping address form with cascading dropdowns where selecting a country loads the " +
            "available states/provinces, and selecting a state loads the available cities. Each level " +
            "depends on the previous selection, and data is loaded asynchronously."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
