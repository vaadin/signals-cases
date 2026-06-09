# API gaps — route hierarchy + page titles (flow#24451, flow#24550)

This module was first built on flow#24451 (the `@RouteParent` annotation and a
`RouteHierarchy` walker) and is now rebuilt on **flow#24550**, which reworks that
feature set:

- `RouteHierarchy` is gone; the walker is now
  `RouteUtil.getRouteParent(class, params)` /
  `RouteUtil.getRouteHierarchy(class, params)` — still `@RouteParent`-first with a
  URL-prefix fallback, but each returned `RouteParentReference` is **paired with
  the `RouteParameters` subset that entry needs**.
- `@RouteParent` gains a dynamic `resolver()` (a `RouteParentResolver`) next to
  the static `value()`.
- Page titles can now be **instance-free dynamic**: `@PageTitle(generator = ...)`
  points at a `PageTitleGenerator` that maps `(class, RouteParameters)` to a
  label, and `MenuRegistry.getTitle(class, params)` resolves it.

That closes most of the gaps the first cut had to fill in application code. What
remains lives in `src/main/java/com/example/MissingAPI.java`, which now only
wraps the (still internal) resolution entry points and patches one residual
parameter bug. The walker is pure static logic over the route registry, so the
browserless tests drive it directly through `navigate(...)`.

## 1. The resolution entry points are still *internal* API — OPEN (narrowed)

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

## 2. Static `@RouteParent` forwards the child's parameters unchanged — OPEN (residual)

**Where it bit us:** uc2 `OrderDetailView` → `OrdersView`;
`MissingAPI.linkParameters`
**Status:** the *general* "which parameters does each ancestor need" gap is
**closed** — `getRouteHierarchy` pairs every entry with a `RouteParameters`
subset, and for a **URL-derived** parent that subset is exactly the parent's own
parameters. The residual bug is the **static `@RouteParent`** path:
`getRouteParent` resolves `@RouteParent(value = …)` by handing the parent *the
child's full `RouteParameters`* (`new RouteParentReference(value, parameters)`),
without narrowing them to the parent's template.
**Symptom:** UC2's `OrderDetailView` (`order-detail/:orderId`) declares
`@RouteParent(OrdersView.class)`, and `OrdersView` (`uc2`) takes no parameters.
The trail hands `OrdersView` a `{orderId=…}` it cannot accept, so building its
`RouterLink` throws `NotFoundException: No route found for … OrdersView and
parameters '{orderId=1001}'`.
**Workaround used:** `MissingAPI.linkParameters(entry, carried)` filters the
carried parameters down to the names the entry's own template declares before the
link is built. (For URL-derived ancestors this is a harmless no-op; it only
matters for the static-parent case.)
**Suggested API:** in `getRouteParent`, narrow a static parent's parameters to
its template — i.e. apply the same subset logic the URL-derived path already
does — so a static `@RouteParent` whose parent has fewer parameters resolves to a
working link with no caller-side filtering. Ref: flow#24550.

## 3. ~~Dynamic page titles can't feed a class-based breadcrumb~~ — closed by `PageTitleGenerator`

**Status:** closed by flow#24550. The earlier cut had to drop dynamic breadcrumb
labels because `HasDynamicTitle#getPageTitle()` is an **instance** method and the
walker only has classes — and we would not instantiate a view just to read a
title. `@PageTitle(generator = …)` removes that constraint: a
`PageTitleGenerator` resolves the label from `(class, RouteParameters)` with **no
instance**, and `MenuRegistry.getTitle(class, params)` honours it.
**How it landed in the demo:** UC2 (`OrderTitleGenerator` → "Order #1234"), UC3
(`UserProfileTitleGenerator` → "Ada Lovelace"), UC4 (`TaskTitleGenerator`) and
UC6 (`MemberTitleGenerator`) all show a dynamic **leaf** crumb resolved purely
from the route parameters. The breadcrumb is still 100% class-based; it never
touches a view instance.
**Dissolved side note:** a class used to be forced to choose `@PageTitle` *or*
`HasDynamicTitle` (`DuplicateNavigationTitleException`). The dynamic title now
rides *on* `@PageTitle` via `generator()`, so there is no either/or and a
class-based resolver always has something to read.

## 4. ~~No reactive "current navigation" signal~~ — closed by `UI.routerStateSignal()`

**Status:** closed. The snapshot carries `UI.routerStateSignal()`, a read-only
`Signal<RouterState>` with `navigationTarget()`, `location()`,
`routeParameters()`, `currentView()` and `activeChain()`.
**How it landed in the demo:** every breadcrumb is signal-bound. `BreadcrumbBar`
subscribes in its constructor via a single `Signal.effect(this, () -> { var
state = UI.getCurrent().routerStateSignal().get(); rebuild(state); })` — the
effect seeds the first render, auto-fires on every navigation (including
same-class navigations with new `RouteParameters`) and auto-unsubscribes on
detach. Views just `add(new BreadcrumbBar())`; no `BeforeEnterObserver`, no
`AfterNavigationObserver`, no manual seed step. UC6's `TeamLayout` follows the
same pattern for its rebuild counter; `UpLink` (UC5) is wired identically.
**Note:** gaps 1 and 2 remain — the signal hands you the leaf class and the
current `RouteParameters`, but the resolvers behind `MissingAPI` are still
internal (gap 1) and the static-parent parameter bug still needs the
`linkParameters` patch (gap 2).

## 5. ~~Ancestor labels cannot be dynamic~~ — closed by `PageTitleGenerator` + per-ancestor params

**Status:** closed by flow#24550, and it is the headline win. Because
`getRouteHierarchy` carries the `RouteParameters` *for each ancestor* and
`MenuRegistry.getTitle(class, params)` honours that ancestor's
`PageTitleGenerator`, an **ancestor** crumb can now be dynamic too — not just the
leaf.
**How it landed in the demo:** UC4's `ProjectView` (`uc4/:projectId`) declares
`@PageTitle(generator = ProjectTitleGenerator.class)`, so when it sits *as an
ancestor* of `TaskDetailView` its crumb reads "Project Apollo" — resolved from
the `:projectId` the trail carried for that crumb — instead of a static
"Project". No instance, no per-consumer callback.
**Suggested API:** none outstanding; the public `PageTitleGenerator` +
per-ancestor `RouteParameters` is exactly the `(routeClass, params) -> label`
hook the earlier draft asked for. Ref: flow#24550.
