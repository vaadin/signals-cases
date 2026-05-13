# Web Share API — API gaps discovered while building the demos

This file lists places where the Flow Web Share API in [vaadin/flow#24325](https://github.com/vaadin/flow/pull/24325)
either did not yet cover a genuine Web Share use case or made one awkward
enough to work around. Each entry is keyed to the use case that surfaced it.

## File sharing (Web Share Level 2)

**Where it bit us:** dropped from the UC list — no view written
**Symptom:** The Web Share API spec defines a `files: File[]` member on
`ShareData` (the "Web Share Level 2" addition). Modern Chrome/Safari on
Android and iOS surface this when sharing photos, generated PDFs, or any
arbitrary blob produced server-side (e.g. a generated invoice). Flow's
`Page#share(String title, String text, String url)` does not expose any way
to pass a file or a `StreamResource`, so there is no path from a Flow
`InputStream` to the native share sheet.
**Workaround used:** None — we dropped the file-share use case rather than
faking it with an `executeJs` blob. The shim could be added later (see
suggested API below) but isn't yet justified by the rest of the demo.
**Suggested API:**

```java
// New overload that accepts StreamResource attachments and is automatically
// rejected with UnsupportedOperationException when navigator.canShare
// reports the files cannot be shared.
PendingJavaScriptResult share(String title, String text, String url,
        StreamResource... files);
```

References: [Web Share Level 2 draft](https://wicg.github.io/web-share/level-2/),
[MDN: ShareData.files](https://developer.mozilla.org/en-US/docs/Web/API/Navigator/share#files).

## No `navigator.canShare(data)` pre-check

**Where it bit us:** uc3 / CustomMessageView.java, uc4 / ShareListItemsView.java
**Symptom:** The browser's `navigator.canShare(data)` predicate lets a page
verify a payload before showing the share button, returning `false` when
e.g. files are too large, the URL scheme is blocked, or the data shape is
otherwise unshareable. We could only check whether *any* share is supported
(`shareSupportSignal()`), not whether *this specific payload* is. As a
result UC3 and UC4 happily enable the Share button even for payloads the
browser would reject.
**Workaround used:** Trust `shareSupportSignal()` to gate the button.
Per-payload validation falls back to the share() promise rejecting at
invocation time, which is too late for good UX.
**Suggested API:**

```java
// Per-payload predicate. Returns a PendingJavaScriptResult<Boolean> so the
// caller can update the UI before the user clicks. Backed by
// navigator.canShare(data).
PendingJavaScriptResult canShare(String title, String text, String url);
```

## No test simulator / no browserless helper

**Where it bit us:** every UC test under `src/test/java/com/example/uc*/`
**Symptom:** The PR added the `WebShareSupport` enum and the signal but no
browserless test simulator. We had to write our own
`WebShareTestSupport.setSupport(state)` helper that pokes
`UI.getCurrent().getInternals().setWebShareSupport(state)`. That setter is
public but documented as "framework-internal" — a third-party developer
writing the same test would have to read flow-server source code to find
it.
**Workaround used:** `web-share/src/test/java/com/example/WebShareTestSupport.java`.
The actual share invocation cannot be verified at all in a browserless
test, only the surrounding signal-driven UI state.
**Suggested API:**

```java
// In browserless-test or a future flow-server test-fixtures jar.
public final class WebShareSimulator {
    /** Drives the support signal in the current UI. */
    public static void setSupport(WebShareSupport support);
    /** Records the last share() call so tests can assert on it. */
    public static SharePayload lastShareInvocation();
    /** Resolves the most recent share() pending result with success. */
    public static void completeLastShare();
    /** Rejects the most recent share() pending result with the given error string. */
    public static void failLastShare(String error);
}
```

Without the last three pieces, UC5's success/cancel/error branches can only
be exercised by reaching into package-private `handleSuccess` /
`handleError` methods on the view itself.

## No way to feature-detect from the server before bootstrap

**Where it bit us:** uc1 / ShareThisPageView.java, uc2 / CopyLinkFallbackView.java
**Symptom:** `shareSupportSignal()` is seeded from the client during the
bootstrap handshake — before that point it reads `UNKNOWN`. For
server-side rendering of an initial view (e.g. a Server-Side Rendered
landing page) you can't know whether to render the native-share button or
the copy-link button until the client has reported back, which causes a
visible swap on first paint (a tiny flash of the "Detecting…" state). The
PR docstring even says the value "is replaced with a real value before any
user code observes the signal" — but the `UNKNOWN` value is visible in
our `Signal.effect` and we have to render for it.
**Workaround used:** We render a disabled "Detecting…" placeholder during
the `UNKNOWN` window. This is fine for an SPA but would be a flicker for
SSR-first rendering.
**Suggested API:** Surface support state as part of the initial HTML
response (analogous to how `vaadin-script-tag` already inlines other
bootstrap params) so SSR renders can read it synchronously. Alternatively,
expose `Page#isShareSupported()` as a `Boolean` (nullable) that returns the
seeded value if it has already arrived, so server-side renderers can
short-circuit the `UNKNOWN` branch.

## No way to invoke share() without a click chain

**Where it bit us:** Considered but not surfaced as a UC.
**Symptom:** Web Share requires a transient user activation
(navigator.share rejects with `NotAllowedError` otherwise). Flow's
`Page#share()` happily lets you invoke it from a server-side timer or
scheduled task, which will always reject. There is no compile-time or
runtime guard against this misuse.
**Workaround used:** Document the requirement in the UC1 Javadoc. It's
hard to enforce server-side because the chain of causation crosses RPC.
**Suggested API:** Add a runtime check that throws / logs a warning when
`share()` is invoked outside a click/keypress listener stack frame; or at
minimum, mention the user-activation requirement in the Javadoc of
`Page#share`.

## `share()` swallows the PendingJavaScriptResult error by default

**Where it bit us:** uc5 / ShareFeedbackView.java
**Symptom:** `Page.share()` already attaches its own
`result.then(ok -> {}, err -> LOGGER.debug(...))` before returning. That
means an application that does *not* care about the result gets a free
debug log line; an application that *does* care attaches its own handler
on top of the framework's. That works (multiple handlers are allowed
before the snippet is flushed), but the docstring is silent about the
framework-installed handler. A developer reading the API may assume a
single `then()` is theirs alone.
**Workaround used:** Documented in UC5's Javadoc that an extra handler can
still be attached on top of the framework's logging one.
**Suggested API:** Either drop the internal debug-logging handler (callers
opt in to it) or document it explicitly so users know multiple handlers
will run. The latter is cheaper.
