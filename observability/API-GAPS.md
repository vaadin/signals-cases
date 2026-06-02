# API gaps — Observability (`prototype/vaadin-micrometer`)

This module is a *consumer-side* exploration of observability for Vaadin Flow.
The baseline is Flow's `prototype/vaadin-micrometer` branch, which already adds a
`vaadin-micrometer` module: server-side binders driven by `VaadinRequestInterceptor`,
`SessionInitListener`, `UIInitListener` and a session-lock listener; a Micrometer
Observation/tracing path; and an in-browser collector (`VaadinMetricsClient.js`)
bridged to the server through a hidden `<vaadin-metrics-collector>` element's
`@ClientCallable`. It is enabled out of the box via `vaadin.metrics.*` Spring Boot
properties (all signals default `true`).

That branch is good and covers most of the use cases in
[#277](https://github.com/vaadin/use-cases/issues/277). The gaps below are what
remains *missing or awkward even with that branch in place* — i.e. the things the
demo can't do cleanly, and the Flow API changes they argue for. Reusable shims, where
one is possible, live in `src/main/java/com/example/MissingAPI.java`.

Meters referenced below use the branch's `MeterNames` constants (e.g.
`vaadin.request.duration`, `vaadin.client.rpc.duration`).

**Reachability is not the problem.** The in-browser collector POSTs its samples
back via the `<vaadin-metrics-collector>` `@ClientCallable`, and `ClientMetricsBinder`
records them into the *same server-side `MeterRegistry`* every other binder uses. So
the client meters (`vaadin.client.rpc.duration`, `vaadin.client.errors`,
`vaadin.client.web_vitals.*`, …) are readable by application code exactly like the
server meters — a Flow view can inject the `MeterRegistry` and read them today. The
gaps below are therefore about **granularity, correlation, and completeness of those
meters**, not about getting client data to the server.

## 1. No public client-side request-lifecycle hook

**Where it bites:** UC1 (end-to-end responsiveness); the whole client collector.
**Symptom:** there is no supported way for application/add-on code to learn when a
UIDL request starts and ends in the browser. The internal `RequestResponseTracker`
fires `RequestStartingEvent` / `ResponseHandlingEndedEvent`, but on a GWT-internal
`EventBus` with Java-only handlers and no `@JsExport` — invisible to JS. As a result
the prototype's `VaadinMetricsClient.js` **monkey-patches `XMLHttpRequest.prototype.send`**
and identifies UIDL traffic by string-matching the `v-r=uidl` query parameter. That is
an implementation detail, not a contract: it breaks if the marker changes, and it
only sees `XMLHttpRequest`.
**Workaround used:** none clean; the collector patches `XHR`.
**Suggested API:** a first-class client hook, e.g.
`window.Vaadin.Flow.addRequestListener({ onRequestStart, onResponseReceived, onRendered })`,
emitting per-request timestamps, request/response sizes, transport, outcome, and a
correlation id — surfacing the events `RequestResponseTracker` already fires.

## 2. Client RPC timing is available, but excludes apply/paint and has no "rendered" signal

**Where it bites:** UC1 (the browser segment of perceived latency).
**Symptom:** `vaadin.client.rpc.duration` *is* collected and readable — but it is measured
from XHR `send` to `loadend`, so it captures network-up + server + network-down and
**not** the time Flow then spends applying the UIDL diff to the DOM and painting.
"Click-to-rendered" — the number the user actually feels — is therefore not measurable
even though the metric exists. The engine *does* measure render time (TestBench reads
`timeSpentRenderingLastRequest()`), but it is not on any public JS surface.
**Workaround used:** none; the apply/paint segment is simply absent from the sample.
**Suggested API:** an `onRendered` timestamp on the hook from gap #1 (fired after the
UIDL response has been applied), and/or exposing the engine's existing render timing to
production client code.

## 3. Client samples are aggregated and uncorrelated — no per-interaction value

**Where it bites:** UC1 (browser/network/server breakdown), UC4 (single trace).
**Symptom:** two distinct limitations, both about *granularity*, not availability:
1. **Aggregated on ingest.** `ClientMetricsBinder.ingest` folds each browser sample into
   a rolling `Timer` (`registry.timer(name, tags).record(...)`); the per-sample values the
   JS buffers (each with its own `ts`/`valueMs`) are discarded. So we can read the *mean/
   max/count* of `vaadin.client.rpc.duration`, but never *this click's* round-trip.
2. **No correlation id.** `vaadin.client.rpc.duration` and the server
   `vaadin.request.duration` share no identifier, so a single slow interaction can't be
   attributed to client vs. network vs. server — only aggregates can be subtracted.
   Relatedly, the collector sends no W3C `traceparent` on the UIDL request, so the
   server-side trace (`vaadin.request` observation) doesn't descend from a browser-rooted
   span — the trace effectively starts on the server, not at the click.
**Workaround used:** aggregate subtraction only (mean client round-trip − mean server
duration); no per-interaction stitch.
**Suggested API:** a per-UIDL-request correlation id exposed to both the client hook
(gap #1) and the server request interceptor, ideally as a W3C `traceparent` the client
injects and the server continues, so browser → server → backend is one trace.

## 4. Push / WebSocket transport is not instrumented on the client

**Where it bites:** UC1, UC2 (apps using `@Push`).
**Symptom:** the client collector only wraps `XMLHttpRequest`, so server-initiated
updates delivered over WebSocket/long-poll (`@Push`) produce no client-side timing or
outcome samples. For push-heavy apps the client view of responsiveness is blind.
**Workaround used:** none.
**Suggested API:** the client hook (#1) should be transport-agnostic, covering the
push connection, not just XHR UIDL requests.

## 5. No connection-state metric, despite the client API existing

**Where it bites:** UC5 (connection lost / reconnecting).
**Symptom:** the prototype collects client *errors* but not connection-state
transitions. Yet `window.Vaadin.connectionState` already exposes
`online`/`offline`, a `state` (`CONNECTED` / `CONNECTION_LOST` / `RECONNECTING`), and
`addStateChangeListener(...)`. The information is one listener away but is neither
collected by the branch nor surfaced as a meter.
**Workaround used (possible):** an app-level shim that subscribes to
`window.Vaadin.connectionState.addStateChangeListener` and reports transitions to the
server via a `@ClientCallable`, recorded as a `vaadin.client.connection.state` meter —
a candidate for `MissingAPI`.
**Suggested API:** have the collector record connection-state transitions out of the
box (e.g. a `vaadin.client.connection` gauge/counter tagged by state), since the
client store already drives them.

## 6. No server-side UI-state-size / component-tree metric

**Where it bites:** UC3 (capacity & scaling).
**Symptom:** the branch reports session and UI *counts* and session-lock contention,
but not how much state each UI holds — component-tree node count or per-session heap
footprint. Because Flow keeps UI state in server memory, *size* (not just count) is the
signal that predicts when a server-driven app must scale, and it is missing.
**Workaround used (possible):** walk the UI's element tree from the server
(`UI.getElement()` / state node tree) to approximate a node count — a `MissingAPI`
candidate, though heap footprint is not derivable this way.
**Suggested API:** a binder for per-UI state size (node count, and ideally an estimated
retained size), bounded for cardinality.

## 7. Server request timing is already computed and transported — but unusable

**Where it bites:** UC1, UC2.
**Symptom:** `UidlWriter` already attaches `"timings": [cumulative, last]` to the UIDL
response (from `VaadinSession#getLastRequestDuration()`), and the client `MessageHandler`
receives it — but only `Console.debug`-logs it into a private field, and the whole thing
is gated by `isRequestTiming()`, which defaults to `!productionMode` (**off in
production**). So the server's own measurement of each request, already shipped to the
browser, is reachable by neither application server code (cleanly) nor client code, and
the prototype re-measures instead of reusing it.
**Workaround used:** the prototype measures server time separately via
`VaadinRequestInterceptor`, and measures round-trip separately on the client.
**Suggested API:** a production-safe, public accessor for per-request server duration on
both sides (e.g. include it in the client hook's response event), so collectors need not
re-measure or parse debug logs.

## 8. Interaction granularity stops at "rpc"

**Where it bites:** UC1 (which action is slow?), UC6 (errors by view/action).
**Symptom:** the request observation tags an interaction as `poll` / `navigation` /
`rpc` (`VaadinObservationNames.KEY_INTERACTION`) but, per its own comment, "cannot break
down further without parsing the UIDL body." So latency and errors can be attributed to
a *route* but not to a specific component or event ("the Save button on OrdersView").
Server error counting (`vaadin.errors`) is tagged by exception type, not by route/view.
**Workaround used:** manual per-listener instrumentation (wrap individual click/value
handlers) is the only way to get action-level granularity today.
**Suggested API:** a server-side hook around RPC invocation carrying the source
component and event type, so interaction latency/errors can be tagged by action without
parsing UIDL or hand-wrapping every listener.

## Test-simulator note

Most of these are client-side, where the repo's browserless tests cannot exercise the
JS collector. Server-side binders *are* testable browserlessly (drive
`navigate(...)` + session/UI lifecycle and assert against a `SimpleMeterRegistry`), and
the prototype branch ships exactly such tests. Gaps that live purely in the browser
(#1–#5, #7-client) have no browserless simulator and would need an end-to-end test or a
documented manual check.
