package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-15", layout = MainLayout.class)
@PageTitle("Use Case 15: Form with Binder Integration and Signal Validation")
@Menu(order = 61, title = "UC 15: Binder Integration")
public class UseCase15View extends VerticalLayout {

    public UseCase15View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 15: Form with Binder Integration and Signal Validation");

        Paragraph scenario = new Paragraph(
            "A user registration form that uses Vaadin's Binder API with signal-based validation. " +
            "The form has cross-field validation (password confirmation, age restrictions based on " +
            "account type), and the save button should be enabled only when all validation passes. " +
            "The validation status should be reactive and displayed to the user."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
