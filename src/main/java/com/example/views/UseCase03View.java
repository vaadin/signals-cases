package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-03", layout = MainLayout.class)
@PageTitle("Use Case 3: Dynamic Button State")
@Menu(order = 12, title = "UC 3: Dynamic Button State")
public class UseCase03View extends VerticalLayout {

    public UseCase03View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 3: Dynamic Button State");

        Paragraph scenario = new Paragraph(
            "A form submission button that should only be enabled when all required fields are " +
            "filled and valid. The button text should also change based on the form state " +
            "(e.g., \"Save\" vs \"Saving...\" during submission)."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
