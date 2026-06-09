# API gaps — `@RouteParent` + `RouteHierarchy` (flow#24451)

The PR adds exactly two things to Flow core: the `@RouteParent` annotation and
the `RouteHierarchy` walker (`resolveAncestors` / `resolveParent`). That walker
is solid and easy to unit-test — but it stops at *route classes*. Turning a
chain of classes into a usable breadcrumb (or any navigation UI) means filling
several gaps in application code, collected below. The reusable shims live in
`src/main/java/com/example/MissingAPI.java` (gaps 1–2). Gap 3 (dynamic titles)
is a deliberate non-goal here, explained below.

There is **no test-simulator gap**: `RouteHierarchy` is pure static logic over a
`RouteConfiguration`, so the browserless tests drive it directly through
`navigate(...)`. The PR's own `RouteHierarchyTest` covers the walking algorithm.

## 1. No *public* API to resolve a page title from a route class

**Where it bit us:** every view; `BreadcrumbBar.java`, `MissingAPI.staticTitle`,
`UpLink`, `SitemapView`
**Symptom:** `resolveAncestors` returns `List<Class<? extends Component>>` with
no notion of a display label. To render "Catalog › Electronics › Laptops" you
must turn each class into its `@PageTitle` (with a fallback when absent). Flow
*already has this logic* — `MenuRegistry.getTitle(Class<? extends Component>)`
reads `@PageTitle` and falls back to `getSimpleName()` — but `MenuRegistry`
lives in `com.vaadin.flow.internal.menu`, i.e. unsupported **internal** API.
There is no supported entry point to resolve a route class's page title, so
either every consumer re-implements the reflection or (as here) reaches into
internal API.
**Workaround used:** `MissingAPI.staticTitle(Class)` delegates straight to
`MenuRegistry.getTitle(Class)`, accepting the internal-API risk to avoid
re-deriving titles in `BreadcrumbBar`, `UpLink` and `SitemapView`.
**Suggested API:** promote the existing helper to a supported entry point next
to the walker, e.g. `RouteHierarchy.titleOf(Class<? extends Component>)`, or
richer chain entries
(`record RouteHierarchyEntry(Class<?> routeClass, String title, String url)`)
returned by a `resolveTrail(...)` overload — so breadcrumb/sitemap code does not
re-derive titles nor depend on `internal`. Ref: flow#24451.

## 2. No mapping from the current parameters onto each ancestor's template

**Where it bit us:** uc4 / `ProjectsView`…`TaskDetailView.java`;
`MissingAPI.parametersFor`
**Symptom:** for a parameterised trail
(`uc4/:projectId/tasks/:taskId`), `resolveAncestors` gives the ancestor classes
but says nothing about which of the current navigation's `RouteParameters` each
ancestor needs. Passing the full `RouteParameters` to an ancestor with fewer
template segments throws (`getUrl`/`RouterLink` reject unexpected parameters),
and passing none produces broken links that drop the project context.
**Workaround used:** `MissingAPI.parametersFor(ancestor, available, cfg)` reads
the ancestor's template via `RouteConfiguration#getTemplate`, parses out its
`:name` segments, and keeps only those parameters.
**Suggested API:** a trail resolver that returns each ancestor already paired
with a working URL given the current `RouteParameters` — e.g.
`RouteHierarchy.resolveAncestorUrls(currentClass, RouteParameters, cfg)` →
`List<RouteHierarchyEntry>` with `url` filled in. The template-parameter parsing
is exactly the kind of detail Flow core should own. Ref: flow#24451.

## 3. Dynamic page titles can't feed a class-based breadcrumb (by design)

**Where it bit us:** uc3 `UserProfileView` (kept as a deliberate showcase);
previously uc2 `OrderDetailView`, uc4 `TaskDetailView`, uc6 `MemberView` (now
static `@PageTitle`)
**Symptom:** a breadcrumb leaf often *wants* a runtime label — the loaded
entity's name via `HasDynamicTitle#getPageTitle()`. But `getPageTitle()` is an
**instance** method, and the walker yields **classes**. Reading it would mean
holding a live instance of the view, and we deliberately **do not instantiate
views just to read a title**. So this module resolves *every* crumb uniformly
from its route class (gap 1): dynamic titles are out of scope. A view that opts
into `HasDynamicTitle` therefore carries no `@PageTitle` to fall back to and
surfaces its bare class name in the trail — uc3 leaves this in on purpose, so
its breadcrumb leaf reads `UserProfileView` even though the H1 / browser tab
title show the resolved person name.
**Why not just use the leaf instance?** `UI.routerStateSignal().currentView()`
*does* hand the breadcrumb the leaf instance, so the leaf alone could apply
`HasDynamicTitle`. We chose not to: resolving the leaf differently from its
ancestors is inconsistent, and it still does nothing for ancestor crumbs that
want a dynamic label (gap 5), where no instance exists at all.
**Closest existing helper:** `RouteUtil.getDynamicTitle(UI)` (static, but
`com.vaadin.flow.router.internal`) reads the dynamic title off the live UI's
active chain — but it is internal and resolves *the current UI's* title, not an
arbitrary class/instance you hand it, so it does not generalise to a trail.
**Suggested API:** there is no clean class-based fix — dynamic titles are
inherently instance-bound. A breadcrumb-friendly route would instead need a
`(routeClass, RouteParameters) -> label` resolution hook (see gap 5) rather than
relying on `HasDynamicTitle`. Ref: flow#24451.

**Side note (existing Flow constraint):** a route class may declare `@PageTitle`
*or* implement `HasDynamicTitle`, never both (`DuplicateNavigationTitleException`
at registry init). So a view that wants a dynamic page title has no static
title left for any consumer that resolves it from its class — exactly what makes
uc3's leaf fall back to the class name. This is the root of the gap above.

## 4. ~~No reactive "current navigation" signal~~ — closed by `UI.routerStateSignal()`

**Status:** closed. This module's snapshot now carries the
`UI.routerStateSignal()` from the breadcrumbs flow-spec
("Reuse and Proposed Adjustments → a `Signal<NavigationState>` for the current
route"), exposing a read-only `Signal<RouterState>` with `navigationTarget()`,
`location()`, `routeParameters()`, `currentView()` and `activeChain()`.
**How it landed in the demo:** every breadcrumb is signal-bound. `BreadcrumbBar`
subscribes in its constructor via a single `Signal.effect(this, () -> { var
state = UI.getCurrent().routerStateSignal().get(); rebuild(state); })` — the
effect seeds the first render, auto-fires on every navigation (including
same-class navigations with new `RouteParameters`) and auto-unsubscribes on
detach. Views just `add(new BreadcrumbBar())`; no `BeforeEnterObserver`, no
`AfterNavigationObserver`, no manual seed step. UC6's `TeamLayout` follows the
same pattern for its rebuild counter. `UpLink` (UC5) is wired identically.
**Note:** gaps 1, 2 and 5 remain — the signal hands you the leaf instance and
the current `RouteParameters`, but `RouteHierarchy.resolveAncestors` still
returns bare classes, so (public) label resolution and per-ancestor parameter
filtering stay an application responsibility. Gap 3 (dynamic titles) is closed
here as a deliberate non-goal, not by the signal.

## 5. Ancestor labels cannot be dynamic

**Where it bit us:** uc4 / `ProjectView` as an ancestor of `TaskDetailView`
**Symptom:** an ancestor crumb can only show its static `@PageTitle` ("Project"),
never "Project Apollo", because the walker hands back the class and there is no
hook to compute an ancestor's label from its parameters or backing data. The
live `:projectId` makes it into the ancestor *link* (gap 2) but not into the
ancestor *label*.
**Workaround used:** none — ancestors show static titles. This shares its root
cause with gap 3: the walker hands back classes, and dynamic labels are
instance-bound. A fully dynamic trail would require building it by hand instead
of from the walker.
**Suggested API:** an optional per-ancestor label resolver callback on the trail
resolver, e.g. `resolveTrail(current, params, cfg, (routeClass, params) -> label)`,
so an application can label ancestors from data without abandoning the walker.
Ref: flow#24451.
