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
| 6 | Failure insights | Failed and over-budget interactions as grouped insights naming the route, component, event and the offending line of application code — the same payload the kit serves at `/actuator/vaadin/observability` for an AI agent to act on. |
| 7 | Monitoring stack | The same meters followed *outward*: exported at `/actuator/prometheus`, scraped by Prometheus, charted by Grafana. Checks each hop separately (exported series, scrape target health, the dashboard's own PromQL) so an empty panel can be told apart from a metric that was never exported. `compose.yaml` runs the stack locally. |

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

Prometheus scrapes `host.docker.internal` on ports 8080 and 8082, so it finds
the app on either; the unused one shows as a down target. Stop it with
`docker compose down`.

This stack is developer tooling: the module deploys as a single container, so the
hosted demo runs without it and UC7 degrades to its export column.
