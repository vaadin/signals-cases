package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-02", layout = MainLayout.class)
@PageTitle("Use Case 2: Form Field Synchronization")
@Menu(order = 11, title = "UC 2: Form Field Synchronization")
public class UseCase02View extends VerticalLayout {

    public UseCase02View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 2: Form Field Synchronization");

        Paragraph scenario = new Paragraph(
            "A form where multiple read-only fields display the same computed value (e.g., a " +
            "username generated from first and last name). All displays should update automatically " +
            "when the source value changes."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
