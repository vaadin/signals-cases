# Vaadin Geolocation API Use Cases

This module contains a collection of Vaadin Flow views demonstrating the
browser Geolocation API exposed via `UI.getGeolocation()`.

## Use Cases

1. **UC1 — One-shot request on user click** — classic "Use my location"
   button that calls `Geolocation.get(...)` and renders the result on a
   `Map`.
2. **UC2 — Continuous tracking with reactive signal** — a single
   `GeolocationTracker` driven by a Start/Stop toggle, with `Signal.effect`
   subscriptions appending readings to a grid and extending a path on the
   map.
3. **UC3 — Auto-fetch on view load, gated on permission** — silently
   fetches the location on attach when permission has previously been
   granted, and reacts to permission flips while the view is open.
4. **UC4 — Handling denial, failure and unavailability** — three cards
   covering availability-driven rendering, error handling for each
   `GeolocationErrorCode`, and a real-browser request.
5. **UC5 — Reading detailed position data** — dumps every field reported
   by the browser (lat/lon/accuracy/altitude/heading/speed/timestamp).
6. **UC6 — Tuning precision, freshness and battery** — four common
   `GeolocationOptions` profiles run against the same entry point so you
   can compare response time and accuracy.
7. **UC7 — Capturing a location as part of a form** — a pothole-reporting
   form where "Pin my location" stores the result on a signal and the
   submit button stays disabled until a description and a recent enough
   pin are present.

## Running the Application

1. **Prerequisites**: Java 25+, Maven 3.9+
2. **Run**: `./mvnw` (defaults to `spring-boot:run`)
3. **Access**: <http://localhost:8080>

For production builds, use `./mvnw package`.

## Technical Stack

- **Vaadin 25.2-SNAPSHOT** — Flow with the Geolocation API
- **Spring Boot 4.0.5**
- **Java 25**
- **Maven**

## Browser Notes

- The browser shows its own permission dialog on the first request; Flow
  cannot style or suppress it.
- Safari never reports permission state — `availabilitySignal()` surfaces
  `UNKNOWN` for granted/denied/prompt; only `UNSUPPORTED` is reliable.
- Firefox does not always propagate browser-settings changes back to the
  page — the cached availability can be stale until the next `get`/`track`
  call.
