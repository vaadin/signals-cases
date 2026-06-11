# Trigger / Action API — gaps surfaced by the use-case module

This module exercises the public-facing trigger API at
`com.vaadin.flow.component.trigger.internal.*` in mainline
`25.2-SNAPSHOT`. The classes there carry "For internal use only. May be
renamed or removed in a future release." — using them is supported but the
shapes will keep evolving.

## Status

| Gap | State | See |
|---|---|---|
| `ServerCallbackAction` post-copy | **Closed** — `WriteToClipboardAction(text, html, onCopied, onError)` | UC10 |
| `SignalOutput` → server-side signal as Output | **Closed** — `SignalInput(owner, signal)` mirrors via effect | UC4, UC8 |
| `FullscreenAction` | **Closed** — `RequestFullscreenAction` ships | (not yet demoed in this module) |
| `ShortcutTrigger` | **Re-opened in mainline** — no public class | UC6, UC7 |
| `ClickAction` (synthesise a click on another element) | **Open** — no public class | UC7 |
| Client-side test simulator | Open — no introspection at all in the new architecture | — |
| Server-side feature detection | Open | — |
| `PropertyInput` doesn't accept raw `Element` | Open — only `Component` accepted; native elements need a wrapper | UC3 |
| Public introspection of trigger wiring | Open — `addJsInitializer` exposes no inspect surface | All tests |

## [Closed in mainline] Server callback after copy

**Where it bit us (originally):** UC10. No working server callback in slice 1.
**Symptom (originally):** `Trigger#triggers(SerializableRunnable)` registered a
`ServerCallbackAction` whose client factory wasn't wired.
**Closed by:** `WriteToClipboardAction(textInput, htmlInput, onCopied, onError)`
exposes the post-copy callback as first-class constructor parameters. The
generic mechanism is `CallbackAction<T>(Class<T>, Consumer<T>, Input<? extends T>)`.
**See:** UC10 (`com.example.uc10.CopyAndCountView`).

## [Closed in mainline] No `SignalOutput`

**Where it bit us (originally):** UC4 had to stash a URL in a read-only
`TextField` so a `PropertyOutput` could read it back.
**Closed by:** `SignalInput<T>(Component owner, Signal<T> signal)` mirrors the
signal value into a private property on the owner element via a Vaadin
effect; the trigger handler then reads from that mirror with the same shape
as `PropertyInput`.
**See:** UC4 (static), UC8 (live mutation).

## [Closed in mainline] No `FullscreenAction`

**Symptom (originally):** The PR description named fullscreen as motivation
but no built-in action shipped.
**Closed by:** `RequestFullscreenAction(target, onSuccess?, onError?)` (and
the higher-level `Component#requestFullscreen()` facade tracked in
vaadin/flow#24326).

## No public `ShortcutTrigger`

**Where it bit us:** UC6 (Ctrl+C copy), UC7 (Enter → submit + disable).
**Symptom:** Mainline ships no keyboard-shortcut Trigger subclass. The
existing `com.vaadin.flow.component.Shortcuts` framework (and
`ShortcutRegistration`) handles keyboard shortcuts for component focus, but
isn't a `Trigger` and so can't be wired to actions through
`trigger.triggers(...)`.
**Workaround used:** A small `com.example.ShortcutTrigger` subclassing the
new public `Trigger`. Listens for `keydown`, filters by exact modifier match
and matches the key against both `event.key` and `event.code`. ~80 lines;
see `triggers/src/main/java/com/example/ShortcutTrigger.java`.
**Suggested API:** The feature branch
`vaadin/flow:feature/triggers-actions` has a `ShortcutTrigger` built on a
`KeyboardEventTrigger` parent. Once that lands in mainline, delete our
local file and switch the imports.

## No public `ClickAction`

**Where it bit us:** UC7 (Enter shortcut chains a synthetic click on the
Send button followed by SetPropertyAction(disabled)).
**Symptom:** No built-in action calls `target.click()`. Reasonable
alternatives:
- Inline the submit logic into the shortcut trigger via a `CallbackAction`
  (changes the demo's teaching point from "fire another button's logic"
  into "the shortcut handler does the work").
- Use `SetPropertyAction(target, "click", true)` — no, that doesn't work
  (`click` isn't an assignable property).
- Subclass `Action` and emit `$0.click()`.
**Workaround used:** A 30-line `com.example.ClickAction`. Same constructor
shape as the slice-2 class we used to import. Delete + import once it
lands in mainline.

## No client-side test simulator / introspection

**Where it bit us:** All tests in this module.
**Symptom:** The new architecture installs handlers via
`Element#addJsInitializer`, which exposes no introspection surface — there
is no equivalent of the old `TriggerSupport.snapshotForTest()`. Browserless
tests can verify view rendering and server-side state changes (e.g.
"clicking Send disables the button server-side") but cannot inspect what
JavaScript was actually emitted. Functional verification of the
trigger-action wire (clipboard write happens, shortcut fires, dblclick
copies) requires a real browser.
**Workaround used:** Browserless tests are pure render assertions;
Playwright is the source of truth for behaviour.
**Suggested API:** Either a `JsInitializerSnapshot` accessor on `Element`
exposing the installed expressions (for assertions on the generated JS),
or a higher-level `TriggerTestKit` that mocks the gesture path so actions
can be fired headlessly.

## No server-side feature-detection signal

**Where it bit us:** Not blocking any UC, but relevant for the share /
file-system / payment / clipboard-read variants.
**Symptom:** A trigger always installs; if the browser lacks the API the
action targets, the call fails silently in JS. There is no server-side way
to ask "is this gesture-gated API supported here?" so the app could
proactively hide the relevant control.
**Suggested API:** A `Feature.detect(host, "clipboard.write")` returning a
`Signal<Boolean>` resolved per session.

## `PropertyInput` doesn't accept a raw `Element`

**Where it bit us:** UC3 wanted to read a native `<select>` element's
`value` property. `PropertyInput`'s only constructor takes a `Component`
target.
**Symptom:** A raw `new Element("select")` cannot be passed to
`PropertyInput`. The workaround is to wrap it in a tiny `@Tag("select")
Component` subclass.
**Workaround used:** `com.example.uc3.NativeSelect` — a 12-line
`Component` wrapper.
**Suggested API:** Add a `PropertyInput(Element target, String name,
Class<T> type)` overload. The body is identical to the current Component
overload after the `.getElement()` call.

## No public introspection of trigger wiring

**Symptom:** Even outside tests — an add-on that wanted to inspect or
augment a trigger's bindings after the fact has no API. The old
`TriggerSupport.snapshotForTest()` was at least documented as test-only;
the new architecture has nothing.
**Suggested API:** A read-only view on `Element#getJsInitializers()` (or a
similar accessor) that returns the install expressions and their captures.
