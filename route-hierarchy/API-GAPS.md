# API gaps — `@RouteParent` + `RouteHierarchy` (flow#24451)

The PR adds exactly two things to Flow core: the `@RouteParent` annotation and
the `RouteHierarchy` walker (`resolveAncestors` / `resolveParent`). That walker
is solid and easy to unit-test — but it stops at *route classes*. Turning a
chain of classes into a usable breadcrumb (or any navigation UI) means filling
several gaps in application code, collected below. The reusable shims live in
`src/main/java/com/example/MissingAPI.java` (gaps 1–3).

There is **no test-simulator gap**: `RouteHierarchy` is pure static logic over a
`RouteConfiguration`, so the browserless tests drive it directly through
`navigate(...)`. The PR's own `RouteHierarchyTest` covers the walking algorithm.

## 1. Walker returns bare classes with no label resolution

**Where it bit us:** every view; `BreadcrumbBar.java`, `MissingAPI.staticTitle`
**Symptom:** `resolveAncestors` returns `List<Class<? extends Component>>` with
no notion of a display label. To render "Catalog › Electronics › Laptops" you
must reach back into each class for its `@PageTitle` (and invent a fallback when
it is absent). Every consumer of the walker re-implements the same reflection.
**Workaround used:** `MissingAPI.staticTitle(Class)` reads `@PageTitle`, else
humanises the simple class name.
**Suggested API:** a label-resolving entry point next to the walker, e.g.
`RouteHierarchy.titleOf(Class<? extends Component>)`, or richer chain entries
(`record RouteHierarchyEntry(Class<?> routeClass, String title, String url)`)
returned by a `resolveTrail(...)` overload — so breadcrumb/sitemap code does not
each re-derive titles. Ref: flow#24451.

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

## 3. Walker never sees the live view instance (no dynamic-title step)

**Where it bit us:** uc2 `OrderDetailView`, uc3 `UserProfileView`, uc4
`TaskDetailView`, uc6 `MemberView`; `MissingAPI.dynamicTitle`
**Symptom:** the breadcrumb's *current* (leaf) crumb usually wants a runtime
label — the loaded entity's name via `HasDynamicTitle#getPageTitle()`. But the
walker operates on classes only, so it cannot apply the current view's dynamic
title; the caller must special-case the leaf with the live instance it happens
to hold.
**Workaround used:** `MissingAPI.dynamicTitle(Component)` checks
`instanceof HasDynamicTitle` on the leaf instance before falling back to the
static title; `BreadcrumbBar.show(Component, RouteParameters)` always takes the
leaf *instance*, not its class, for this reason.
**Suggested API:** let the trail resolver accept the current view instance and
apply `HasDynamicTitle` to the leaf automatically, so dynamic leaf labels are
not every consumer's responsibility. Ref: flow#24451.

**Side note (existing Flow constraint, not a walker gap):** a route class may
declare `@PageTitle` *or* implement `HasDynamicTitle`, never both
(`DuplicateNavigationTitleException` at registry init). So a view that wants a
dynamic breadcrumb leaf must drop its static `@PageTitle` entirely — and then
has no static title left for any *other* consumer that walks to it as an
ancestor (it would fall back to a humanised class name). A walker-side title
hook (gap 1) would side-step this by not depending on `@PageTitle` at all.

## 4. No reactive "current navigation" signal — layout breadcrumbs need a listener

**Where it bit us:** uc6 / `TeamLayout.java`
**Symptom:** for a single breadcrumb bar hosted in a parent layout and shared by
all child views, there is no `Signal<NavigationState>` to subscribe to. You must
implement `AfterNavigationObserver`, rebuild the trail in `afterNavigation`,
pull the leaf instance out of `AfterNavigationEvent#getActiveChain()` and the
parameters out of `getRouteParameters()`, and remember to seed the first render.
**Workaround used:** `TeamLayout implements AfterNavigationObserver` and rebuilds
on every event; a counter badge confirms the rebuild fires once per navigation.
**Suggested API:** the `Signal<NavigationState>` proposed in the breadcrumbs
flow-spec ("Reuse and Proposed Adjustments → a `Signal<NavigationState>` for the
current route"). With it the layout collapses to a single
`Signal.effect(this, () -> breadcrumbs.show(state.viewInstance(), state.routeParameters()))`
that auto-seeds and auto-unsubscribes. (Note: a sibling snapshot already ships
`UI.routerStateSignal()` in the `signals` module — this PR's snapshot does not
include it, so the listener pattern is the only option here.) Ref: flow#24451 and
the breadcrumbs flow-spec.

## 5. Ancestor labels cannot be dynamic

**Where it bit us:** uc4 / `ProjectView` as an ancestor of `TaskDetailView`
**Symptom:** an ancestor crumb can only show its static `@PageTitle` ("Project"),
never "Project Apollo", because the walker hands back the class and there is no
hook to compute an ancestor's label from its parameters or backing data. The
live `:projectId` makes it into the ancestor *link* (gap 2) but not into the
ancestor *label*.
**Workaround used:** none — ancestors show static titles; only the current view
gets a dynamic label (gap 3). A fully dynamic trail would require building it by
hand instead of from the walker.
**Suggested API:** an optional per-ancestor label resolver callback on the trail
resolver, e.g. `resolveTrail(current, params, cfg, (routeClass, params) -> label)`,
so an application can label ancestors from data without abandoning the walker.
Ref: flow#24451.
