package com.example.uc1;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * UC1 — URL-prefix trail (leaf, {@code uc1/electronics/laptops}).
 * <p>
 * RouteHierarchy strips two segments to reach the root, producing the full
 * Catalog › Electronics › Laptops trail with both ancestors rendered as links.
 */
@Route(value = "uc1/electronics/laptops", layout = MainLayout.class)
@PageTitle("Laptops")
public class SubcategoryView extends VerticalLayout
        implements BeforeEnterObserver {

    private final BreadcrumbBar breadcrumbs = new BreadcrumbBar();

    public SubcategoryView() {
        add(breadcrumbs);
        add(new H1("Laptops"));
        add(new Paragraph(
                "The deepest level. The breadcrumb reads Catalog › Electronics "
                        + "› Laptops; the first two crumbs are links built from "
                        + "their route classes, the last is the current page. "
                        + "All of it came from RouteHierarchy.resolveAncestors "
                        + "without a single @RouteParent annotation."));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        breadcrumbs.show(this, event.getRouteParameters());
    }
}
