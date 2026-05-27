package com.example.uc3;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;

/**
 * UC3 — Dynamic leaf label (parent, {@code uc3}).
 * <p>
 * RouteHierarchy hands the breadcrumb builder bare ancestor classes; it never
 * sees a live view instance, so it cannot apply a per-view dynamic title. The
 * builder special-cases the leaf: when the current view implements
 * {@link com.vaadin.flow.router.HasDynamicTitle} the crumb uses that title.
 * Here the profile page resolves a person's display name from its
 * {@code :userId}.
 */
@Route(value = "uc3", layout = MainLayout.class)
@PageTitle("Users")
@Menu(order = 3, title = "UC3 — Dynamic leaf label")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final BreadcrumbBar breadcrumbs = new BreadcrumbBar();

    public UsersView() {
        add(breadcrumbs);
        add(new H1("Users"));
        add(new Paragraph(
                "Open a profile below. Its breadcrumb leaf is not the static "
                        + "@PageTitle (\"Profile\") — it is the person's name, "
                        + "resolved at runtime from the :userId via "
                        + "HasDynamicTitle. The Users crumb is still found by "
                        + "URL-prefix walking."));
        Directory.USERS.forEach((id, name) -> add(new RouterLink(
                "View " + name, UserProfileView.class,
                new RouteParameters("userId", id))));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        breadcrumbs.show(this, event.getRouteParameters());
    }
}
