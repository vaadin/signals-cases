# Fullscreen API — use cases

A standalone Spring Boot demo of the new `Component#requestFullscreen()`,
`Page#requestFullscreen()`, `Page#exitFullscreen()`, and
`Page#fullscreenSignal()` APIs from vaadin/flow#23616. Each view exercises
a single realistic scenario.

| # | View | What it shows |
| - | ---- | ------------- |
| UC1 | Image lightbox | Per-component fullscreen via `Component#requestFullscreen()`; thumbnails enlarge into a stage that fills the viewport. |
| UC2 | Slideshow / presentation | Whole-page fullscreen via `Page#requestFullscreen()` with prev/next shortcuts that keep working in fullscreen. |
| UC3 | Distraction-free editor | TextArea expands to fullscreen; the rest of the app chrome is hidden by the wrapper. |
| UC4 | Reactive layout | A six-card dashboard reformats itself purely through `bindClassName` subscriptions to `fullscreenSignal()` — no observer code. |
| UC5 | Kiosk: detect unexpected exit | Differentiates Escape-initiated exits from programmatic ones by toggling an `expectingExit` flag around `exitFullscreen()`. |
| UC6 | Chart expand | A dashboard of chart cards, each with its own Expand button that fullscreens just that card. |

## Run

```
cd fullscreen
mvn spring-boot:run
```

Open <http://localhost:8080/>.

## Notes

- Browsers require **transient user activation** to enter fullscreen mode.
  Each "request fullscreen" action in these views is wired to a real button
  click; calls from server push or view constructors are no-ops.
- The Fullscreen API in Flow uses a *wrapper* approach: instead of
  fullscreening the target component directly, it fullscreens
  `document.documentElement` and hides the rest of the view. This keeps
  Vaadin theming and overlay components (Notification, ContextMenu,
  ComboBox dropdowns) working in fullscreen.
- See `API-GAPS.md` for the rough edges this module surfaced.

## Flow snapshot

The PR has not landed in mainline yet. `pom.xml` pins
`flow.version=25.2.fullscreen-SNAPSHOT`, which is published to
`maven.vaadin.com/vaadin-prereleases` by the upstream
`fullscreen-api` branch build. Maven resolves it transparently
through the parent's `vaadin-prereleases` repository — no local Flow
build is required.
