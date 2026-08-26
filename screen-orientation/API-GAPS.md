# Screen Orientation API — gaps surfaced while building use cases

Rough edges hit while building the five use cases on the
`com.vaadin.flow.component.screenorientation` API. Each entry names the use
case where it bit us, the symptom, the workaround, and a suggested shape for
the real fix. The API:

- `ScreenOrientation.orientationSignal()` returns
  `Signal<ScreenOrientationData>`, where `ScreenOrientationData` is a record
  `(ScreenOrientationType type, int angle)`; `ScreenOrientationType` has
  `isLandscape()` / `isPortrait()`.
- `ScreenOrientation.lock(ScreenOrientationType, onSuccess, onError)` — the
  error carries a `ScreenOrientationLockError` (`ScreenOrientationLockErrorCode`
  + debug info) — and `ScreenOrientation.unlock()` /
  `unlock(SerializableRunnable onComplete)`.

Last verified against Vaadin / Flow `25.3-SNAPSHOT` on 2026-08-26.

## No `ScreenOrientationSimulator` for tests

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc2 / `OrientationViewerView`,
uc3 / `RotatePromptView` (and their tests).
**Symptom:** There is no dedicated, discoverable simulator for driving
`ScreenOrientation.orientationSignal()` in tests. The geolocation feature
exposes `GeolocationSimulator.current()` for this purpose; the
screen-orientation API has nothing equivalent.
**Workaround used:** `ScreenOrientationTestSupport` (in `src/test/java`) drives
the signal through the public
`UI.getCurrent().getInternals().setScreenOrientationFromClient(String type,
String angle)` — no reflection needed. That public seam works, but is the
raw JS-bridge entry point rather than a purpose-built testing helper.
**Suggested API:** Ship a `ScreenOrientationSimulator` alongside
`GeolocationSimulator` (`ScreenOrientationSimulator.current().setOrientation(
ScreenOrientationType, int angle)`) so tests have a discoverable, typed seam
rather than reaching into `UIInternals#setScreenOrientationFromClient`.

## No `Signal<Boolean>` for `isLandscape` / `isPortrait`

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc3 / `RotatePromptView`.
**Symptom:** `ScreenOrientationType#isLandscape()` and `#isPortrait()` exist on
the enum but the natural binding shape we want is a `Signal<Boolean>` over
"is the device currently in landscape?". The consumer has to do
`ScreenOrientation.orientationSignal().map(d -> d.type().isLandscape())`
everywhere.
**Workaround used:** A `.map(...)` in every consumer. Tolerable, but boilerplate.
**Suggested API:** Convenience derived signals —
`ScreenOrientation.isLandscapeSignal()` and
`ScreenOrientation.isPortraitSignal()` — both mapping to `false` when the type
is `UNKNOWN` or `UNSUPPORTED`, matching the existing enum behaviour.

## Pre-bootstrap `UNKNOWN` is briefly observable in `onAttach`

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc2 / `OrientationViewerView`,
uc3 / `RotatePromptView`.
**Symptom:** Views attached during the very first request still see
`ScreenOrientationType.UNKNOWN` on the very first signal read because the
client-side bootstrap parameter has not been processed yet when `onAttach`
runs in some test/dev scenarios. Each view ends up needing an explicit
`UNKNOWN` branch in its mapping logic just to render something sensible
for that one tick.
**Workaround used:** Every view enumerates the `UNKNOWN` branch in its
switch — usually as "treat like UNSUPPORTED" or "treat like portrait".
**Suggested API:** Either (a) document a recommended default mapping, or
(b) consider a `orientationSignalOrDefault(ScreenOrientationType
fallback)` accessor that swaps `UNKNOWN` for an application-provided
fallback before the value reaches consumers.

## Feature-detecting screen-orientation support — accessor landed, pre-bootstrap window did not

**Where it bit us:** uc3 / `RotatePromptView`, uc4 / `LockForVideoView`.
**Symptom:** To decide whether to show a "rotate your device" hint or a
"lock orientation" button, you want a synchronous answer to "does this
browser implement the Screen Orientation API?". You must wait for the
signal to settle on `UNSUPPORTED` — distinct from `UNKNOWN` but still
asynchronous.
**Was suggested:** `ExtendedClientDetails#supportsScreenOrientation()`, filled in
synchronously from the bootstrap.
**Status: half of it shipped.** `ExtendedClientDetails#isScreenOrientationSupported()`
exists since 25.2 and gives the synchronous boolean. But it is derived from the
signal, not from the bootstrap payload:

```java
ScreenOrientationType type = ScreenOrientation.orientationSignal(ui).peek().type();
return type != UNKNOWN && type != UNSUPPORTED;
```

so it returns `false` both when the browser has no Screen Orientation API *and*
before the client handshake has seeded the signal — exactly the pre-bootstrap
window the gap was about, and the same `UNKNOWN`-conflation described in the
previous section. Reading it from `onAttach` on a first request would therefore
hide the lock button on a device that does support locking.
**Workaround used:** unchanged — the use cases keep binding to
`orientationSignal()` and treat `UNSUPPORTED` as the feature-detect, so the UI
corrects itself when the real value arrives. The new accessor is the right tool
for a one-shot decision *after* the handshake, and both views would regress if
they used it at attach time, so they don't.
**Still suggested:** make the accessor tri-state (or seed it from the bootstrap
parameters, which already carry the value) so "not known yet" is distinguishable
from "not supported".
