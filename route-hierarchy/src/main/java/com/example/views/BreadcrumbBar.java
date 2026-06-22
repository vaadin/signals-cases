package com.example.views;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteReference;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.RouterState;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.signals.Signal;

/**
 * A reusable breadcrumb trail rendered as a {@link HorizontalLayout} of
 * {@link RouterLink}s. It is the "breadcrumbs section" shared by every use case
 * in this module.
 * <p>
 * The trail is built entirely from the route-hierarchy API: a single
 * {@code getRouteHierarchy(leafClass, parameters)} call returns the whole chain
 * (root first, leaf last) with every entry already paired with the
 * {@link RouteParameters} subset its template needs. Each non-leaf entry
 * becomes a {@link RouterLink}; the leaf is a plain {@link Span} marked as the
 * current page. Each label comes from
 * {@code MenuRegistry.getTitle(class, params)}, which honours both static
 * {@code @PageTitle} and instance-free {@code PageTitleGenerator}s — so dynamic
 * crumbs (leaf <em>and</em> ancestor) need no view instance.
 * <p>
 * The bar wires itself to {@link UI#routerStateSignal()} from its constructor
 * via a single {@link Signal#effect}: the effect rebuilds the trail from the
 * current {@link RouterState} on every navigation (including same-class
 * navigations with different route parameters) and unsubscribes automatically
 * when this component is detached. Views just {@code add(new BreadcrumbBar())}
 * — no {@code BeforeEnterObserver}, no manual seeding.
 */
public class BreadcrumbBar extends HorizontalLayout {

    public BreadcrumbBar() {
        addClassName("breadcrumb-bar");
        setSpacing(false);
        setPadding(false);

        Signal.effect(this, () -> {
            RouterState state = UI.getCurrent().routerStateSignal().get();
            rebuild(state);
        });
    }

    private void rebuild(RouterState state) {
        removeAll();
        HasElement leaf = state.currentView().orElse(null);
        if (!(leaf instanceof Component leafView)) {
            return;
        }

        RouteRegistry registry = VaadinService.getCurrent().getRouter()
                .getRegistry();
        List<RouteReference> trail = RouteUtil.getRouteHierarchy(registry,
                leafView.getClass(), state.routeParameters());

        for (int i = 0; i < trail.size(); i++) {
            if (i > 0) {
                add(separator());
            }
            RouteReference entry = trail.get(i);
            String title = MenuRegistry.getTitle(entry.navigationTarget(),
                    entry.routeParameters());
            boolean isLeaf = i == trail.size() - 1;
            if (isLeaf) {
                add(currentCrumb(title));
            } else {
                add(ancestorLink(entry, title));
            }
        }
    }

    private static RouterLink ancestorLink(RouteReference ancestor,
            String title) {
        RouteParameters parameters = ancestor.routeParameters();
        if (parameters.getParameterNames().isEmpty()) {
            return new RouterLink(title, ancestor.navigationTarget());
        }
        return new RouterLink(title, ancestor.navigationTarget(), parameters);
    }

    private static Span currentCrumb(String text) {
        Span current = new Span(text);
        current.addClassName("breadcrumb-current");
        current.getElement().setAttribute("aria-current", "page");
        return current;
    }

    private static Span separator() {
        Span separator = new Span("›");
        separator.addClassName("breadcrumb-separator");
        separator.getElement().setAttribute("aria-hidden", "true");
        return separator;
    }
}
