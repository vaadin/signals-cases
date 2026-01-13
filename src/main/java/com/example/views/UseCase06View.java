package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-06", layout = MainLayout.class)
@PageTitle("Use Case 6: Dynamic Form with Conditional Subforms")
@Menu(order = 30, title = "UC 6: Conditional Subforms")
public class UseCase06View extends VerticalLayout {

    public UseCase06View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 6: Dynamic Form with Conditional Subforms");

        Paragraph scenario = new Paragraph(
            "An insurance quote form where different types of insurance (Auto, Home, Life) require " +
            "completely different sets of fields. When the user selects an insurance type from a " +
            "dropdown, the form should show only the relevant fields for that type."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
