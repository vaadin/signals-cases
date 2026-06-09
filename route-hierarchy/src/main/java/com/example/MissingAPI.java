package com.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteHierarchy;
import com.vaadin.flow.router.RouteParameters;

/**
 * Static shims for the bits of breadcrumb building that the route-hierarchy API
 * from <a href="https://github.com/vaadin/flow/pull/24451">flow#24451</a> does
 * not (yet) cover.
 * <p>
 * {@link RouteHierarchy#resolveAncestors} returns bare
 * {@code Class<? extends Component>} objects. To render a usable breadcrumb you
 * still have to (a) turn each class into a human label and (b) figure out which
 * route parameters each ancestor's template actually needs so its link resolves
 * to a working URL. Both of those are done here. See {@code API-GAPS.md} for
 * the shape these methods suggest the API should grow.
 */
public final class MissingAPI {

    private MissingAPI() {
    }

    /**
     * Resolves the breadcrumb label for a route class: its {@code @PageTitle}
     * value, or the simple class name when absent.
     * <p>
     * Gap: the actual logic already exists in Flow as
     * {@link MenuRegistry#getTitle(Class)} — this method just delegates to it —
     * but {@code MenuRegistry} lives in {@code com.vaadin.flow.internal.menu},
     * so it is unsupported internal API. There is no public entry point (e.g.
     * {@code RouteHierarchy.titleOf(Class)}) to resolve a route class's page
     * title. See gap 1 in {@code API-GAPS.md}.
     * <p>
     * Note this is a purely class-based label: a view that opts into a dynamic
     * title via {@code HasDynamicTitle} carries no {@code @PageTitle}, so it
     * resolves to its bare class name here (see gap 3 and UC3).
     */
    public static String staticTitle(Class<? extends Component> viewClass) {
        return MenuRegistry.getTitle(viewClass);
    }

    /**
     * Builds the subset of {@code available} route parameters that
     * {@code ancestor}'s route template actually declares, so an ancestor
     * {@code RouterLink} resolves without "too many parameters" errors.
     * <p>
     * Gap: {@code resolveAncestors} returns the ancestor classes but says
     * nothing about how the current navigation's parameters map onto each one.
     * Passing the full {@link RouteParameters} to {@code getUrl}/{@code
     * RouterLink} of an ancestor with fewer template segments throws, so each
     * consumer must re-derive the per-ancestor parameter subset from the
     * template — exactly what this method does.
     */
    public static RouteParameters parametersFor(
            Class<? extends Component> ancestor, RouteParameters available,
            RouteConfiguration routeConfiguration) {
        Optional<String> template = routeConfiguration.getTemplate(ancestor);
        if (template.isEmpty()) {
            return RouteParameters.empty();
        }
        Map<String, String> subset = new LinkedHashMap<>();
        for (String segment : template.get().split("/")) {
            if (segment.startsWith(":")) {
                String name = parameterName(segment);
                available.get(name).ifPresent(value -> subset.put(name, value));
            }
        }
        return subset.isEmpty() ? RouteParameters.empty()
                : new RouteParameters(subset);
    }

    /**
     * Extracts the parameter name from a template segment such as
     * {@code :projectId}, {@code :id(int)} or {@code :id?}.
     */
    private static String parameterName(String segment) {
        String name = segment.substring(1);
        int typeStart = name.indexOf('(');
        if (typeStart >= 0) {
            name = name.substring(0, typeStart);
        }
        return name.replaceAll("[?*+]+$", "");
    }
}
