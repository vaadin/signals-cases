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
