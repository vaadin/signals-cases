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

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.

To also log UC2's N+1 as SQL on the console, activate the `sql-log` profile:

```
mvn spring-boot:run -pl :observability-use-cases -Dspring-boot.run.profiles=sql-log
```
