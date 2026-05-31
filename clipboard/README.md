# Vaadin Clipboard API Use Cases

This module contains a collection of Vaadin Flow views demonstrating the
browser Clipboard API exposed via `com.vaadin.flow.component.clipboard.Clipboard`.

The write surface in Vaadin Flow 25.2 covers *writing* to the clipboard
from a user click in plain text, HTML, multi-format, or image. The
`25.3.paste-file-SNAPSHOT` branch of `vaadin/flow`
([PR #24485](https://github.com/vaadin/flow/pull/24485)) extends the
surface with:

- `Clipboard.onPaste(...)` — text/html paste listener, used in UC5;
- `Clipboard.onClick(...).writeImage(Component | DownloadHandler)` —
  client-side image copy, used in UC4;
- `Clipboard.onFilePaste(component, UploadHandler)` plus
  `PasteFileHandler.session()` / `PasteFileHandler.inMemory(...)` —
  per-file upload of pasted screenshots and files, used in UC7.

The clipboard module is pinned to that branch via `<flow.version>` in
`clipboard/pom.xml`; the rest of the Vaadin stack stays on the regular
25.2 release.

One PRD use case is still not supported by the API on this branch and
is therefore not included here:

- **Detecting clipboard availability** (HTTPS context, restrictive
  iframe, denied permission). No `availabilityHintSignal()` or
  `ClipboardAvailability` type exists in this build.

The original tracking issue is
[vaadin/platform#8759](https://github.com/vaadin/platform/issues/8759);
the initial write API landed via
[vaadin/flow#23615](https://github.com/vaadin/flow/pull/23615) and
file-paste support is on
[vaadin/flow#24485](https://github.com/vaadin/flow/pull/24485).

## API at a glance

```java
// Write — fires on a user click on the source component.
Clipboard.onClick(button).writeText("hello");
Clipboard.onClick(button).writeText(textField);
Clipboard.onClick(button).writeHtml("<p>hello</p>");
Clipboard.onClick(button).writeImage(image);   // 25.3 paste-events branch
Clipboard.onClick(button).write(
        ClipboardContent.create().text("hello").html("<p>hello</p>"));

// Read — listens for browser paste events on a focused element.
// 25.3 paste-file branch.
Clipboard.onPaste(div, event -> {
    if (event.hasHtml()) {
        handleHtml(event.getHtml());
    } else if (event.hasText()) {
        handleText(event.getText());
    }
});

// File paste — each pasted file is uploaded to the URL Flow generates
// for the handler. 25.3 paste-file branch.
Clipboard.onFilePaste(div, PasteFileHandler.session()
        .onStart(start -> showProgress(start.totalFiles()))
        .onFile(file -> render(file))
        .onComplete(end -> hideProgress())
        .build());
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
4. **UC4 — Copy image** —
   `Clipboard.onClick(button).writeImage(image, onCopied, onError)`.
   The source component's root `<img>` is rasterised to PNG on the
   client and written to the clipboard inside the click handler.
5. **UC5 — Paste a table from a spreadsheet** —
   `Clipboard.onPaste(div, event -> …)`. Reads the HTML branch of the
   paste (a `<table>`) when available; falls back to TSV from the plain
   text branch.
6. **UC6 — Copy via context menu** —
   `Clipboard.onClick(menuItem).writeText(…)` on a `ContextMenu` item,
   demonstrating that the same path works for any `ClickNotifier`.
7. **UC7 — Paste images and files** —
   `Clipboard.onFilePaste(div, PasteFileHandler.session()…)`. Each
   pasted file is uploaded via Flow's standard upload mechanism;
   `onStart` / `onFile` / `onComplete` give the application
   paste-aware lifecycle hooks for progress reporting.

The remaining PRD item (availability signal) is omitted because the
underlying API is still not part of this Flow branch — see the note at
the top of this file.

## Running the Application

1. **Prerequisites**: Java 25+, Maven 3.9+
2. **Run**: `./mvnw` (defaults to `spring-boot:run`)
3. **Access**: <http://localhost:8080>

For production builds, use `./mvnw package`.

## Technical Stack

- **Vaadin 25.2-SNAPSHOT** (Flow overridden to `25.3.paste-file-SNAPSHOT`)
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
