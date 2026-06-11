# API gaps — route hierarchy + page titles (flow#24451, flow#24550)

This module was first built on flow#24451 (the `@RouteParent` annotation and a
`RouteHierarchy` walker) and is now rebuilt on **flow#24550**, which reworks that
feature set: the walker is now `RouteUtil.getRouteParent` /
`getRouteHierarchy` (taking a `RouteRegistry`, each entry paired with the
`RouteParameters` subset it needs), `@RouteParent` gains a dynamic `resolver()`,
and page titles can be **instance-free dynamic** via `@DynamicPageTitle` +
`PageTitleGenerator`.

That closed most of what the first cut had to hand-roll. One gap remains open;
everything else that was previously flagged is now fixed and summarised at the
end. The demo carries no shim of its own any more — it calls the Flow API
directly.

## 1. The resolution entry points are still *internal* API

**Where it bites us:** `BreadcrumbBar`, `UpLink`, `SitemapView`.
**Symptom:** #24550 makes the *building blocks* public — `PageTitleGenerator`,
`PageTitleContext`, `RouteParentReference`, `RouteParentResolver` and
`@DynamicPageTitle` are all in `com.vaadin.flow.router`. But the methods that
actually *resolve* a hierarchy or a title are still unsupported **internal** API:

- `com.vaadin.flow.router.internal.RouteUtil#getRouteHierarchy` / `getRouteParent`
- `com.vaadin.flow.internal.menu.MenuRegistry#getTitle(Class, RouteParameters)`

So a breadcrumb/sitemap/up-link consumer either re-implements the walk and the
title reflection, or imports `internal`. The demo does the latter — `BreadcrumbBar`,
`UpLink` and `SitemapView` call `RouteUtil` and `MenuRegistry` directly, since a
one-line wrapper around them would only obscure the dependency.
**Suggested API:** promote them to a supported surface next to the public
records, e.g. `RouteHierarchy.of(class, params) → List<RouteParentReference>` and
`RouteHierarchy.titleOf(class, params)` (or fold the title onto the reference as
`RouteParentReference#title()`). Ref: flow#24550.

## Previously found, now closed

These gaps the demo originally had to work around no longer exist:

- **Static `@RouteParent` forwarded the child's parameters unchanged.** A static
  `@RouteParent(value = …)` used to hand the parent the child's full
  `RouteParameters`, so UC2's `OrderDetailView` (`order-detail/:orderId`) →
  `OrdersView` (`uc2`, no parameters) made the ancestor `RouterLink` throw
  `NotFoundException`. **Closed** by `getRouteParent` narrowing a static parent's
  parameters to its own template (matching the URL-derived path); the demo's
  `linkParameters` shim — and the whole `MissingAPI` class — is gone.
- **Dynamic page titles needed a view instance.** `HasDynamicTitle#getPageTitle()`
  is an instance method, so a class-based breadcrumb could not show a runtime
  label. **Closed by `@DynamicPageTitle` / `PageTitleGenerator`**, which
  resolves a label from `(class, RouteParameters)` with no instance. A view
  declares its generator with `@DynamicPageTitle(...)` instead of implementing
  `HasDynamicTitle`. UC2/UC3/UC4/UC6 show dynamic leaf crumbs.
- **Ancestor labels could not be dynamic.** An ancestor crumb could only show its
  static `@PageTitle`. **Closed**, because `getRouteHierarchy` carries each
  ancestor's own `RouteParameters` and `MenuRegistry.getTitle(class, params)`
  honours that ancestor's generator — UC4's `ProjectView` reads "Project Apollo"
  as an ancestor of `TaskDetailView`.
- **No per-ancestor parameter mapping.** The walker used to return bare classes,
  leaving the caller to figure out which parameters each ancestor link needed.
  **Closed** by `getRouteHierarchy` pairing every entry with its own subset, for
  both URL-derived and static-`@RouteParent` parents.
- **No reactive "current navigation" signal.** **Closed by
  `UI.routerStateSignal()`** — a read-only `Signal<RouterState>` exposing
  `navigationTarget()`, `location()`, `routeParameters()`, `currentView()` and
  `activeChain()`. `BreadcrumbBar`/`UpLink`/`TeamLayout` subscribe with a single
  `Signal.effect` that rebuilds on every navigation and auto-unsubscribes on
  detach — no `BeforeEnterObserver`, no `AfterNavigationObserver`, no manual seed.
