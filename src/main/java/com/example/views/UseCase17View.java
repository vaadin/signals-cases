package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-17", layout = MainLayout.class)
@PageTitle("Use Case 17: Employee Management Grid with Dynamic Editability")
@Menu(order = 63, title = "UC 17: Grid Providers")
public class UseCase17View extends VerticalLayout {

    public UseCase17View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 17: Employee Management Grid with Dynamic Editability");

        Paragraph scenario = new Paragraph(
            "An employee management grid where cells can be edited based on user permissions and the " +
            "current edit mode. The grid supports drag-and-drop reordering (only when enabled), context " +
            "menus with dynamic content based on row data, and selective row selection based on employee " +
            "status. All these behaviors are controlled by signals."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
