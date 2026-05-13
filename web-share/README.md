# Web Share API — use cases

A standalone Spring Boot demo of the new `Page#share(...)` and
`Page#shareSupportSignal()` API in Vaadin Flow (PR
[vaadin/flow#24325](https://github.com/vaadin/flow/pull/24325)). Each view
exercises one realistic Web Share scenario.

| # | View | What it shows |
| - | ---- | ------------- |
| UC1 | Share this page | Single Share button reflecting `shareSupportSignal()`; hands current URL + title to the native share sheet. |
| UC2 | Copy-link fallback | Signal-driven swap between native Share button and Copy-link button for browsers without `navigator.share`. |
| UC3 | Share a custom message | Form lets the user fill any of title/text/url, shows a live JSON preview, and shares the result. Empty fields → `null`. |
| UC4 | Per-item share in a list | A feed of three articles, each row with its own Share icon bound to that row's payload. |
| UC5 | Share with completion feedback | Hooks `.then(ok, err)` on the returned `PendingJavaScriptResult` and surfaces success/cancel/error in a log. |
| UC6 | Share an invite link | Generates a fresh join code, builds an invite URL, then shares it. |

## Run

```
cd web-share
mvn spring-boot:run
```

Open <http://localhost:8080/>. Use a mobile browser (or recent
Safari/Edge) for the share sheet to actually appear; desktop Firefox is
the easiest browser to verify the fallback path in UC2.

## Flow snapshot

This module overrides `flow.version` to `25.2.web-share-SNAPSHOT` because
the API lives on the feature branch of vaadin/flow. The published snapshot
needs to include the May 13 commit ("Reshape Web Share API to match Flow
signal/facade patterns") for this module to compile — see `API-GAPS.md`.

## API gaps

See [API-GAPS.md](API-GAPS.md) for everything we wanted but the current
API doesn't expose (file sharing / Web Share Level 2, `navigator.canShare`
pre-check, browserless test simulator, SSR-time feature detection, …).
