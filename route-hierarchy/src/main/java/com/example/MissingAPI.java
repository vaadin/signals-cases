package com.example;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.internal.menu.MenuRegistry;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteParentReference;
import com.vaadin.flow.router.internal.RouteUtil;

/**
 * Thin wrappers over the route-hierarchy and page-title resolution that
 * <a href="https://github.com/vaadin/flow/pull/24550">flow#24550</a> added on
 * top of <a href="https://github.com/vaadin/flow/pull/24451">flow#24451</a>.
 * <p>
 * With #24550 the heavy lifting now lives in Flow: {@code getRouteHierarchy}
 * walks {@code @RouteParent} (and falls back to the URL prefix) and pairs every
 * entry with the {@link RouteParameters} subset it needs, and
 * {@code MenuRegistry.getTitle(Class, RouteParameters)} resolves a label
 * honouring {@code @PageTitle} <em>and</em> its instance-free
 * {@code PageTitleGenerator}. That closes the per-ancestor parameter and the
 * dynamic-title gaps this class used to fill.
 * <p>
 * What remains a gap (see {@code API-GAPS.md} gap 1) is that the resolution
 * entry points are still <strong>internal</strong> API: {@code RouteUtil} is in
 * {@code com.vaadin.flow.router.internal} and {@code MenuRegistry} in
 * {@code com.vaadin.flow.internal.menu}. This class centralises those internal
 * calls behind one supported-looking surface so the demo views never import
 * {@code internal} directly.
 */
public final class MissingAPI {

    private MissingAPI() {
    }

    /**
     * Resolves the display label for a route class with the given parameters,
     * honouring a static {@code @PageTitle} value or its instance-free
     * {@code PageTitleGenerator}.
     * <p>
     * Wraps {@code MenuRegistry.getTitle(Class, RouteParameters)} — gap 1: the
     * resolver exists but only as internal API.
     */
    public static String titleOf(Class<? extends Component> viewClass,
            RouteParameters parameters) {
        return MenuRegistry.getTitle(viewClass, parameters);
    }

    /**
     * Returns the logical route hierarchy of {@code viewClass} ordered root to
     * leaf (the leaf itself is the last entry), each entry already carrying the
     * {@link RouteParameters} subset its template needs.
     * <p>
     * Wraps {@code RouteUtil.getRouteHierarchy(Class, RouteParameters)} — gap
     * 1: the walker exists but only as internal API.
     */
    public static List<RouteParentReference> trail(
            Class<? extends Component> viewClass, RouteParameters parameters) {
        return RouteUtil.getRouteHierarchy(viewClass, parameters);
    }

    /**
     * Returns the immediate logical parent of {@code viewClass}, or empty when
     * it is a hierarchy root.
     * <p>
     * Wraps {@code RouteUtil.getRouteParent(Class, RouteParameters)} — gap 1:
     * the resolver exists but only as internal API.
     */
    public static Optional<RouteParentReference> parentOf(
            Class<? extends Component> viewClass, RouteParameters parameters) {
        return RouteUtil.getRouteParent(viewClass, parameters);
    }

    /**
     * Narrows the {@link RouteParameters} carried for a hierarchy entry down to
     * only the names that entry's route template actually declares, so building
     * a {@link com.vaadin.flow.router.RouterLink} to it never fails with "no
     * route found for parameters".
     * <p>
     * Gap 2 (residual): {@code getRouteHierarchy} pairs each entry with
     * parameters, and for a <em>URL-derived</em> parent those are already the
     * parent's own subset. But for a <em>static</em> {@code @RouteParent}
     * parent the resolver forwards the child's full parameters unchanged — so
     * an ancestor whose template has fewer (or no) parameters, like UC2's
     * Orders, receives a {@code :orderId} it cannot accept. This filter
     * compensates; Flow filtering the static-parent parameters to the parent
     * template would remove the need for it. See {@code API-GAPS.md}.
     */
    public static RouteParameters linkParameters(
            Class<? extends Component> entry, RouteParameters carried) {
        Optional<String> template = RouteConfiguration.forSessionScope()
                .getTemplate(entry);
        if (template.isEmpty()) {
            return RouteParameters.empty();
        }
        Map<String, String> subset = new LinkedHashMap<>();
        for (String segment : template.get().split("/")) {
            if (segment.startsWith(":")) {
                String name = parameterName(segment);
                carried.get(name).ifPresent(value -> subset.put(name, value));
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
