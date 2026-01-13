# Signal API Testing Results

## Summary
Found critical issue: `Signal.effect()` callbacks are not re-running when signal values change, causing all reactive UI updates to fail after initial render.

## Test Results

### ✅ Use Case 1: User Profile Settings
**Status**: WORKING
**Test**: Toggle "Enable Advanced Mode" checkbox
**Result**: Advanced settings panel shows/hides correctly
**Why it works**: Uses `bindVisible()` which is implemented in Vaadin core, not our MissingAPI

### ❌ Use Case 2: Form Field Synchronization
**Status**: BROKEN
**Test**: Type "John" in First Name, "Doe" in Last Name
**Expected**: Welcome header should update to "Welcome, john.doe"
**Actual**: Header shows "Welcome," with no username
**Root cause**: `bindText()` effect not re-running when `usernameSignal` changes

### ❌ Use Case 3: Dynamic Button State
**Status**: BROKEN
**Test**: Fill email, password, and matching confirm password
**Expected**: "Create Account" button should become enabled
**Actual**: Button stays disabled
**Root cause**: `bindEnabled()` effect not re-running when validation signals change

## Root Cause Analysis

The issue is in `MissingAPI.java`. The `Signal.effect()` callbacks are:
1. ✅ Being registered successfully (no errors)
2. ✅ Running once on initial setup
3. ❌ NOT re-running when signal dependencies change

### Current Implementation Pattern
```java
public static void bindText(HasText component, Signal<String> signal) {
    UI ui = UI.getCurrent();
    Signal.effect(() -> {
        String text = signal.value();        // Reads signal (should track dependency)
        ui.access(() -> component.setText(text));  // Updates UI asynchronously
    });
}
```

### Hypothesis
The `Signal.effect()` return value (a `Runnable` for cleanup) is being discarded. Effects may need to be kept alive for the lifetime of the component.

## Impact
- **Working**: Use Case 1 (uses Vaadin's native `bindVisible`)
- **Broken**: Use Cases 2, 3, and likely all others that depend on MissingAPI bindings
- All `bindText`, `bindEnabled`, `bindAttribute`, `bindThemeName`, `bindClassName`, `bindRequired`, `bindHelperText`, `bindItems`, `bindChildren` are affected

## Next Steps
1. Investigate Signal.effect() lifecycle - does it need to be kept as a strong reference?
2. Test if storing effect cleanup Runnables fixes the issue
3. Consider alternative: use Signal's built-in UI integration if available
4. May need to use Component.addDetachListener() to clean up effects
