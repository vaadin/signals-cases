# Clipboard API — API gaps discovered while building the demos

Places where the `com.vaadin.flow.component.clipboard` API either doesn't
cover a genuine clipboard use case or makes one awkward enough to work
around. Each entry is keyed to the use case that surfaced it.

The API is a small static facade:

- `Clipboard.onClick(component)` returns a `ClipboardBinding` whose
  `writeText` / `writeHtml` / `writeImage` / `write(ClipboardContent)`
  methods bind a clipboard write to a click trigger, so the write runs inside
  the browser's own click handler while the user gesture is still valid; the
  `read` / `readText` / `readHtml` methods do the same for reading. Every
  variant has an observed overload taking `onCopied` / `onError`.
- The value written is either a literal `String` fixed at binding time, or —
  for `writeText` and `ClipboardContent.text` only — the `value` property of a
  `Component & HasValue<?, String>`, read on the client at click time.
- `Clipboard.onPaste(component, listener)` and
  `Clipboard.onFilePaste(component, handler)` observe browser paste events.

The gaps below were all found while building **UC8 — Copy from a data grid**
(`uc8/CopyFromGridView.java`), the use case from the
[forum thread](https://vaadin.com/forum/t/clipboard-copy/164697/11): a table
with a copy affordance next to the cell values, without "instantiating
millions of components and instances of the clipboard helper".

## No dynamic value source — the copied value must be known before the click

**Where it bit us:** uc8 / CopyFromGridView.java (all four copy actions)
**Symptom:** `Clipboard.onClick(...)` has to be given the value at binding
time. The two sources are a `String` literal and the `value` property of a
`HasValue<?, String>` component. Neither can express "copy whatever row the
user just right-clicked" or "copy the current grid selection", which is the
normal shape of a copy action in a data-heavy application: the value is only
known server-side, after the user has picked a target, and re-binding on every
change is not an option because each `writeText` call adds another wiring
(`Trigger.triggers` appends, it does not replace).

Note that the underlying trigger machinery already supports this — the
internal `com.vaadin.flow.component.trigger.internal.SignalInput` mirrors a
server-side `Signal` into a client-side property and reads it when the trigger
fires, which is exactly what is needed. It is simply not reachable from the
public clipboard API.

This is also a regression against what the community had already settled on.
The reusable helper the forum thread converged on
([post 8](https://vaadin.com/forum/t/clipboard-copy/164697/8)) is
`CopyToClipboard(Supplier<String> text)` — a copy icon whose value is resolved
at click time — and it is used as
`new CopyToClipboard(() -> textField.getValue())`. That helper is unsafe for
other reasons (the `executeJs` it wraps runs after a server round trip, so the
user gesture is gone), but its *ergonomics* are the ones a grid needs, and they
are what the new API dropped: it can bind a value, but not a way to compute
one.

**Workaround used:** an off-screen value-holder component per copy action,
used purely as a client-side staging slot, plus a server-side hook that fills
the slot before the user is able to click:

```java
StagingSlot emailSlot = new StagingSlot();  // never shown to the user
Clipboard.onClick(copyEmail).writeText(emailSlot, onCopied, onError);

grid.addContextMenu().setDynamicContentHandler(customer -> {
    emailSlot.setValue(customer.email()); // reaches the client before the
    return true;                          // menu opens
});
```

Three sharp edges in that workaround, all of them found the hard way:

- **The obvious slot component corrupts the value.** The natural choice is
  `com.vaadin.flow.component.html.Input` — it is the lightest
  `Component & HasValue<?, String>` in the platform. It also silently breaks
  every multi-line copy: the HTML value sanitisation algorithm strips CR and
  LF from an `<input>`'s value, so "copy this row" and "copy the selected
  rows" put a single run-together line on the clipboard. Nothing warns you;
  the copy succeeds and `onCopied` reports the mangled string. UC8 therefore
  declares a five-line `AbstractSinglePropertyField` over a `<span>`, whose
  `value` is a plain JS property with no sanitisation. Having to invent a fake
  field component to satisfy the API's only dynamic value source is the gap in
  a nutshell.
- **The slot must not be hidden with `setVisible(false)`.** Flow does not push
  property updates to invisible elements, so the staged value would arrive
  only when the component became visible again and the copy would silently
  write a stale value. It has to stay "visible" and be hidden with CSS
  instead.
- **It only works where something guarantees a server round trip *before* the
  click** — `GridContextMenu.setDynamicContentHandler` and a selection
  listener do; a plain hover or a keyboard-driven flow may not.

**Suggested API:** a signal-backed (or supplier-backed) value source on every
write, mirroring the existing `HasValue` overloads:

```java
// The signal is mirrored to the client on every change; the write reads the
// mirrored value at click time. Backed by the existing SignalInput.
ClipboardBinding.writeText(Signal<String> value);
ClipboardBinding.writeText(Signal<String> value, SerializableConsumer<String> onCopied,
        SerializableConsumer<PromiseAction.Error> onError);
ClipboardContent.text(Signal<String> value);
// e.g.
// ValueSignal<String> email = new ValueSignal<>("");
// Clipboard.onClick(copyEmail).writeText(email);
// grid.addContextMenu().setDynamicContentHandler(c -> { email.set(c.email()); return true; });
```

A `SerializableSupplier<String>` overload would read even closer to the
thread's `CopyToClipboard(Supplier<String>)`, but it cannot work on its own:
the supplier would have to be invoked on the server, and the gesture is gone by
then. A signal is the version of that idea that survives the gesture
constraint, because its value is already on the client when the click happens.

## `GridMenuItem` is not a `ClickNotifier`, so a grid context menu cannot be a trigger

**Where it bit us:** uc8 / CopyFromGridView.java
**Symptom:** `Clipboard.onClick` requires `T extends Component &
ClickNotifier<?>`. `ContextMenu`'s `MenuItem` implements `ClickNotifier` (which
is what UC6 relies on), but `GridContextMenu`'s `GridMenuItem` — the item type
of *the* context menu you use with a `Grid` — does not; it only extends
`MenuItemBase`. So the most natural per-row copy affordance in a grid does not
compile:

```java
GridMenuItem<Customer> copyEmail = grid.addContextMenu().addItem("Copy email");
Clipboard.onClick(copyEmail).writeText(emailSlot); // does not compile
```

**Workaround used:** wrap the item's label in a `Span` (an `HtmlContainer`, so
a `ClickNotifier`), bind the clipboard action to the `Span`, and stretch it
with CSS to cover the whole menu item so that clicks anywhere on the item hit
it:

```java
Span copyEmail = new Span("Copy email");
copyEmail.addClassName("menu-action"); // display: block; width: 100%
grid.addContextMenu().addItem(copyEmail);
Clipboard.onClick(copyEmail).writeText(emailSlot, onCopied, onError);
```

**Suggested API:** make `GridMenuItem` implement `ClickNotifier<GridMenuItem<T>>`
for parity with `MenuItem` (a `vaadin-grid-flow` change, not a clipboard one).
Failing that, `Clipboard.onClick` could accept any `Component` and install the
DOM `click` listener directly rather than going through `ClickNotifier`.

## No `copy` / `cut` event counterpart to `onPaste` — Ctrl+C cannot be served

**Where it bit us:** uc8 / CopyFromGridView.java
**Symptom:** reading the clipboard is event-driven (`Clipboard.onPaste`), but
writing is click-driven only. The browser fires a `copy` event on Ctrl/Cmd+C
that a page can answer with `event.clipboardData.setData(...)`; that is how
every spreadsheet-like web UI implements "select rows, press Ctrl+C". Flow
exposes no equivalent, and the server-side route (`Shortcuts.addShortcutListener`
or a `KeyNotifier`) is useless here because the gesture is gone by the time the
server sees the key press. In a grid this is the single most expected copy
interaction, and it is the one interaction that needs *no* extra components at
all — so its absence is what forces the context-menu/toolbar design in the
first place.

**Workaround used:** none — UC8 has no Ctrl+C support. Copying is only
available through the context menu and the toolbar button.

**Suggested API:**

```java
// Symmetric with onPaste: the handler runs on the server when the browser
// fires `copy`/`cut` on the component, and what it returns is what gets put
// on the clipboard.
Registration Clipboard.onCopy(Component component,
        SerializableFunction<CopyEvent, ClipboardContent> handler);
Registration Clipboard.onCut(Component component,
        SerializableFunction<CopyEvent, ClipboardContent> handler);
// e.g. Clipboard.onCopy(grid, event -> ClipboardContent.create()
//         .text(toTsv(grid.getSelectedItems()))
//         .html(toHtmlTable(grid.getSelectedItems())));
```

(Serving the event from the server means the round trip has to happen inside
the browser's `copy` handler — so this most likely needs the value to be
staged the same way the signal-backed source above stages it, i.e. the two
suggestions compose: `Clipboard.onCopy(grid).write(contentSignal)`.)

## `writeHtml` / `ClipboardContent.html` take a literal only

**Where it bit us:** uc8 / CopyFromGridView.java ("Copy selected rows")
**Symptom:** `ClipboardContent.text` has both a literal and a
`HasValue`-component overload, but `html` (and `writeHtml`) only takes a
literal `String`. There is therefore no way at all to put *dynamic* rich
content on the clipboard — not even with the staging-slot workaround, because
there is nothing to stage into. Copying a grid selection as an HTML `<table>`
alongside the plain-text TSV is the standard way to make a paste into Excel,
Word or Google Docs keep its structure (and it is the exact inverse of UC5,
which parses that HTML on the way in), but it can only be done for a fixed
value known at binding time.

**Workaround used:** UC8 copies the selection as tab-separated text only.
Spreadsheets still split it into cells; rich-text targets get a single blob of
tab-separated text.

**Suggested API:** the same value sources as `text`, on every format:

```java
ClipboardContent.html(Signal<String> value);
ClipboardContent.html(C source); // C extends Component & HasValue<?, String>
ClipboardBinding.writeHtml(Signal<String> value, onCopied, onError);
```

## No per-row / per-cell binding for renderers

**Where it bit us:** uc8 / CopyFromGridView.java — the original forum question
**Symptom:** the affordance users actually ask for is a copy icon *in the
row*. Today that means a `ComponentRenderer` column creating a `Button` and a
`Clipboard.onClick(...)` binding per rendered row — one component, one trigger
and one client-side listener each, re-created as the user scrolls, which is
what the forum thread was worried about. The cheap alternative, `LitRenderer`,
cannot help: its `withFunction(...)` callbacks are server round trips, so the
user gesture is gone before the write could run.

**Workaround used:** no per-row affordance at all. UC8 puts the copy actions
in a single context menu and a single toolbar button, so the binding count is
constant in the number of rows.

**Suggested API:** a way to attach a client-side trigger action to a renderer
template, so one binding serves every row and the copied value comes from the
row's own client-side data:

```java
// The action is rendered into the LitRenderer template and fires on the
// client, with the item's serialised properties available as the value
// source.
LitRenderer.<Customer> of("<button @click=${copy}>Copy</button>")
        .withProperty("email", Customer::email)
        .withClientAction("copy", Clipboard.write().text("email"));
```

## Clipboard writes cannot be asserted in browserless tests

**Where it bit us:** every UC test under `src/test/java/com/example/uc*/`
**Symptom:** the write is a client-side trigger action bound to a click
(`Clipboard.onClick(button).writeText(...)`), so nothing about it is
observable from a browserless test: there is no way to fire the trigger, no
way to inspect what a component's bindings would write, and no fake clipboard
to assert against. Tests can only check the surrounding server-side state —
in UC8's case, the values staged into the slots. A regression that unbound a
copy action entirely, or bound it to the wrong slot, would not be caught.

**Workaround used:** `CopyFromGridViewTest` asserts the staged values and
drives the dynamic content handler directly
(`contextMenu.getDynamicContentHandler().test(customer)`), which is the
server-side half of the interaction.

**Suggested API:** a browserless simulator, e.g.

```java
// In browserless-test or a flow-server test-fixtures jar.
ClipboardSimulator clipboard = ClipboardSimulator.install();
test(copyButton).click();
assertEquals("ada.lovelace@acme.example", clipboard.lastWrittenText());
```

## Detecting clipboard availability

**Where it bit us:** dropped from the UC list — no view written
**Symptom:** there is no way to ask whether the clipboard is usable at all
(non-secure context, restrictive iframe permissions policy, denied
`clipboard-write` permission) before offering a copy affordance. No
`availabilityHintSignal()` or `ClipboardAvailability` type exists in this
build; the only signal is an `onError` callback firing after the user has
already clicked.
**Workaround used:** none — the copy affordances are always offered, and a
failure is reported through `onError`.
**Suggested API:** a support/permission signal mirroring
`WebShare.supportSignal()` and `Fullscreen.stateSignal()`:

```java
Signal<ClipboardAvailability> Clipboard.availabilitySignal();
```
