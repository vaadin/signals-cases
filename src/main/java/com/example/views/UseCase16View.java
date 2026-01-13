package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-16", layout = MainLayout.class)
@PageTitle("Use Case 16: Responsive Dashboard with ComponentToggle")
@Menu(order = 62, title = "UC 16: ComponentToggle")
public class UseCase16View extends VerticalLayout {

    public UseCase16View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 16: Responsive Dashboard with ComponentToggle");

        Paragraph scenario = new Paragraph(
            "A business dashboard that displays data in different view modes (Table, Cards, Chart). " +
            "Users can switch between views, and the application should only instantiate the currently " +
            "selected view (lazy loading). On mobile devices, the default view should be Cards, while " +
            "desktop shows Table by default. The ComponentToggle utility manages the mutually exclusive rendering."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
