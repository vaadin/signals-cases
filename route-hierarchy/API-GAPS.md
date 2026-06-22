# API gaps — route hierarchy + page titles

Rough edges hit while building the use cases on the route-hierarchy and
page-title API. The public building blocks live in `com.vaadin.flow.router`:
the `@RouteParent` annotation (with a dynamic `resolver()`),
`RouteParentResolver` / `RouteParentContext`, `RouteReference` (a navigation
target paired with its `RouteParameters`), and instance-free dynamic titles via
`@DynamicPageTitle` + `PageTitleGenerator` / `PageTitleContext`.

## The resolution entry points are internal API

**Where it bites us:** `BreadcrumbBar`, `UpLink`, `SitemapView`.
**Symptom:** the *building blocks* above are public, but the methods that
actually *resolve* a hierarchy or a title are unsupported **internal** API:

- `com.vaadin.flow.router.internal.RouteUtil#getRouteHierarchy` / `getRouteParent`
- `com.vaadin.flow.internal.menu.MenuRegistry#getTitle(Class, RouteParameters)`

So a breadcrumb/sitemap/up-link consumer either re-implements the walk and the
title reflection, or imports `internal`. The demo does the latter —
`BreadcrumbBar`, `UpLink` and `SitemapView` call `RouteUtil` and `MenuRegistry`
directly, since a one-line wrapper around them would only obscure the
dependency.
**Suggested API:** promote them to a supported surface next to the public
records, e.g. `RouteHierarchy.of(class, params) → List<RouteReference>` and
`RouteHierarchy.titleOf(class, params)` (or fold the title onto the reference
as `RouteReference#title()`).
