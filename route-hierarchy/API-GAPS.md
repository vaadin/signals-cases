# API gaps — route hierarchy + page titles

Rough edges hit while building the use cases on the route-hierarchy and
page-title API. The public building blocks live in `com.vaadin.flow.router`:
the `@RouteParent` annotation (with a dynamic `resolver()`),
`RouteParentResolver` / `RouteParentContext`, `RouteReference` (a navigation
target paired with its `RouteParameters`), and instance-free dynamic titles via
`@DynamicPageTitle` + `PageTitleGenerator` / `PageTitleContext`.

## The resolution entry points are internal API

**Where it bites us:** `UpLink` (UC5), `SitemapView` (UC7).
**Symptom:** the *building blocks* above are public, but the methods that
actually *resolve* a hierarchy or a title are unsupported **internal** API:

- `com.vaadin.flow.router.internal.RouteUtil#getRouteHierarchy` / `getRouteParent`
- `com.vaadin.flow.internal.menu.MenuRegistry#getTitle(Class, RouteParameters)`

The standard breadcrumb case is now covered: the `Breadcrumbs` component
(`ROUTER` mode) consumes this internal API for us, so UC1–UC4 and UC6 just
`add(new Breadcrumbs())` with no `internal` import. But any *other* consumer of
the raw hierarchy still has to re-implement the walk and the title reflection,
or import `internal`. The two non-breadcrumb use cases do the latter — `UpLink`
calls `RouteUtil.getRouteParent` and `SitemapView` calls
`RouteUtil.getRouteHierarchy` + `MenuRegistry.getTitle` directly, since a
one-line wrapper around them would only obscure the dependency.
**Suggested API:** promote them to a supported surface next to the public
records, e.g. `RouteHierarchy.of(class, params) → List<RouteReference>` and
`RouteHierarchy.titleOf(class, params)` (or fold the title onto the reference
as `RouteReference#title()`).

## A flat menu entry does not say where it sits in the tree

**Where it bites us:** `MainLayout#includeInMainNav` (UC8).
**Symptom:** `MenuConfiguration.getMenuEntriesTree()` + `MenuEntry#children()`
give a *nested* view of the `@Menu` set — public, supported API in
`com.vaadin.flow.server.menu`, and the whole of UC8. But the flat
`getMenuEntries()` that a normal side nav is built from still hands back every
entry with no indication of its depth, so a layout that wants "top-level items
only" (leaving the deeper ones to UC8's tree) has to work the nesting out for
itself. `MainLayout` does that by calling the same **internal**
`RouteUtil.getRouteParent` as the gap above — comparing URL path prefixes is not a
substitute, because a logical parent need not share a URL prefix.
**Suggested API:** either give `MenuEntry` a `parent()`/depth of its own, or let
the caller flatten the tree itself — e.g. `MenuEntry#descendants()` — so that
"top-level only" is a filter on public API rather than a second walk through
`internal`.
