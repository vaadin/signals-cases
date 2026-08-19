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
| 6 | Failure insights | Failed and over-budget interactions as grouped insights naming the route, component, event and the offending line of application code — the same payload the kit serves at `/actuator/vaadin/observability` for an AI agent to act on. |

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.

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
