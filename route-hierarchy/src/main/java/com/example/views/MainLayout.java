package com.example.views;

import java.util.Optional;

import com.example.common.BaseMainLayout;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteReference;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.menu.MenuEntry;

@PageTitle("Route Hierarchy Use Cases")
public class MainLayout extends BaseMainLayout {

    public MainLayout() {
        super("route-hierarchy", "Route Hierarchy Use Cases");
    }

    /**
     * Keep the flat main navigation to top-level use cases by asking the actual
     * route hierarchy — {@code RouteUtil.getRouteParent}, the same
     * {@code @RouteParent}-aware helper {@code getMenuEntriesTree()} nests with
     * — rather than comparing URL paths (a logical parent need not share a URL
     * prefix). An entry is shown when it is a root, or a direct child of a
     * root: every use-case root sits under the landing page ({@code ""}), so
     * this keeps Home and UC1–UC8 while hiding the deeper nested {@code @Menu}
     * views (Electronics, Security, Team, …) that only belong in UC8's tree.
     */
    @Override
    protected boolean includeInMainNav(MenuEntry entry) {
        RouteRegistry registry = VaadinService.getCurrent().getRouter()
                .getRegistry();
        Optional<RouteReference> parent = RouteUtil.getRouteParent(
                registry, entry.menuClass(), RouteParameters.empty());
        if (parent.isEmpty()) {
            return true;
        }
        return RouteUtil.getRouteParent(registry,
                parent.get().navigationTarget(), RouteParameters.empty())
                .isEmpty();
    }
}
