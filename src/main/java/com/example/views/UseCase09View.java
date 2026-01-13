package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-09", layout = MainLayout.class)
@PageTitle("Use Case 9: Filtered and Sorted Data Grid")
@Menu(order = 40, title = "UC 9: Filtered Data Grid")
public class UseCase09View extends VerticalLayout {

    public UseCase09View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 9: Filtered and Sorted Data Grid");

        Paragraph scenario = new Paragraph(
            "A product inventory dashboard with a data grid showing products. Users can filter by " +
            "category, search by name, and toggle between showing all products or only those in stock. " +
            "The grid updates reactively as filters change."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
