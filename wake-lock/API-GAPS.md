# Wake Lock API — gaps observed while building the use cases

The Screen Wake Lock API exposed in vaadin/flow#23619 carried all four
use cases through to a green build. Nothing was *blocked* by missing
API surface. The notes below are friction points where the use cases
needed a workaround or where the surface felt thinner than the rest of
the page-level facades in Flow (compare e.g. `Page#pageVisibilitySignal()`
which has a paired `GeolocationSimulator`-style test simulator).

## No test simulator / no way to drive the signal from a browserless test

**Where it bit us:** all four use cases, via
`wake-lock/src/test/java/com/example/WakeLockTestSupport.java`.
**Symptom:** `WakeLock#setActive(String)` is package-private — only the
JS bridge calls it in production. A browserless test that wants to
verify a status badge transitions to "Holding lock" has no way to
simulate the browser confirming the lock without going through the
client.
**Workaround used:** reflective access to `WakeLock#setActive` via
`WakeLockTestSupport.simulateAcquired() / simulateReleased()`. Mirrors
`PageVisibilityTestSupport` in the page-visibility module, which uses
the same trick for the same reason.
**Suggested API:** a `WakeLockSimulator` analogous to
`GeolocationSimulator` in flow-server-test — `simulateAcquired()`,
`simulateReleased()`, plus an `isLockRequested()` predicate so a test
can also verify that the server-side `request()` call reached the
client. Without an `isLockRequested()` hook, UC1's "click invokes
wakeLock" assertion has to settle for "after clicking, the next
simulated ACTIVE event is reflected on screen" — a cause-and-effect
chain, not a direct observation.

## No way to feature-detect support from the server

**Where it bit us:** every use case, especially UC1 (the
manual-toggle view) and UC2 (recipe view).
**Symptom:** the Wake Lock API needs a secure context (HTTPS or
`localhost`) and is gated behind Safari ≥ 16.4. A view that requests
the lock on an unsupported browser is left with `activeSignal()`
permanently false, but the user can't tell whether the lock was
refused, never granted, or simply not yet confirmed.
**Workaround used:** the badge label hedges ("Released — waiting for
browser"). UC1 cannot grey out the toggle when the API is unsupported
because the server has no way to know.
**Suggested API:** `WakeLock#supportedSignal()` returning
`Signal<Boolean>` (or a tri-state `WakeLockSupport` enum:
`SUPPORTED / UNSUPPORTED / UNKNOWN`). The client already knows the
answer — `'wakeLock' in navigator` — and could send it once during the
first roundtrip.

## `request()` and `release()` are fire-and-forget

**Where it bit us:** UC1, UC3 (slideshow), UC4 (workout timer).
**Symptom:** `request()` returns `void`. A caller cannot know whether
the browser denied the request, was already holding the lock, or is
still asking. The only signal of failure is the absence of an
`activeSignal()` flip — there is no `WakeLockDeniedEvent` and no
distinction between *not yet acquired* and *will never be acquired*.
**Workaround used:** views observe `activeSignal()` only and present
"Released" as the catch-all for "we asked but nothing came back".
**Suggested API:** either a `request()` overload returning
`CompletableFuture<Boolean>` (true = acquired, false = denied), or a
separate read-only `lastErrorSignal()` that surfaces the most recent
reason the lock failed (insecure context, low battery, user revoked,
unsupported).

## No reason exposed when the browser drops the lock

**Where it bit us:** UC3 and UC4.
**Symptom:** when the tab hides, the browser drops the lock and the
client transparently re-acquires it on return. That's exactly what
UC3 / UC4 want — but the signal flips false-true-false-true and the
view has no way to distinguish "user revoked", "low battery", and
"transient hide" without correlating with `pageVisibilitySignal()`.
Apps that want to give up after N revocations cannot.
**Workaround used:** UC3 / UC4 simply re-issue `request()` from a
Signal effect; the client's built-in auto-reacquire makes that mostly
redundant but harmless.
**Suggested API:** a `WakeLockReleaseReason` enum carried on the
`vaadin-wake-lock-change` event (`USER_RELEASE`, `VISIBILITY`,
`BATTERY_SAVER`, `OS_LOCK`, …), and exposed as a paired
`releaseReasonSignal()` on the facade.

## Client `wanted` flag is a module-level singleton

**Where it bit us:** not yet — but UC2 (recipe) and UC5/dashboard-style
scenarios would have hit this if the user opens two tabs of the same
app.
**Symptom:** in `flow-client/src/main/frontend/WakeLock.ts` the
`wanted`, `sentinel`, and `visibilityListenerInstalled` variables are
module-globals. A page can have multiple UIs (popout windows, embedded
iframes) — calling `release()` on one would drop the lock for the
other.
**Workaround used:** none needed for these use cases (single UI per
tab).
**Suggested API:** scope `wanted` / `sentinel` by the element the
facade is bound to (the UI element passed into `request(element)`),
not module-wide. The event already carries `element` so the server
side could distinguish them.

## No "lock anything other than the screen"

**Where it bit us:** considered for UC4 (workout timer) — the spec
allows `wakeLock.request('system')` in some browsers to keep CPU
awake too. Out of scope for the PR, but worth noting if Flow ever
exposes a richer set of lock types.
**Suggested API:** `WakeLock#request(WakeLockType)` overload with an
enum (today only `SCREEN`; later `SYSTEM` or others if the spec
expands).
