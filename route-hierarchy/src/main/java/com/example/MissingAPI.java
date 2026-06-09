package com.example;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteParameters;

/**
 * The one bit of breadcrumb building that
 * <a href="https://github.com/vaadin/flow/pull/24550">flow#24550</a> does not
 * get right on its own.
 * <p>
 * The hierarchy walking ({@code RouteUtil.getRouteHierarchy} /
 * {@code getRouteParent}) and the label resolution
 * ({@code MenuRegistry.getTitle(class, params)}) are called directly by the
 * demo views — they are still internal API, but wrapping a one-line forward
 * adds nothing. What does need a shim is the residual bug below.
 */
public final class MissingAPI {

    private MissingAPI() {
    }

    /**
     * Narrows the {@link RouteParameters} carried for a hierarchy entry down to
     * only the names that entry's route template actually declares, so building
     * a {@link com.vaadin.flow.router.RouterLink} to it never fails with "no
     * route found for parameters".
     * <p>
     * Gap: {@code getRouteHierarchy} pairs each entry with parameters, and for
     * a <em>URL-derived</em> parent those are already the parent's own subset.
     * But for a <em>static</em> {@code @RouteParent} parent the resolver
     * forwards the child's full parameters unchanged — so an ancestor whose
     * template has fewer (or no) parameters, like UC2's Orders, receives a
     * {@code :orderId} it cannot accept. This filter compensates; Flow
     * filtering the static-parent parameters to the parent template would
     * remove the need for it. See {@code API-GAPS.md}.
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
