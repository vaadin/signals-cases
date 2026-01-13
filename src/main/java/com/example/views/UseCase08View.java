package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-08", layout = MainLayout.class)
@PageTitle("Use Case 8: Permission-Based Component Visibility")
@Menu(order = 32, title = "UC 8: Permission-Based UI")
public class UseCase08View extends VerticalLayout {

    public UseCase08View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 8: Permission-Based Component Visibility");

        Paragraph scenario = new Paragraph(
            "An admin dashboard where UI elements (buttons, sections, menu items) are shown or " +
            "hidden based on the current user's role and permissions. Permissions can change " +
            "dynamically (e.g., when switching between user accounts or when permissions are updated)."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
