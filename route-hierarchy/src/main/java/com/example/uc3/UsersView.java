package com.example.uc3;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;

/**
 * UC3 — Dynamic title gap (parent, {@code uc3}).
 * <p>
 * RouteHierarchy hands the breadcrumb builder bare ancestor classes; it never
 * sees a live view instance, so it cannot apply a per-view dynamic title. The
 * profile page implements {@link com.vaadin.flow.router.HasDynamicTitle} to set
 * its browser tab title to the person's name, but the breadcrumb leaf cannot
 * use that — it falls back to the class name. This use case exists to show that
 * gap.
 */
@Route(value = "uc3", layout = MainLayout.class)
@PageTitle("Users")
@Menu(order = 3, title = "UC3 — Dynamic title gap")
public class UsersView extends VerticalLayout {

    public UsersView() {
        add(new BreadcrumbBar());
        add(new H1("Users"));
        add(new Paragraph(
                "Open a profile below. Its H1 and browser tab title show the "
                        + "person's name (resolved at runtime from the :userId "
                        + "via HasDynamicTitle), but the breadcrumb leaf only "
                        + "shows \"UserProfileView\": the class-based breadcrumb "
                        + "cannot reach a per-instance dynamic title. The Users "
                        + "crumb is still found by URL-prefix walking."));
        Directory.USERS.forEach((id, name) -> add(new RouterLink("View " + name,
                UserProfileView.class, new RouteParameters("userId", id))));
    }
}
