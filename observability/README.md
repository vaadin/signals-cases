# Observability — use cases

A standalone Spring Boot demo module, scaffolded as the home for upcoming
observability use cases (request tracing, metrics, structured logging of UI
interactions, …). It currently ships only a `HomeView` that lists the use
cases via the auto-generated menu; concrete use cases will be added as sibling
`ucN` views, each mirroring the layout of the other modules in this repository.

| # | View | What it shows |
| - | ---- | ------------- |
| — | Home | Landing page and auto-generated index of the use cases to come. |

## Run

```
mvn spring-boot:run -pl :observability-use-cases
```

Open <http://localhost:8080/>.
