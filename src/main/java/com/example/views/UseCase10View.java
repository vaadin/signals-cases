package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-10", layout = MainLayout.class)
@PageTitle("Use Case 10: Dynamic Task List with Real-time Updates")
@Menu(order = 41, title = "UC 10: Task List")
public class UseCase10View extends VerticalLayout {

    public UseCase10View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 10: Dynamic Task List with Real-time Updates");

        Paragraph scenario = new Paragraph(
            "A task management view where users can add, complete, and remove tasks. The list shows " +
            "tasks grouped by status (pending/completed), with counts and progress indicators that " +
            "update automatically."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
