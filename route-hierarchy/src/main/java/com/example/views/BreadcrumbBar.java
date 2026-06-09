package com.example.views;

import java.util.List;

import com.example.MissingAPI;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteHierarchy;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.router.RouterState;
import com.vaadin.flow.signals.Signal;

/**
 * A reusable breadcrumb trail rendered as a {@link HorizontalLayout} of
 * {@link RouterLink}s. It is the "breadcrumbs section" shared by every use case
 * in this module.
 * <p>
 * The trail is built entirely from the Flow-core route-hierarchy API introduced
 * in <a href="https://github.com/vaadin/flow/pull/24451">flow#24451</a>:
 * {@link RouteHierarchy#resolveAncestors(Class, RouteConfiguration)} returns
 * the ancestor chain (root-first, leaf last) by consulting {@code @RouteParent}
 * first and falling back to URL-prefix walking. Each ancestor becomes a
 * {@link RouterLink}; the leaf is rendered as a plain, non-linked {@link Span}
 * marked as the current page. Every crumb's label is resolved from its route
 * class via {@link MissingAPI#staticTitle(Class)}; per-ancestor parameter
 * filtering is done by {@link MissingAPI} too (see {@code API-GAPS.md}). The
 * bar does not apply per-view dynamic titles — those need a live view instance
 * the class-based walker does not give you (gap 3).
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

        RouteConfiguration routeConfiguration = RouteConfiguration
                .forSessionScope();
        List<Class<? extends Component>> chain = RouteHierarchy
                .resolveAncestors(leafView.getClass(), routeConfiguration);

        if (chain.isEmpty()) {
            // Defensive: a non-@Route leaf has no hierarchy to show.
            add(currentCrumb(MissingAPI.staticTitle(leafView.getClass())));
            return;
        }

        RouteParameters parameters = state.routeParameters();
        for (int i = 0; i < chain.size(); i++) {
            if (i > 0) {
                add(separator());
            }
            Class<? extends Component> step = chain.get(i);
            boolean isLeaf = i == chain.size() - 1;
            if (isLeaf) {
                add(currentCrumb(MissingAPI.staticTitle(leafView.getClass())));
            } else {
                add(ancestorLink(step, parameters, routeConfiguration));
            }
        }
    }

    private static RouterLink ancestorLink(Class<? extends Component> ancestor,
            RouteParameters parameters, RouteConfiguration routeConfiguration) {
        String title = MissingAPI.staticTitle(ancestor);
        RouteParameters ancestorParameters = MissingAPI.parametersFor(ancestor,
                parameters, routeConfiguration);
        if (ancestorParameters.getParameterNames().isEmpty()) {
            return new RouterLink(title, ancestor);
        }
        return new RouterLink(title, ancestor, ancestorParameters);
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
