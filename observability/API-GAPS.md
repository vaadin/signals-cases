# API gaps — Observability Kit

This module is a *consumer-side* exploration of observability for Vaadin Flow.
The baseline is **Observability Kit 5.0** (`com.vaadin:observability-kit`, the
Micrometer rewrite — `observability-kit-micrometer` + `observability-kit-spring`
+ `observability-kit-starter`), built on **Flow 25.3**, which merged the two SPIs
the kit needs:

- session lock request/acquire/release events ([flow#24498](https://github.com/vaadin/flow/pull/24498)), and
- the RPC invocation listener ([flow#24499](https://github.com/vaadin/flow/pull/24499)).

The kit ships server-side binders (request, RPC, session, session-lock, UI,
navigation, errors), a Micrometer Observation/tracing path, and an in-browser
collector (`VaadinMetricsClient.js`) injected per UI and bridged to the server
through the `<vaadin-metrics-collector>` element's `@ClientCallable recordSamples`.
The Spring Boot starter wires a `MeterRegistry` and enables the instrumentation
out of the box.

The kit is good and covers most of the use cases in
[#277](https://github.com/vaadin/use-cases/issues/277). The gaps below are what
remains *missing or awkward even with the kit in place* — i.e. the things the
demo can't do cleanly, and the Flow / kit API changes they argue for. A reusable
shim, where one is possible, would live in `src/main/java/com/example/MissingAPI.java`.

Meters referenced below use the kit's `MeterNames` constants. The server records
`vaadin.request.duration`, `vaadin.rpc.duration` (tagged `type` / `outcome`),
`vaadin.navigation`, `vaadin.errors`, `vaadin.sessions.*`, `vaadin.session.lock.*`
and `vaadin.ui.*`. The client collector records, into the *same* registry,
`vaadin.client.bootstrap.duration`, `vaadin.client.navigation.duration` (tagged
`route` / `trigger`), `vaadin.client.web_vitals.lcp`, `vaadin.client.web_vitals.fcp`
and the `vaadin.client.errors` counter.

**Reachability is not the problem.** The in-browser collector POSTs its samples
back via the `<vaadin-metrics-collector>` `@ClientCallable`, and `ClientMetricsBinder`
records them into the *same server-side `MeterRegistry`* every other binder uses. So
the client meters above are readable by application code exactly like the server
meters — a Flow view can inject the `MeterRegistry` and read them today. The gaps
below are therefore about **what the client measures, granularity, correlation, and
completeness**, not about getting client data to the server.

> **Watch out:** `MeterNames.CLIENT_RPC_DURATION` (`vaadin.client.rpc.duration`)
> is a *defined constant but is intentionally never collected* — it is excluded
> from `ClientMetricNames.ALLOWED`, and the collector never emits it, because RPC
> timing is measured server-side only. Application code that reads
> `vaadin.client.rpc.duration` expecting a browser round-trip will always find an
> empty meter. (UC1 originally made this mistake; it now reads the client meters
> that actually populate — see gap #2.)

## 1. No public client-side request-lifecycle hook

**Where it bites:** UC1 (end-to-end responsiveness); the whole client collector.
**Symptom:** there is no supported way for application/add-on code to learn when a
UIDL request starts and ends in the browser. The internal `RequestResponseTracker`
fires `RequestStartingEvent` / `ResponseHandlingEndedEvent`, but on a GWT-internal
`EventBus` with Java-only handlers and no `@JsExport` — invisible to JS. Lacking
such a hook, the kit's `VaadinMetricsClient.js` does **not** instrument UIDL
requests at all: it sidesteps the problem by measuring SPA navigation through the
History API (wrapping `history.pushState` / `replaceState` and listening for
`popstate`) and page quality through `PerformanceObserver` (LCP/FCP) — never the
request/response of an in-place interaction. (The earlier prototype monkey-patched
`XMLHttpRequest.prototype.send` and string-matched `v-r=uidl`; the kit dropped that
rather than rely on an implementation detail.)
**Workaround used:** none clean; the kit measures navigation + vitals instead of requests.
**Suggested API:** a first-class client hook, e.g.
`window.Vaadin.Flow.addRequestListener({ onRequestStart, onResponseReceived, onRendered })`,
emitting per-request timestamps, request/response sizes, transport, outcome, and a
correlation id — surfacing the events `RequestResponseTracker` already fires.

## 2. No per-interaction client timing — only navigation and web vitals

**Where it bites:** UC1 (the browser segment of perceived latency).
**Symptom:** the kit deliberately emits **no client round-trip meter**
(`vaadin.client.rpc.duration` is excluded from the ingest allowlist — see the note
above). So an in-place interaction such as a button click produces no client-side
timing whatsoever; only a *route navigation* (a History change) yields
`vaadin.client.navigation.duration`, and even that is measured to the next
animation frame after the URL changes, not to the moment Flow finishes applying the
UIDL diff and painting. "Click-to-rendered" — the number the user actually feels for
a non-navigating interaction — is therefore not measurable on the client at all. The
engine *does* measure render time (TestBench reads `timeSpentRenderingLastRequest()`),
but it is not on any public JS surface.
**Workaround used:** UC1 reads the meters that do populate — `vaadin.request.duration`
and the new server-side `vaadin.rpc.duration` for the server share, plus
`vaadin.client.navigation.duration` and the web-vitals timers for page-load quality.
The per-click browser/network share is simply absent.
**Suggested API:** an `onRendered` timestamp on the hook from gap #1 (fired after the
UIDL response has been applied), and/or exposing the engine's existing render timing to
production client code, so a per-interaction client duration can exist.

## 3. Client samples are aggregated and uncorrelated — no per-interaction value

**Where it bites:** UC1 (browser/network/server breakdown), UC4 (single trace).
**Symptom:** two distinct limitations, both about *granularity / stitching*:
1. **Aggregated on ingest.** `ClientMetricsBinder.ingest` folds each browser sample into
   a rolling `Timer` (`registry.timer(name, tags).record(...)`); the per-sample values the
   JS buffers (each with its own `ts`/`valueMs`) are discarded. So we can read the *mean/
   max/count* of, say, `vaadin.client.navigation.duration`, but never *this navigation's*
   value.
2. **No correlation id.** The client navigation/vitals samples and the server
   `vaadin.request.duration` / `vaadin.rpc.duration` timers share no identifier, so a
   single slow interaction can't be stitched into one client → server picture — only
   aggregates exist. The collector also sends no W3C `traceparent` on the UIDL request
   (`recordSamples` carries only name/tags/value/ts), so the server-side trace
   (`vaadin.request` observation) doesn't descend from a browser-rooted span — the trace
   effectively starts on the server, not at the click.
**Workaround used:** aggregates only; no per-interaction stitch.
**Suggested API:** a per-UIDL-request correlation id exposed to both the client hook
(gap #1) and the server request interceptor, ideally as a W3C `traceparent` the client
injects and the server continues, so browser → server → backend is one trace.

## 4. Push / WebSocket transport is not instrumented on the client

**Where it bites:** UC1, UC2 (apps using `@Push`).
**Symptom:** the client collector hooks the History API, `PerformanceObserver` and
global error events — nothing for the push connection. So server-initiated updates
delivered over WebSocket/long-poll (`@Push`) produce no client-side timing or outcome
samples. For push-heavy apps the client view of responsiveness is blind.
**Workaround used:** none.
**Suggested API:** the client hook (#1) should be transport-agnostic, covering the
push connection, not just navigations.

## 5. No connection-state metric, despite the client API existing

**Where it bites:** UC5 (connection lost / reconnecting).
**Symptom:** the kit collects client *errors* but not connection-state
transitions. Yet `window.Vaadin.connectionState` already exposes
`online`/`offline`, a `state` (`CONNECTED` / `CONNECTION_LOST` / `RECONNECTING`), and
`addStateChangeListener(...)`. The information is one listener away but is neither
collected by the kit nor surfaced as a meter.
**Workaround used (possible):** an app-level shim that subscribes to
`window.Vaadin.connectionState.addStateChangeListener` and reports transitions to the
server via a `@ClientCallable`, recorded as a `vaadin.client.connection.state` meter —
a candidate for `MissingAPI`.
**Suggested API:** have the collector record connection-state transitions out of the
box (e.g. a `vaadin.client.connection` gauge/counter tagged by state), since the
client store already drives them.

## 6. No server-side UI-state-size / component-tree metric

**Where it bites:** UC3 (capacity & scaling).
**Symptom:** the kit reports session and UI *counts* (`vaadin.ui.active` etc.) and
session-lock contention, but not how much state each UI holds — component-tree node
count or per-session heap footprint. Because Flow keeps UI state in server memory,
*size* (not just count) is the signal that predicts when a server-driven app must
scale, and it is missing.
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
the kit re-measures instead of reusing it.
**Workaround used:** the kit's `RequestMetricsBinder` measures server time independently;
the client never sees a server duration.
**Suggested API:** a production-safe, public accessor for per-request server duration on
both sides (e.g. include it in the client hook's response event), so collectors need not
re-measure or parse debug logs.

## 8. Interaction attribution — now on insights, still absent from metrics

**Where it bites:** UC1 (which action is slow?), UC6 (errors by view/action).
**Status: largely addressed by the kit.** Flow's RPC invocation listener
([flow#24499](https://github.com/vaadin/flow/pull/24499)) lets the kit's `RpcMetricsBinder`
time *individual* RPC invocations as `vaadin.rpc.duration`, tagged by `type` and
`outcome` (`success`/`error`). The kit now also records the invocation *name* (the DOM
event, e.g. `click`) and the *targeted component class* — as high-cardinality span
attributes, and as fields of the interaction insights it captures for failed and
over-budget interactions. UC6 reads those insights: each names the route, the component,
the event and the first non-framework stack frame, so "the `click` on `Button` throws
`IllegalStateException` at `FailureInsightsView.java:114`" is a groupable finding rather
than something to reconstruct from a log.
**What still bites:** the attribution lives on **spans and insights, not on meter tags**.
`vaadin.rpc.duration` is still tagged only by `type` / `outcome`, and `vaadin.errors`
only by exception type — deliberately, to bound cardinality. So a Prometheus/Grafana
dashboard still cannot group latency or errors by component or view; only the in-process
insights (or a tracing backend) carry that. A *business* action name ("save order", as
opposed to the `click` that carried it) also remains the application's own to record.
**Workaround used:** UC1 keeps its own `uc1.interaction` timer for per-action metric
granularity; UC6 uses the insights for per-component attribution.
**Suggested API:** an opt-in, cardinality-bounded resolver the application supplies (e.g.
route plus a logical action name) that the kit may apply as *meter* tags, so dashboards
can group by view/action without unbounded cardinality.

## 9. Insights are consumable in-process only as an untyped JSON map

**Where it bites:** UC6.
**Symptom:** the kit's interaction insights are shaped for the Actuator endpoint:
`InsightsService.payload()` returns a `Map<String, Object>` of nested maps and lists. An
application that wants to render insights in its own UI — as UC6 does, rather than have
the app call its own HTTP endpoint — has to cast its way through that map
(`(List<Map<String, Object>>) payload.get("insights")`, then
`(Map<String, Object>) insight.get("evidence")`), with unchecked casts, string keys and
no compile-time contract. The JSON shape is a good published contract for *agents*; it is
a poor one for Java callers.
**Workaround used:** UC6 flattens the map into a view-local `Row` record, with
`@SuppressWarnings("unchecked")`.
**Suggested API:** typed insight objects (e.g. `List<Insight>` exposing id, severity,
summary, evidence and examples) alongside the JSON rendering, so in-app consumers get a
compile-checked contract and JSON stays a serialization concern.

## Test-simulator note

Most client-side gaps are where the repo's browserless tests cannot exercise the JS
collector. Server-side binders *are* testable browserlessly (drive `navigate(...)` +
session/UI lifecycle and assert against a `SimpleMeterRegistry`), and the kit ships
exactly such tests. Gaps that live purely in the browser (#1–#5, #7-client) have no
browserless simulator and would need an end-to-end test or a documented manual check.

**RPC-driven capture is also outside browserless reach.** Anything hooked on Flow's RPC
invocation listener — `vaadin.rpc.duration`, and UC6's interaction insights — is observed
only while handling a real UIDL request. A browserless `test(button).click()` invokes the
component listener directly, bypassing `ServerRpcHandler`, so no invocation is reported
and nothing is captured. UC6's test therefore covers rendering, wiring (the failing
action must let its exception propagate) and lifecycle, while the capture itself needs a
browser. A `SpringBrowserlessTest` hook to drive an invocation through the RPC pipeline
would close this.
