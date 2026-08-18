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
| 7 | Monitoring stack | The same meters followed *outward*: exported at `/actuator/prometheus`, scraped by Prometheus, charted by Grafana. Checks each hop separately (exported series, scrape target health, the dashboard's own PromQL) so an empty panel can be told apart from a metric that was never exported. `compose.yaml` runs the stack locally. |

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.

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
