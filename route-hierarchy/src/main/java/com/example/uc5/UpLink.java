package com.example.uc5;

import java.util.Optional;

import com.example.MissingAPI;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteHierarchy;
import com.vaadin.flow.router.RouterLink;

/**
 * A single "↑ Up to &lt;parent&gt;" control — the minimal back-navigation
 * helper the route-hierarchy PR names as a target consumer.
 * <p>
 * Unlike a full breadcrumb it only needs the <em>immediate</em> parent, so it
 * calls {@link RouteHierarchy#resolveParent(Class, RouteConfiguration)} rather
 * than {@code resolveAncestors}. When the current view is already a hierarchy
 * root, {@code resolveParent} returns {@link Optional#empty()} and the control
 * renders a plain "top level" note instead of a link.
 */
public class UpLink extends Div {

    public UpLink() {
        addClassName("up-link");
    }

    public void show(Component leafView) {
        removeAll();
        RouteConfiguration routeConfiguration = RouteConfiguration
                .forSessionScope();
        Optional<Class<? extends Component>> parent = RouteHierarchy
                .resolveParent(leafView.getClass(), routeConfiguration);
        if (parent.isPresent()) {
            add(new RouterLink("↑ Up to " + MissingAPI.staticTitle(parent.get()),
                    parent.get()));
        } else {
            add(new Span("You are at the top level."));
        }
    }
}
