package com.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
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
     * Resolves the breadcrumb label for an ancestor route class: its
     * {@link PageTitle} value, or a humanised class name when absent.
     * <p>
     * Gap: {@code RouteHierarchy} hands back classes with no label resolution,
     * so every consumer re-implements this. A
     * {@code RouteHierarchy.titleOf(Class)} (or richer chain entries carrying a
     * resolved title) would remove it.
     */
    public static String staticTitle(Class<? extends Component> viewClass) {
        PageTitle pageTitle = viewClass.getAnnotation(PageTitle.class);
        if (pageTitle != null && !pageTitle.value().isEmpty()) {
            return pageTitle.value();
        }
        return humanize(viewClass.getSimpleName());
    }

    /**
     * Resolves the breadcrumb label for the current (leaf) view instance:
     * {@link HasDynamicTitle#getPageTitle()} when implemented, otherwise the
     * static title.
     * <p>
     * Gap: the walker only sees classes, never the live view instance, so it
     * cannot apply the dynamic-title step the current view may define. The
     * caller has to special-case the leaf, as done here.
     */
    public static String dynamicTitle(Component leafView) {
        if (leafView instanceof HasDynamicTitle dynamic) {
            String title = dynamic.getPageTitle();
            if (title != null && !title.isEmpty()) {
                return title;
            }
        }
        return staticTitle(leafView.getClass());
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

    /**
     * Turns a {@code FooBarView} class name into {@code "Foo Bar"} for use as a
     * fallback breadcrumb label.
     */
    private static String humanize(String className) {
        String name = className;
        if (name.endsWith("View")) {
            name = name.substring(0, name.length() - "View".length());
        }
        String spaced = name.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        return spaced.isEmpty() ? className : spaced;
    }
}
