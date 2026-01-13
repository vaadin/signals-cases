package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-14", layout = MainLayout.class)
@PageTitle("Use Case 14: Multi-Step Wizard with Validation")
@Menu(order = 60, title = "UC 14: Multi-Step Wizard")
public class UseCase14View extends VerticalLayout {

    public UseCase14View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 14: Multi-Step Wizard with Validation");

        Paragraph scenario = new Paragraph(
            "A multi-step registration wizard where users progress through steps (Personal Info → " +
            "Company Info → Plan Selection → Review). Each step must be valid before proceeding. " +
            "The wizard shows progress and allows navigation back to previous steps."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
