# Vaadin Clipboard API Use Cases

This module contains a collection of Vaadin Flow views demonstrating the
browser Clipboard API exposed via `com.vaadin.flow.component.page.Clipboard`.

The use cases were derived from the Clipboard PRD on
[vaadin/platform#8759](https://github.com/vaadin/platform/issues/8759) and
the draft Flow PR
[vaadin/flow#23615](https://github.com/vaadin/flow/pull/23615).

## Use Cases

1. **UC1 — Copy static text on click** — `Clipboard.copyOnClick(button, "...", onSuccess, onError)`.
   The text is known at render time; the write happens client-side inside the
   click handler so it works in all browsers.
2. **UC2 — Copy current value of a component** — `Clipboard.copyOnClick(button, textField)`.
   The source component's value is read client-side at click time, so the user
   gesture is preserved even when the value has been edited since render.
3. **UC3 — Copy image** — `Clipboard.copyImageOnClick(button, image)` fetches
   the image from the source component's `src` inside the click handler.
4. **UC4 — Paste text and HTML** — `Clipboard.addPasteListener(...)` reading
   `event.getText()` and `event.getHtml()` separately.
5. **UC5 — Paste files** — `Clipboard.addPasteListener(...)` consuming
   `event.getFiles()`; image files are rendered inline.
6. **UC6 — Copy via context menu** — `Clipboard.copyOnClick(menuItem, "...")`
   on a `ContextMenu` item, demonstrating that the same gesture-safe path
   works for non-button triggers.
7. **UC7 — Detect availability and degrade gracefully** — query
   `Clipboard.isAvailable()` up front and disable copy controls when
   the API can't be used (HTTP, restrictive iframe, denied permission).
   The availability method itself is not in the draft PR yet; the view
   anticipates the eventual signature.

## Running the Application

1. **Prerequisites**: Java 25+, Maven 3.9+
2. **Run**: `./mvnw` (defaults to `spring-boot:run`)
3. **Access**: <http://localhost:8080>

For production builds, use `./mvnw package`.

## Technical Stack

- **Vaadin 25.2.clipboard-SNAPSHOT** — Flow with the Clipboard API from
  the draft PR
- **Spring Boot 4.0.5**
- **Java 25**
- **Maven**

## Browser Notes

- The Clipboard API only works in **secure contexts** (HTTPS or
  `localhost`); over plain HTTP the operations will fail.
- Server-initiated writes (`Clipboard.writeText` / `Clipboard.writeImage`
  called from a server click listener) involve a server round-trip and
  may be rejected by Firefox or Safari because the user gesture has
  timed out by the time the JS runs. The `copyOnClick` /
  `copyImageOnClick` variants demonstrated here keep the operation
  inside the gesture, which is why the PRD treats them as the primary
  copy path.
- `Clipboard.readText` (programmatic read) is unreliable in Firefox.
  Paste-event-based reading (UC4, UC5) works everywhere and is the
  recommended way to receive clipboard content.
- UC7 calls a hypothetical `Clipboard.isAvailable()` that does not exist
  in the draft PR. Once the real availability API lands, swap the call
  site in `AvailabilityView`.
