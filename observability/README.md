# Observability — use cases

A standalone Spring Boot demo module, scaffolded as the home for upcoming
observability use cases (request tracing, metrics, structured logging of UI
interactions, …), built on Observability Kit 5.0. A `HomeView` lists the use
cases via the auto-generated menu; each use case is a sibling `ucN` view
mirroring the layout of the other modules in this repository.

| # | View | What it shows |
| - | ---- | ------------- |
| — | Home | Landing page and auto-generated index of the use cases. |
| 1 | Interaction latency | Where an interaction's time goes: server request handling, the per-RPC server invocation (`vaadin.rpc.duration`), a per-action timer, and the browser's page-load signals (navigation timing, web vitals) — all read from the app's `MeterRegistry`. See [`API-GAPS.md`](API-GAPS.md). |

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.
