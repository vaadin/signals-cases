# API gaps surfaced by the Fullscreen use cases

These are the rough edges hit while building the use cases on top of
vaadin/flow#24326 (the published successor of the closed #23616).
The published `25.2.fullscreen-SNAPSHOT` adds `FullscreenSession` /
`FullscreenSessionState` / `Component#exitFullscreen()` /
`Page#simulateFullscreenChange()` on top of the original surface, which
closed several gaps from an earlier draft of this file. The "Resolved"
section at the end records those for posterity.

## Permission state not surfaced reactively

**Where it bit us:** no specific UC — this is a latent gap that hits
embedded iframes (e.g. a Flow app inside a host page that didn't grant
the `fullscreen` permissions policy). `FullscreenState.UNSUPPORTED` is
reported, but it conflates "browser doesn't support fullscreen at all"
with "this document is not permitted to enter fullscreen". The
distinction matters for the message we show the user: "Your browser
doesn't support fullscreen" vs "This page isn't allowed to enter
fullscreen — check the iframe `allow` attribute".
**Workaround used:** treat both as UNSUPPORTED.
**Suggested API:** either a finer-grained `FullscreenState`
(`UNSUPPORTED`, `PERMISSION_DENIED`) or an additional
`Page#fullscreenPermission()` signal mirroring how the Permissions
API exposes other capabilities. `FullscreenSession.error()` partly
mitigates this for the request path — but the steady-state "is it
allowed?" question still has no answer.

## No simulator for `FullscreenSession` state transitions

**Where it bit us:** `uc6/ChartExpandViewTest.sessionEndExitedByUserClearsExpandedClass`
and the unwritten "active card" assertion.
**Symptom:** `Page#simulateFullscreenChange(FullscreenState)` drives
the global `fullscreenSignal()` and (transitively) the open
session's `EXITED_BY_*` terminal state — but there is no public way
to move a session from `PENDING` to `ACTIVE` or `REJECTED`. UC6 has
to update its `activeOwner` mirror eagerly in the click handler
because in tests the session never reaches `ACTIVE`.
**Workaround used:** mirror `session.owner()` into a private
`ValueSignal<Optional<Component>>` immediately on click rather than
waiting for `session.stateSignal()` to fire `ACTIVE`.
**Suggested API:** add overloads such as
`Page#simulateFullscreenRequestOutcome(boolean success, String error)`
or `FullscreenSession#simulateActive()` /
`#simulateRejected(String)` so tests can drive the full lifecycle.
Without them, tests have to either skip the `PENDING → ACTIVE`
transition or mirror state outside the session.

## `requestFullscreen()` is a silent no-op outside user activation

**Where it bit us:** mentioned in the Javadoc; we did not surface it
to users in any UC.
**Symptom:** if a view's constructor or a server-push handler calls
`Component#requestFullscreen()`, the returned session ends up in
`REJECTED` (good) — but nothing tells the developer that's *because*
the call was made outside a user gesture. The server only sees an
empty `session.error()` and a generic browser message.
**Workaround used:** none.
**Suggested API:** detect the case where `requestFullscreen()` is
invoked outside the bounds of a UI event handler and log at WARN
level with a clear "called without user activation" message before
even sending it to the client. Optional: keep the request and arm a
"next user gesture" hook to fire it on the upcoming click.

## No usage example in the Javadoc for binding the signal

**Where it bit us:** view authoring, not user-facing.
**Symptom:** the canonical pattern is `someElement.bindClassName(...,
fullscreenSignal().map(...))`, but the Javadoc on
`Page#fullscreenSignal()` only narrates the four states. Several
minutes were spent re-reading the page-visibility use cases to
confirm the shape.
**Workaround used:** none — copy from the page-visibility module.
**Suggested API:** add a short usage block to
`Page#fullscreenSignal()` Javadoc showing both `Signal.effect(...)`
and `bindClassName(...)` patterns, or link out to a "common patterns"
section in the Vaadin docs once the API ships.

---

## Resolved in the published `25.2.fullscreen-SNAPSHOT`

For history, the following gaps were noted against an earlier draft of
the PR (when it was still vaadin/flow#23616) and were addressed by the
expanded surface in vaadin/flow#24326:

| Gap | How it's resolved |
| --- | ----------------- |
| No public test simulator for the fullscreen signal | `Page#simulateFullscreenChange(FullscreenState)` |
| Signal can't tell *which* component is fullscreen | `FullscreenSession#owner(): Optional<Component>` |
| No way to distinguish programmatic vs user-initiated exit | `FullscreenSessionState.EXITED_BY_CODE` / `EXITED_BY_USER` on `FullscreenSession#stateSignal()` |
| No outcome / no Future from `requestFullscreen()` | `FullscreenSession.stateSignal()` going `PENDING → ACTIVE`/`REJECTED`, plus `FullscreenSession.error()` |
| `Component.exitFullscreen()` does not exist | Added; UC3's “Done” button uses it directly |
