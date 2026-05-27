# Route Hierarchy — use cases

A standalone Spring Boot demo of the route-hierarchy API introduced in Vaadin
Flow [PR #24451](https://github.com/vaadin/flow/pull/24451): the
`@RouteParent` annotation and the `RouteHierarchy` walker
(`resolveAncestors` / `resolveParent`). This module pins Flow to the feature
snapshot `25.2.route-hierarchy-SNAPSHOT` (see its `pom.xml`); the rest of the
repository stays on the baseline `25.2-SNAPSHOT`.

The PR does **not** ship a `Breadcrumbs` component — that lives in
flow-components and consumes this API. Every view here therefore builds its
breadcrumb by hand as a `HorizontalLayout` of `RouterLink`s
(`BreadcrumbBar`), driven entirely by `RouteHierarchy`. See
[`API-GAPS.md`](API-GAPS.md) for what was awkward.

| # | View | What it shows |
| - | ---- | ------------- |
| UC1 | URL-prefix trail | `uc1` → `uc1/electronics` → `uc1/electronics/laptops` with no annotations; the trail comes purely from `RouteHierarchy`'s URL-prefix fallback. |
| UC2 | `@RouteParent` override | A detail page at `order-detail/:orderId` whose URL shares no prefix with its conceptual parent `uc2`; `@RouteParent(OrdersView.class)` is what links them. |
| UC3 | Dynamic leaf label | A profile at `uc3/:userId` implementing `HasDynamicTitle`; the current crumb shows the resolved person name, not the static `@PageTitle`. |
| UC4 | Parameter-preserving links | A four-level parameterised hierarchy where each ancestor link keeps the live `:projectId` (and only the parameters its own template needs). |
| UC5 | Up-one-level button | A single "↑ Up to <parent>" control built from `resolveParent(...)`, hidden at the hierarchy root. |
| UC6 | Layout-wide auto breadcrumbs | One breadcrumb bar in a parent layout, rebuilt on every navigation via `AfterNavigationObserver` (no reactive navigation signal exists in this snapshot). |
| UC7 | Route-tree sitemap | `resolveAncestors` used as a graph-builder: leaf routes from across the demo are expanded and merged into a nested sitemap tree. |

## Run

```
cd route-hierarchy
mvn spring-boot:run
```

Open <http://localhost:8080/>.
