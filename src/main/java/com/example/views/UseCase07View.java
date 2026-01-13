package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-07", layout = MainLayout.class)
@PageTitle("Use Case 7: Progressive Disclosure with Nested Conditions")
@Menu(order = 31, title = "UC 7: Nested Conditions")
public class UseCase07View extends VerticalLayout {

    public UseCase07View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 7: Progressive Disclosure with Nested Conditions");

        Paragraph scenario = new Paragraph(
            "A job application form where additional questions appear based on previous answers, with " +
            "multiple levels of nesting. For example, if applicant says they need visa sponsorship, " +
            "show visa-related fields, and if they select H1-B visa type, show H1-B-specific fields."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
