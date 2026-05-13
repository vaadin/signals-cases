# Screen Orientation API — gaps surfaced while building use cases

Gaps below are the places where the API in
[vaadin/flow#23620](https://github.com/vaadin/flow/pull/23620) was insufficient
or awkward for the five use cases in this module. Each entry names the use
case where it bit us, the symptom, the workaround, and a suggested shape for
the real fix.

## No `ScreenOrientationSimulator` for tests

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc2 / `OrientationViewerView`,
uc3 / `RotatePromptView` (and their tests).
**Symptom:** Tests cannot drive `Page#screenOrientationSignal()` through any
public API. The geolocation feature exposes `GeolocationSimulator.current()`
for this purpose; the new screen-orientation API has nothing equivalent.
**Workaround used:** `ScreenOrientationTestSupport` (in `src/test/java`) calls
`Page#setScreenOrientation(String, String)` reflectively. That setter is
package-private precisely because it is the JS-bridge entry point, so
exposing a proper test seam would be cleaner.
**Suggested API:** Either ship a `ScreenOrientationSimulator` alongside
`GeolocationSimulator` (`ScreenOrientationSimulator.current().setOrientation(
ScreenOrientation, int angle)`), or promote the package-private setter to a
test-friendly form on a dedicated `ScreenOrientationTesting` utility class.

## Lock requires fullscreen, but Flow has no fullscreen API

**Where it bit us:** uc4 / `LockForVideoView`.
**Symptom:** Most browsers reject `screen.orientation.lock()` outside of
fullscreen with `SecurityError`. The natural use case for locking ("watch
this video in landscape") demands a `requestFullscreen()` call right before
the lock, but Flow has no API for fullscreen requests.
**Workaround used:** `MissingAPI.requestFullscreen(Component)` /
`MissingAPI.exitFullscreen(Component)` shims that wrap an
`element.requestFullscreen()` `executeJs` call.
**Suggested API:** A `Component#requestFullscreen()` / `Page#requestFullscreen(
Component)` pair, ideally with a `Signal<Boolean>` for fullscreen state so
the lock flow can be driven reactively rather than imperatively. Track at
vaadin/flow.

## No `Signal<Boolean>` for `isLandscape` / `isPortrait`

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc3 / `RotatePromptView`.
**Symptom:** `ScreenOrientation#isLandscape()` and `#isPortrait()` exist on
the enum but the natural binding shape we want is a `Signal<Boolean>` over
"is the device currently in landscape?". Today the consumer has to do
`screenOrientationSignal().map(d -> d.type().isLandscape())` everywhere.
**Workaround used:** A `.map(...)` in every consumer. Tolerable, but boilerplate.
**Suggested API:** Convenience accessors on `Page` returning derived signals —
`Page#isLandscapeSignal()` and `Page#isPortraitSignal()` — both mapping to
`false` when the type is `UNKNOWN` or `UNSUPPORTED`, matching the existing
enum behaviour.

## Pre-bootstrap `UNKNOWN` is briefly observable in `onAttach`

**Where it bit us:** uc1 / `AdaptiveLayoutView`, uc2 / `OrientationViewerView`,
uc3 / `RotatePromptView`.
**Symptom:** Views attached during the very first request still see
`ScreenOrientation.UNKNOWN` on the very first signal read because the
client-side bootstrap parameter has not been processed yet when `onAttach`
runs in some test/dev scenarios. Each view ends up needing an explicit
`UNKNOWN` branch in its mapping logic just to render something sensible
for that one tick.
**Workaround used:** Every view enumerates the `UNKNOWN` branch in its
switch — usually as "treat like UNSUPPORTED" or "treat like portrait".
**Suggested API:** Either (a) document a recommended default mapping, or
(b) consider a `screenOrientationDataSignalOrDefault(ScreenOrientation
fallback)` accessor that swaps `UNKNOWN` for an application-provided
fallback before the value reaches consumers.

## No way to feature-detect screen-orientation support from the server before bootstrap

**Where it bit us:** uc3 / `RotatePromptView`, uc4 / `LockForVideoView`.
**Symptom:** To decide whether to show a "rotate your device" hint or a
"lock orientation" button, you want a synchronous answer to "does this
browser implement the Screen Orientation API?". Today you must wait for the
signal to settle on `UNSUPPORTED` — distinct from `UNKNOWN` but still
asynchronous.
**Workaround used:** Treat `UNSUPPORTED` as feature-detect, but accept a
brief window where the UI offers a control that will fail on click.
**Suggested API:** Add `ExtendedClientDetails#supportsScreenOrientation()` (the
bootstrap already carries enough information to fill it in synchronously).
That mirrors how `ExtendedClientDetails` already exposes other
"feature-detect" booleans.

## `unlockOrientation()` has no completion callback

**Where it bit us:** uc4 / `LockForVideoView`.
**Symptom:** `Page#lockOrientation(orientation, onSuccess, onError)` exposes
both success and error branches reactively, but `Page#unlockOrientation()` is
fire-and-forget. There is no way to tell whether the unlock landed before
the user navigates away.
**Workaround used:** None — we assume unlock always lands. This is fine on
the happy path but unhelpful for telemetry and "are we fully cleaned up?"
checks.
**Suggested API:** `Page#unlockOrientation(SerializableRunnable onComplete)`
or a `Page#unlockOrientation(): CompletableFuture<Void>`-style return,
modelled on the `lockOrientation` callback shape.
