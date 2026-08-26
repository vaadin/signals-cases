# API gaps — route hierarchy + page titles

Rough edges hit while building the use cases on the route-hierarchy and
page-title API. The public building blocks live in `com.vaadin.flow.router`:
the `@RouteParent` annotation (with a dynamic `resolver()`),
`RouteParentResolver` / `RouteParentContext`, `RouteReference` (a navigation
target paired with its `RouteParameters`), and instance-free dynamic titles via
`@DynamicPageTitle` + `PageTitleGenerator` / `PageTitleContext`.

Last verified against Vaadin / Flow `25.3-SNAPSHOT` on 2026-08-26.

## ~~The resolution entry points are internal API~~ — resolved in 25.2

**Was:** the *building blocks* were public, but the methods that actually
*resolve* a hierarchy or a title were unsupported **internal** API
(`com.vaadin.flow.router.internal.RouteUtil#getRouteHierarchy` /
`getRouteParent`, and `com.vaadin.flow.internal.menu.MenuRegistry#getTitle`),
so `UpLink` (UC5) and `SitemapView` (UC7) — the two non-breadcrumb consumers —
had to import `internal` to walk the hierarchy and resolve entry titles.

**Now:** both are on the supported surface (`@since 25.2`), shaped almost
exactly as suggested:

```java
// com.vaadin.flow.router.RouteConfiguration
Optional<RouteReference> getRouteParent(Class<? extends Component> target,
        RouteParameters parameters);
List<RouteReference> getRouteHierarchy(Class<? extends Component> target,
        RouteParameters parameters);

// com.vaadin.flow.router.Router
Optional<String> resolvePageTitle(Class<? extends Component> target,
        RouteParameters routeParameters);
Optional<String> resolvePageTitle(Class<? extends Component> target,
        RouteParameters routeParameters, QueryParameters queryParameters);
```

`MenuRegistry.getTitle` is now itself a thin wrapper over
`Router#resolvePageTitle` that falls back to the class simple name, so the only
thing a caller gives up by moving to the public method is that fallback — one
`orElseGet(target::getSimpleName)`.

Both use cases have been updated: `UpLink` calls
`RouteConfiguration.forRegistry(router.getRegistry()).getRouteParent(...)` plus
`router.resolvePageTitle(...)`, and `SitemapView` calls
`getRouteHierarchy(...)` the same way — nothing in this module imports
`com.vaadin.flow.router.internal` or `com.vaadin.flow.internal.menu` any more.

The title is still resolved separately from the reference rather than folded
onto `RouteReference#title()` as originally suggested — deliberately, it looks
like, since resolving a title needs a `VaadinService` while a `RouteReference`
is a plain record. Not a gap.

## No open gaps

Everything the seven use cases needed is reachable from supported API.
