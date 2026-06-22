# Route Hierarchy — use cases

A standalone Spring Boot demo of the route-hierarchy API in Vaadin Flow 25.2:
the `@RouteParent` annotation and the `RouteUtil.getRouteHierarchy` /
`getRouteParent` walkers, which return `RouteReference` entries (each a
navigation target paired with its `RouteParameters`).

There is no `Breadcrumbs` component — that lives in flow-components and
consumes this API. Every view here therefore builds its breadcrumb by hand as
a `HorizontalLayout` of `RouterLink`s
(`BreadcrumbBar`), driven by `RouteUtil.getRouteHierarchy`. The bar wires itself to
`UI.routerStateSignal()` via a single `Signal.effect` from its own constructor,
so views never call a `show(...)` method — they just `add(new
BreadcrumbBar())` and the bar rebuilds reactively on every navigation. The same
applies to UC5's `UpLink` and UC6's `TeamLayout`. See
[`API-GAPS.md`](API-GAPS.md) for what was awkward.

| # | View | What it shows |
| - | ---- | ------------- |
| UC1 | URL-prefix trail | `uc1` → `uc1/electronics` → `uc1/electronics/laptops` with no annotations; the trail comes purely from `getRouteHierarchy`'s URL-prefix fallback. |
| UC2 | `@RouteParent` override | A detail page at `order-detail/:orderId` whose URL shares no prefix with its conceptual parent `uc2`; `@RouteParent(OrdersView.class)` is what links them. |
| UC3 | Dynamic leaf label | A profile at `uc3/:userId` implementing `HasDynamicTitle`; the current crumb shows the resolved person name, not the static `@PageTitle`. |
| UC4 | Parameter-preserving links | A four-level parameterised hierarchy where each ancestor link keeps the live `:projectId` (and only the parameters its own template needs). |
| UC5 | Up-one-level button | A single "↑ Up to <parent>" control built from `getRouteParent(...)`, hidden at the hierarchy root. |
| UC6 | Layout-wide auto breadcrumbs | One breadcrumb bar in a parent layout, rebuilt on every navigation via a `Signal.effect` subscribed to `UI.routerStateSignal()` — no `AfterNavigationObserver` and no manual seeding. |
| UC7 | Route-tree sitemap | `getRouteHierarchy` used as a graph-builder: leaf routes from across the demo are expanded and merged into a nested sitemap tree. |

## Run

```
cd route-hierarchy
mvn spring-boot:run
```

Open <http://localhost:8080/>.
