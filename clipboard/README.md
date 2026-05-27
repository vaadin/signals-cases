# Vaadin Clipboard API Use Cases

This module contains a collection of Vaadin Flow views demonstrating the
browser Clipboard API exposed via `com.vaadin.flow.component.clipboard.Clipboard`.

The public API in Vaadin Flow 25.2 is intentionally narrow: it covers
*writing* to the clipboard from a user click, in plain text, HTML, or
multi-format. There is no Java API for reading from the clipboard,
listening to paste events, copying image data, or detecting clipboard
availability.

The original tracking issue is
[vaadin/platform#8759](https://github.com/vaadin/platform/issues/8759);
the API landed via
[vaadin/flow#23615](https://github.com/vaadin/flow/pull/23615).

## API at a glance

```java
Clipboard.onClick(button).writeText("hello");
Clipboard.onClick(button).writeText(textField);
Clipboard.onClick(button).writeHtml("<p>hello</p>");
Clipboard.onClick(button).write(
        ClipboardContent.create().text("hello").html("<p>hello</p>"));
```

Every variant has an overload that takes `onSuccess` and `onError`
callbacks (`SerializableConsumer<String>` and
`SerializableConsumer<PromiseAction.Error>`). The handler is registered
once at view-construction time and re-fires on every click of the source
component, which can be any `Component` that implements `ClickNotifier`
— a `Button`, a context-menu `MenuItem`, and so on.

## Use Cases

1. **UC1 — Copy static text on click** —
   `Clipboard.onClick(button).writeText("…", onSuccess, onError)`. The
   text is known at render time; the write happens client-side inside
   the click handler so the user gesture is preserved.
2. **UC2 — Copy current value of a component** —
   `Clipboard.onClick(button).writeText(textField, …)`. The source
   component's value is read client-side at click time, so editing the
   field after render still works.
3. **UC3 — Copy rich content (HTML + plain text)** —
   `Clipboard.onClick(button).write(ClipboardContent.create().text(…).html(…))`.
   Rich destinations get the HTML; plain destinations get the
   plain-text fallback.
6. **UC6 — Copy via context menu** —
   `Clipboard.onClick(menuItem).writeText(…)` on a `ContextMenu` item,
   demonstrating that the same path works for any `ClickNotifier`.

Use-case numbers 4, 5 and 7 from the original PRD (paste text/HTML,
paste files, availability signal) are intentionally omitted: the
underlying APIs are not part of the merged Flow 25.2 surface.

## Running the Application

1. **Prerequisites**: Java 25+, Maven 3.9+
2. **Run**: `./mvnw` (defaults to `spring-boot:run`)
3. **Access**: <http://localhost:8080>

For production builds, use `./mvnw package`.

## Technical Stack

- **Vaadin 25.2-SNAPSHOT**
- **Spring Boot 4.0.5**
- **Java 25**
- **Maven**

## Browser Notes

- The Clipboard API only works in **secure contexts** (HTTPS or
  `localhost`); over plain HTTP the operations fail and the `onError`
  callback is invoked.
- Because the write is performed by the click handler installed on the
  source component, the user gesture is always preserved — no
  Firefox/Safari gesture-timeout issues.
