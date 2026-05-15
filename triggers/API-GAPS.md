# Trigger / Action API — gaps surfaced by the use-case module

This module exercises slice 1 of the trigger API
(<https://github.com/vaadin/flow/pull/24353>). The use cases that fit slice 1
have a view (UC1–UC5). Several other use cases that the feature is *for* — i.e.
client-only actions that need a user-gesture and would otherwise force a server
round-trip — can't be expressed yet. They are recorded here so the next slice
can prioritise.

## No client-side handler for `ServerCallbackAction` (round-trip after copy)

**Where it bit us:** No UC view; mentioned in `Trigger#triggers(SerializableRunnable)`.
**Symptom:** `new ClickTrigger(button).triggers(() -> ...)` registers a
`ServerCallbackAction` (type id `flow:server-callback`) on the server, but
`Triggers.ts` ships no `flow:server-callback` factory. The server callback
never runs. The action's own javadoc admits this: *"binding this action emits
the snapshot entry but no client behaviour is wired up"*.
**Workaround used:** None — a UC that wanted "copy AND tell the server it
happened" had to be left out.
**Suggested API:** Land the matching client handler in slice 2; until then a
`ServerCallbackAction` should either throw on registration or log a clear
warning, so applications don't silently drop callbacks.

## No `ShortcutTrigger` (keyboard shortcuts can't fire client-only actions)

**Where it bit us:** No UC view (Ctrl+C → copy).
**Symptom:** The only built-in `Trigger` is `ClickTrigger`. A clipboard-copy
fired by a keyboard shortcut would need a `ShortcutTrigger(host, key, mods)`,
but slice 1 doesn't ship one. Writing one against the public SPI is doable but
needs an accompanying JS factory.
**Suggested API:**
```java
new ShortcutTrigger(button, Key.KEY_C, KeyModifier.CONTROL)
    .triggers(new ClipboardCopyAction(value));
```

## No `FullscreenAction` (the PR's own motivating example)

**Where it bit us:** No UC view (toggle fullscreen on click).
**Symptom:** The PR description names fullscreen as a target use case for the
trigger API, but slice 1 only ships clipboard copy. A fullscreen toggle action
would be a 30-line subclass of `AbstractAction` plus a small TS factory — but
that's an extension job, not built-in.
**Suggested API:** Ship `FullscreenAction(target)` (and an exit/toggle
variant) so the most-cited use case works without each app re-implementing
it.

## No `WebShareAction` (the PR's other motivating example)

**Where it bit us:** No UC view.
**Symptom:** Same shape as the fullscreen gap — the PR description cites
`navigator.share` as motivation, but no built-in action ships it. Apps that
want server data → `share` have to write the action themselves; a custom
shim isn't reachable from the simple use case the API is meant to enable.
**Suggested API:** `WebShareAction(titleOutput, textOutput, urlOutput)` whose
client factory calls `navigator.share({...})`.

## No `SignalOutput` (signal values can't feed actions)

**Where it bit us:** UC4 (share URL widget) had to stash the URL in a
read-only `TextField` so `PropertyOutput` could read it back. The natural
shape would be `new SignalOutput<>(shareUrlSignal)` so the action reads
straight from the server-side signal.
**Symptom:** `PropertyOutput` is the only built-in `Output`; it can only read
DOM properties. The `Output` javadoc gestures at "use a `SignalOutput` if you
need a server-side `Signal` to feed the value" — but no such class ships.
**Workaround used:** Render the value into a hidden/read-only field and read
the property.
**Suggested API:**
```java
Signal<String> url = ...;
new ClickTrigger(button).triggers(new ClipboardCopyAction(new SignalOutput<>(url)));
```

## No test simulator / mock for the client side

**Where it bit us:** UC1–UC5 tests.
**Symptom:** `Element.executeJs(...)` runs the snapshot bind on the client at
runtime, so a browserless test can't observe what actually fires when the
trigger goes off. The tests fall back to inspecting
`TriggerSupport.on(host).snapshotForTest()` — they verify the wiring is
correct, but not that the client factories run, the clipboard write succeeds,
or the user-gesture context is preserved.
**Workaround used:** Treat the snapshot as the test contract: type ids,
config keys, element/output ids, bindings list. Functional behaviour has to
be tested with a real browser (as the upstream `TriggerClipboardCopyIT`
does).
**Suggested API:** A `TriggerSupportTestKit` that drives a trigger's "fire"
path from JUnit, instantiating registered factories against a stubbed DOM
(JSDOM-style). Alternatively, expose hooks so tests can assert "if trigger 0
fires, action 0 sees output 0 with value X".

## No server-side feature-detection for trigger support

**Where it bit us:** Not blocking any UC, but relevant for the share / file
system / payment / clipboard-read action variants that would land later.
**Symptom:** Slice 1 always emits the snapshot; if the browser lacks
`navigator.clipboard`, the client factory `console.debug`s and the click does
nothing. There is no server-side way to say *"don't even render the Copy
button on this browser"*.
**Suggested API:** A `Feature.detect(host, "clipboard")` Signal/CompletableFuture
or a `Trigger.onAvailable(...)` hook that surfaces support state back to the
server. Until then, applications must always show the trigger UI and live
with the silent no-op when the API is missing.

## `flow:property` output ignores `elementIndex === 0` (the host)

**Where it bit us:** None of UC1–UC5 — every UC referenced a non-host element
for its output. But this is a latent gap: the `Triggers.ts` factory has a
comment-laden explicit early-return when the property is read against the
host element, with the note *"elementIndex 0 means 'host'; not supported for
property outputs in v0 (outputs aren't bound to the host element directly)"*.
**Symptom:** A natural pattern — copy *this very button's* `textContent` —
silently produces `undefined`.
**Suggested API:** Either lift the restriction (the host element is in scope
as `this` on the client) or surface a `referenceHost()` flag that says
"resolve to the same element this trigger fired on".

## No way to introspect bindings without `snapshotForTest`

**Where it bit us:** All five UC tests.
**Symptom:** Verifying that a trigger is wired correctly required calling
`TriggerSupport.on(host).snapshotForTest()`, which is marked `// Test-only
accessors.` in the PR. Production code that needed to inspect its own
bindings (e.g. an add-on that wants to add a server-side mirror to an
existing trigger) has no supported API.
**Suggested API:** Expose a stable public view —
`TriggerSupport#getTriggers()`, `#getActionsFor(Trigger)` — separate from
the JSON snapshot.
