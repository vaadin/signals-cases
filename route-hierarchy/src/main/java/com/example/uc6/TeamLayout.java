package com.example.uc6;

import java.util.List;

import com.example.views.BreadcrumbBar;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.RouterLayout;

/**
 * UC6 — Layout-wide auto breadcrumbs.
 * <p>
 * One {@link BreadcrumbBar} lives in this parent layout, shared by every child
 * view ({@code uc6}, {@code uc6/team}, {@code uc6/team/:member}). The child
 * views build no breadcrumb of their own.
 * <p>
 * The route-hierarchy PR ships no reactive "current navigation" signal, so the
 * rebuild is driven the classic way: this layout implements
 * {@link AfterNavigationObserver} and rebuilds the trail in
 * {@link #afterNavigation}. {@code AfterNavigationEvent#getActiveChain()} gives
 * the leaf view instance (so {@code HasDynamicTitle} still works) and
 * {@code getRouteParameters()} the live parameters. See {@code API-GAPS.md} for
 * the {@code Signal<NavigationState>} this pattern is crying out for.
 */
@ParentLayout(MainLayout.class)
public class TeamLayout extends Div
        implements RouterLayout, AfterNavigationObserver {

    public static final String REBUILD_BADGE_ID = "uc6-rebuilds";

    private final BreadcrumbBar breadcrumbs = new BreadcrumbBar();
    private final Span rebuildBadge = new Span();
    private final Div content = new Div();
    private int rebuilds;

    public TeamLayout() {
        addClassName("team-layout");
        rebuildBadge.setId(REBUILD_BADGE_ID);
        rebuildBadge.addClassName("rebuild-badge");
        Div header = new Div(breadcrumbs, rebuildBadge);
        header.addClassName("team-header");
        content.addClassName("team-content");
        add(header, content);
    }

    @Override
    public void showRouterLayoutContent(HasElement routerLayoutContent) {
        content.getElement().removeAllChildren();
        if (routerLayoutContent != null) {
            content.getElement().appendChild(routerLayoutContent.getElement());
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        List<HasElement> activeChain = event.getActiveChain();
        if (activeChain.isEmpty()
                || !(activeChain.get(0) instanceof Component leaf)) {
            return;
        }
        breadcrumbs.show(leaf, event.getRouteParameters());
        rebuilds++;
        rebuildBadge.setText("breadcrumb rebuilds: " + rebuilds);
    }
}
