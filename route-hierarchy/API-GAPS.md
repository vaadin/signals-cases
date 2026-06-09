# API gaps — route hierarchy + page titles (flow#24451, flow#24550)

This module was first built on flow#24451 (the `@RouteParent` annotation and a
`RouteHierarchy` walker) and is now rebuilt on **flow#24550**, which reworks that
feature set: the walker is now `RouteUtil.getRouteParent` /
`getRouteHierarchy` (each entry paired with the `RouteParameters` subset it
needs), `@RouteParent` gains a dynamic `resolver()`, and page titles can be
**instance-free dynamic** via `@PageTitle(generator = ...)` +
`PageTitleGenerator`.

That closed most of what the first cut had to hand-roll. Two gaps remain open;
everything else that was previously flagged is now fixed and summarised at the
end. The reusable shims live in `src/main/java/com/example/MissingAPI.java`.

## 1. The resolution entry points are still *internal* API

**Where it bites us:** every view, via `MissingAPI`
(`BreadcrumbBar`, `UpLink`, `SitemapView`).
**Symptom:** #24550 makes the *building blocks* public — `PageTitleGenerator`,
`PageTitleContext`, `RouteParentReference`, `RouteParentResolver` and
`@PageTitle(generator)` are all in `com.vaadin.flow.router`. But the methods that
actually *resolve* a hierarchy or a title are still unsupported **internal** API:

- `com.vaadin.flow.router.internal.RouteUtil#getRouteHierarchy` / `getRouteParent`
- `com.vaadin.flow.internal.menu.MenuRegistry#getTitle(Class, RouteParameters)`

So a breadcrumb/sitemap/up-link consumer either re-implements the walk and the
title reflection, or (as here) imports `internal`.
**Workaround used:** `MissingAPI` centralises those three internal calls behind
`trail(...)`, `parentOf(...)` and `titleOf(...)` so no demo view imports
`internal` directly.
**Suggested API:** promote them to a supported surface next to the public
records, e.g. `RouteHierarchy.of(class, params) → List<RouteParentReference>` and
`RouteHierarchy.titleOf(class, params)` (or fold the title onto the reference as
`RouteParentReference#title()`). Ref: flow#24550.

## 2. Static `@RouteParent` forwards the child's parameters unchanged

**Where it bit us:** uc2 `OrderDetailView` → `OrdersView`;
`MissingAPI.linkParameters`
**Symptom:** `getRouteHierarchy` pairs every entry with a `RouteParameters`
subset, and for a **URL-derived** parent that subset is exactly the parent's own
parameters. But the **static `@RouteParent`** path does not narrow them:
`getRouteParent` resolves `@RouteParent(value = …)` by handing the parent *the
child's full `RouteParameters`* (`new RouteParentReference(value, parameters)`).
UC2's `OrderDetailView` (`order-detail/:orderId`) declares
`@RouteParent(OrdersView.class)`, and `OrdersView` (`uc2`) takes no parameters —
so the trail hands `OrdersView` a `{orderId=…}` it cannot accept and building its
`RouterLink` throws `NotFoundException: No route found for … OrdersView and
parameters '{orderId=1001}'`.
**Workaround used:** `MissingAPI.linkParameters(entry, carried)` filters the
carried parameters down to the names the entry's own template declares before the
link is built. (For URL-derived ancestors this is a harmless no-op; it only
matters for the static-parent case.)
**Suggested API:** in `getRouteParent`, narrow a static parent's parameters to
its template — the same subset logic the URL-derived path already does — so a
static `@RouteParent` whose parent has fewer parameters resolves to a working
link with no caller-side filtering. Ref: flow#24550.

## Previously found, now closed

These gaps the demo originally had to work around no longer exist:

- **Dynamic page titles needed a view instance.** `HasDynamicTitle#getPageTitle()`
  is an instance method, so a class-based breadcrumb could not show a runtime
  label. **Closed by `@PageTitle(generator = ...)` / `PageTitleGenerator`**, which
  resolves a label from `(class, RouteParameters)` with no instance. The old
  `@PageTitle`-*or*-`HasDynamicTitle` exclusivity also dissolves — the dynamic
  title now rides on `@PageTitle`. UC2/UC3/UC4/UC6 show dynamic leaf crumbs.
- **Ancestor labels could not be dynamic.** An ancestor crumb could only show its
  static `@PageTitle`. **Closed**, because `getRouteHierarchy` carries each
  ancestor's own `RouteParameters` and `MenuRegistry.getTitle(class, params)`
  honours that ancestor's generator — UC4's `ProjectView` reads "Project Apollo"
  as an ancestor of `TaskDetailView`.
- **No per-ancestor parameter mapping.** The walker used to return bare classes,
  leaving the caller to figure out which parameters each ancestor link needed.
  **Closed** by `getRouteHierarchy` pairing every entry with its subset (the only
  residual is the static-`@RouteParent` case, gap 2 above).
- **No reactive "current navigation" signal.** **Closed by
  `UI.routerStateSignal()`** — a read-only `Signal<RouterState>` exposing
  `navigationTarget()`, `location()`, `routeParameters()`, `currentView()` and
  `activeChain()`. `BreadcrumbBar`/`UpLink`/`TeamLayout` subscribe with a single
  `Signal.effect` that rebuilds on every navigation and auto-unsubscribes on
  detach — no `BeforeEnterObserver`, no `AfterNavigationObserver`, no manual seed.
