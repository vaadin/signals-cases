package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-01", layout = MainLayout.class)
@PageTitle("Use Case 1: User Profile Settings Panel")
@Menu(order = 10, title = "UC 1: User Profile Settings")
public class UseCase01View extends VerticalLayout {

    public UseCase01View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 1: User Profile Settings Panel");

        Paragraph scenario = new Paragraph(
            "A user profile settings page where enabling \"Advanced Mode\" reveals additional " +
            "configuration options. The visibility of advanced settings should be reactive to " +
            "the checkbox state."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
