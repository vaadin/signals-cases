# Observability — use cases

A standalone Spring Boot demo module for observability use cases (request
tracing, metrics, structured logging of UI interactions, …). Each concrete use
case is a sibling `ucN` view, mirroring the layout of the other modules in this
repository, and the `HomeView` lists them via the auto-generated menu.

| # | View | What it shows |
| - | ---- | ------------- |
| — | Home | Landing page and auto-generated index of the use cases. |
| 1 | Interaction latency | Where an interaction's time goes: server request handling, the per-RPC server invocation (`vaadin.rpc.duration`), a per-action timer, and the browser's page-load signals (navigation timing, web vitals) — all read from the app's `MeterRegistry`. See [`API-GAPS.md`](API-GAPS.md). |
| 2 | Application health | A live readout of the app's own signals (sessions, UIs, memory, timings, connection), plus a database-health demo: a button that loads a product catalog and surfaces the classic N+1 join-table fetch — N products cost N+1 single-row fetches — through the Observability Kit's own `vaadin.db.fetch.rows` meter (`vaadin.observability.database=true`). Adding `@BatchSize` to `Product.category` collapses it, exactly as in the bookstore-example. See [`API-GAPS.md`](API-GAPS.md). |
| 3 | Capacity & scaling | How much state the server is holding for live users, and which signals actually predict needing another instance. Reads the kit's counts (`vaadin.sessions.active`, `vaadin.ui.active`, session creation rate and lifetime, session-lock contention) together with its UI-state gauges (`vaadin.ui.state.nodes`, `.nodes.max`, `.components`, `.views`, `vaadin.session.state.nodes.max`, `vaadin.session.uis.max`, `vaadin.ui.state.sample.age.max`), which the kit publishes once `vaadin.observability.ui-state=true` — this used to be [`API-GAPS.md`](API-GAPS.md) #6 and the view had to measure it itself. What remains local is the byte conversion: the kit counts nodes and will not guess what one weighs, so a probe measures it and the view reports whether the configured `ui-state-bytes-per-node` still holds. |
| 5 | Connection & client problems | The problems that never reach a server log: a browser losing the connection and getting it back, and a script failing in a tab nobody is watching. The connection half is the kit's now — its in-browser collector subscribes to Flow's `window.Vaadin.connectionState` and records `vaadin.client.connection` per transition and `vaadin.client.connection.downtime` for the time spent unreachable, so this view only reads them. What it makes visible is what those tags mean: downtime is tagged *per state*, because Flow enters `reconnecting` on the first failed request and `connection-lost` only after giving up retrying, so a short outage never leaves `reconnecting` and the whole outage is the two summed — which the readout does. Alongside them, `vaadin.resync` (the server side of a lost message, which Flow handles internally) and `vaadin.client.throttled`, which matters because one outage flushes as one batch. The errors are the kit's too: `vaadin.client.errors` only counts — a message on a tag would be one time series per message — so what identifies one is retained as a `client-error` *insight*, and this view reads those out of the endpoint payload UC6 renders in full. The kit parses the location out of the stack line and keeps it only when it is actually a location, groups by route, kind, source and frame with an occurrence count, gates the message and the function name behind `insights-details`, and reports `maxBufferedMs` — the offline time a report waited before it could be delivered. UC5 previously carried a shim for each half; both are deleted ([`API-GAPS.md`](API-GAPS.md) #5). Deliberately does not poll — a poll is a UIDL request, and one that gets through ends the outage as far as the browser is concerned — and since nothing signals that client samples have arrived, the readout has a refresh button. |
| 6 | Failure insights | Failed and over-budget interactions as grouped insights naming the route, component, event and the offending line of application code — the same payload the kit serves at `/actuator/vaadin/observability` for an AI agent to act on. |
| 7 | Monitoring stack | The same meters followed *outward*: exported at `/actuator/prometheus`, scraped by Prometheus, charted by Grafana. Checks each hop separately (exported series, scrape target health, the dashboard's own PromQL) so an empty panel can be told apart from a metric that was never exported. `compose.yaml` runs the stack locally. |
| 8 | Lazy list latency | Why a lazy `ComboBox` feels slow, and where the time actually goes. Typing a filter makes the component ask its data provider for a count of matches and for one page of items, and both run *after* the RPC invocation that triggered them has returned, so `vaadin.rpc.duration` for the keystroke stays in the microseconds however slow the backend is. Reads the kit's data query meters instead, by their tags: `vaadin.data.count.duration` and `vaadin.data.fetch.duration` split by `filtered` (the timers carry no route tag, and `filtered=true` is what separates a combo box searching typed text from any component loading a whole data set), and `vaadin.data.fetch.requested` next to `vaadin.data.fetch.rows` scoped to `route=uc8`, so over-fetching and short pages show as a gap between the two. The meter table is a plain HTML table, not a `Grid`, because the kit instruments in-memory data providers too and a `Grid` would record on this route while displaying it. The backend delay per query is adjustable. |

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.

To also log UC2's N+1 as SQL on the console, activate the `sql-log` profile:

```
mvn spring-boot:run -pl :observability-use-cases -Dspring-boot.run.profiles=sql-log
```

The kit's insights endpoint is exposed alongside the views:

```
curl -s http://localhost:8080/actuator/vaadin/observability | jq
```

The kit withholds the session id, the exception message and the stack frames
unless `vaadin.observability.insights-details=true`, since that payload is meant
to be forwarded — into issue trackers, AI agents and log pipelines. This module
enables it so UC6 shows a complete insight; with it off the session id is a
short hash and the payload states that the message was withheld rather than
absent. A production application should leave it off until it has reviewed what
those fields can contain.

## Monitoring stack (UC7)

UC7 ships the module's metrics to the standard OSS stack. Start the app, then
from this directory:

```
docker compose up -d
```

- Prometheus <http://localhost:9090> — scrapes `/actuator/prometheus`
- Grafana <http://localhost:3000> — anonymous admin, dashboard provisioned

The dashboard's bottom rows chart the kit's UI-state gauges next to its
counts, which is where the difference shows: state climbing while the session
count is flat means capacity is going to what users have open, not to how many
of them there are. `vaadin_ui_state_size_bytes` exists only because this module
configures `vaadin.observability.ui-state-bytes-per-node`, and the last panel
tracks `vaadin.ui.state.sample.age.max` — how stale the oldest per-UI
measurement in the aggregate is, since a UI is measured on its own session's
thread.

Prometheus scrapes `host.docker.internal` on ports 8080 and 8082, so it finds
the app on either; the unused one shows as a down target. Stop it with
`docker compose down`.

This stack is developer tooling: the module deploys as a single container, so the
hosted demo runs without it and UC7 degrades to its export column.
