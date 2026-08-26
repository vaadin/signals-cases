# API gaps — route hierarchy + page titles

Rough edges hit while building the use cases on the route-hierarchy and
page-title API. The public building blocks live in `com.vaadin.flow.router`:
the `@RouteParent` annotation (with a dynamic `resolver()`),
`RouteParentResolver` / `RouteParentContext`, `RouteReference` (a navigation
target paired with its `RouteParameters`), the resolution entry points
`RouteConfiguration#getRouteParent` / `#getRouteHierarchy`, and instance-free
dynamic titles via `@DynamicPageTitle` + `PageTitleGenerator` /
`PageTitleContext`. The menu counterpart is
`MenuConfiguration#getMenuEntriesTree()` + `MenuEntry#children()` in
`com.vaadin.flow.server.menu`.

## Resolving an ancestor's title is internal API

**Where it bites us:** `UpLink` (UC5), `SitemapView` (UC7).
**Symptom:** resolving the *hierarchy* is public — `UpLink` calls
`RouteConfiguration#getRouteParent` and `SitemapView` calls
`#getRouteHierarchy`, both returning public `RouteReference` records. But
turning one of those references into a **label** is not: the only lookup that
resolves a route's title from `(class, RouteParameters)` — honouring
`@PageTitle`, `@DynamicPageTitle` and `PageTitleGenerator` without
instantiating the view — is

- `com.vaadin.flow.internal.menu.MenuRegistry#getTitle(Class, RouteParameters)`

The standard breadcrumb case is covered: the `Breadcrumbs` component
(`ROUTER` mode) consumes this internally for us, so UC1–UC4 and UC6 just
`add(new Breadcrumbs())` with no `internal` import. Any *other* consumer that
builds its own control out of the now-public walker — an up-link, a sitemap, a
custom nav — gets the structure from supported API and then has to reach into
`internal` for every label, or re-implement the title reflection itself.
**Suggested API:** expose the same lookup next to the public records, e.g.
`RouteConfiguration#getRouteTitle(Class, RouteParameters)`, or fold it onto the
reference as `RouteReference#title()` so that a resolved hierarchy is directly
renderable.
