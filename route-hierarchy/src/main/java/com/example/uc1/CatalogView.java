package com.example.uc1;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/**
 * UC1 — URL-prefix trail (root).
 * <p>
 * The three views of this use case nest purely by URL: {@code uc1},
 * {@code uc1/electronics}, {@code uc1/electronics/laptops}. None of them carry
 * a {@code @RouteParent} annotation, so
 * {@link com.vaadin.flow.router.RouteHierarchy} discovers the trail entirely
 * through its URL-prefix fallback — it strips the last path segment and looks
 * the shorter URL up in the route registry. This is the zero-configuration
 * case: lay your routes out hierarchically and the breadcrumb just works.
 * <p>
 * The view does no breadcrumb plumbing: the {@link BreadcrumbBar} subscribes to
 * {@code UI.routerStateSignal()} from its own constructor and rebuilds itself
 * on every navigation.
 */
@Route(value = "uc1", layout = MainLayout.class)
@PageTitle("Catalog")
@Menu(order = 1, title = "UC1 — URL-prefix trail")
public class CatalogView extends VerticalLayout {

    public CatalogView() {
        add(new BreadcrumbBar());
        add(new H1("Catalog"));
        add(new Paragraph(
                "This is the root of a three-level catalog. Each level lives at "
                        + "a longer URL — uc1 → uc1/electronics → "
                        + "uc1/electronics/laptops — with no @RouteParent "
                        + "annotations anywhere. RouteHierarchy walks the trail "
                        + "by stripping URL segments. Drill in and watch the "
                        + "breadcrumb above grow."));
        add(new RouterLink("Browse Electronics →", CategoryView.class));
    }
}
