package com.example.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "use-case-05", layout = MainLayout.class)
@PageTitle("Use Case 5: Dynamic Pricing Calculator")
@Menu(order = 21, title = "UC 5: Pricing Calculator")
public class UseCase05View extends VerticalLayout {

    public UseCase05View() {
        setSpacing(true);
        setPadding(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2("Use Case 5: Dynamic Pricing Calculator");

        Paragraph scenario = new Paragraph(
            "A pricing calculator for a service where the final price depends on multiple factors: " +
            "base service, add-ons, quantity, discount code, and tax rate. All price components " +
            "should be displayed in real-time as users adjust inputs."
        );

        Paragraph pending = new Paragraph("Implementation pending...");
        pending.getStyle().set("color", "var(--lumo-secondary-text-color)");

        add(title, scenario, pending);
    }
}
