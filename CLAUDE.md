# Repository conventions

## Testing

Every use case must have a browserless test. When adding a new `uc*View`
(or any equivalent demo view) under any module in this repo, also add a
sibling test in `src/test/java/...` that at minimum:

- navigates to the view via `navigate(MyView.class)`,
- asserts that the expected top-level components render (heading, key
  buttons, the primary signal-bound container),
- exercises the view's signal wiring with at least one interaction or
  state mutation when the view has any.

Use `SpringBrowserlessTest` (from `browserless-test-spring`) with
`@SpringBootTest` and `@ViewPackages(classes = MyView.class)`. Existing
examples to mirror: `signals/src/test/java/com/example/usecase19/`
(per-view interaction), `signals/src/test/java/com/example/muc01/`
(cross-session state via a shared bean).

When a view depends on an application-scoped bean that holds shared
state, also write a test that simulates a second session — either by
mutating the bean directly from the test thread, or by calling
`cleanVaadinEnvironment()` + `initVaadinEnvironment()` between two
`navigate()` calls. This catches `ListSignal`-vs-`SharedListSignal`
mistakes and other cross-session leaks at compile-and-test time rather
than at runtime in the second browser tab.
