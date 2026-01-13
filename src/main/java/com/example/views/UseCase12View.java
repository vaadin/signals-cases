package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-12", layout = MainLayout.class)
@PageTitle("Use Case 12: Shopping Cart with Real-time Totals")
@Menu(order = 51, title = "UC 12: Shopping Cart")
public class UseCase12View extends VerticalLayout {

    public UseCase12View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 12: Shopping Cart with Real-time Totals");

        Paragraph scenario = new Paragraph(
            "An e-commerce shopping cart where users can adjust quantities of multiple items. The " +
            "subtotal, shipping, tax, and grand total update automatically as quantities change. " +
            "Discount codes and shipping options affect the final price."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
