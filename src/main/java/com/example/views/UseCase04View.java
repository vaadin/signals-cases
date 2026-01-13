package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-04", layout = MainLayout.class)
@PageTitle("Use Case 4: E-commerce Product Configurator")
@Menu(order = 20, title = "UC 4: Product Configurator")
public class UseCase04View extends VerticalLayout {

    public UseCase04View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 4: E-commerce Product Configurator");

        Paragraph scenario = new Paragraph(
            "A product configuration page where users select options (size, color, quantity) and " +
            "the page displays the real-time price, availability status, and product image. Changes " +
            "to any option should immediately update all dependent displays."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
