# Use Case Gaps Analysis

## Current Coverage (10 Use Cases)

### What We Have
1. **UC 3**: Dynamic Button State - form validation, async operations
2. **UC 7**: Progressive Disclosure with Binder - complex conditional forms
3. **UC 8**: Permission-Based UI - uses Spring Security roles
4. **UC 9**: Filtered Data Grid - collection rendering, filtering
5. **UC 11**: Cascading Selectors - dependent dropdowns
6. **UC 12**: Shopping Cart - cross-component state, computed totals
7. **UC 13**: Master-Detail - selection state across components
8. **UC 14**: Multi-Step Wizard - navigation state, validation per step
9. **UC 15**: Form with Binder - integration with Vaadin's data binding
10. **UC 17**: Grid with Signal Providers - editable cells, row selection based on signals

## Missing Coverage Areas

### 1. Multi-User / Collaborative Features ❌

**Not Covered**: Real-time collaboration where multiple users interact with shared state.

**Proposed New Use Cases:**

**UC 18: Shared Chat/Message List**
- **Scenario**: Chat-like view with append-only shared list signal
- **Signal Features**:
  - Shared `Signal<List<Message>>` across users
  - Server-side signal source (Push/WebSocket)
  - Append-only operations (add message to end)
  - Each user can only add, not modify history
- **API Needs**:
  - Server-side Signal source
  - Signal.fromPush() or similar
  - Collaborative Signal API

**UC 19: Collaborative Cursor Positions**
- **Scenario**: Show all users' cursor positions in a shared document/canvas
- **Signal Features**:
  - Each user updates own cursor position signal
  - All users read all cursor signals
  - Map<UserId, Signal<CursorPosition>>
  - Real-time updates with Push
- **API Needs**:
  - Per-user writable signals
  - Shared read-only signal map
  - Efficient multi-signal updates

**UC 20: Competitive Button Click Game**
- **Scenario**: Button appears, fastest clicker gets points, all see leaderboard
- **Signal Features**:
  - Optimistic UI with conflict resolution
  - Server-authoritative signal (only server can update points)
  - Race condition handling
  - Shared leaderboard Signal<Map<User, Score>>
- **API Needs**:
  - Server-side signal coordination
  - Conflict resolution strategy
  - Atomic operations on shared signals

**UC 21: Collaborative Form Editing with Locking**
- **Scenario**: Multiple users editing shared data, field-level locking
- **Signal Features**:
  - Field lock status Signal<Map<Field, User>>
  - Optimistic locking on save
  - Show who's editing what
  - Conflict detection on submit
- **API Needs**:
  - Distributed signal state
  - Lock/unlock operations
  - Merge conflict detection

### 2. Browser/Environment Signals ❌

**Not Covered**: Using browser state (window size, online status, etc.) as reactive signals.

**Proposed New Use Case:**

**UC 22: Responsive Layout with Window Size Signal**
- **Scenario**: Different UI layout for small vs large screens
- **Signal Features**:
  - `Signal<WindowSize>` from browser window resize events
  - Computed Signal<Boolean> for isSmallScreen
  - ComponentToggle based on screen size
  - Responsive component visibility/layout
- **API Needs**:
  - Signal.fromBrowserEvent() or similar
  - Browser window dimensions as signal source
  - Debounced resize signal

**Other Browser Signals to Consider:**
- Online/offline status
- Battery level (for PWAs)
- Geolocation
- Media query matching
- Preferred color scheme (dark mode)

### 3. Cross-Component Communication (Partial Coverage)

**Partially Covered**: UC 12, 13 show some cross-component state

**Gap Identified**:

**UC 23: Dynamic View Title in Layout & Browser Tab**
- **Scenario**: View defines its title, shown in main layout header and browser tab
- **Signal Features**:
  - View exposes `titleSignal` to layout
  - Layout binds to view's title signal
  - Browser tab title updates automatically
  - Title = "App Name - " + viewTitle
- **API Needs**:
  - Signal propagation from view to layout
  - Browser document.title binding
  - Signal composition (prefix + view title)

### 4. Authentication/User Context Signals (Partial Coverage)

**Partially Covered**: UC 8 uses Spring Security but doesn't use signals

**Gap Identified**:

**UC 24: Current User as Signal**
- **Scenario**: User info in signal, used across views and layout
- **Signal Features**:
  - Application-wide `Signal<User>` from SecurityContext
  - Used in MainLayout to show username
  - Used in views to customize UI per user
  - Reactive to impersonation changes
- **API Needs**:
  - Signal from Spring Security context
  - Application-scoped signals
  - Integration with VaadinSession

## Summary

### Currently Missing
| Category | Count | Priority |
|----------|-------|----------|
| Multi-user/Collaborative | 4 use cases | HIGH (if collaborative features are in scope) |
| Browser/Environment Signals | 1+ use cases | MEDIUM-HIGH (responsive design is common) |
| Cross-component Title | 1 use case | MEDIUM (common pattern) |
| User Context Signal | 1 use case | MEDIUM (builds on existing UC 8) |

### Questions to Answer

1. **Are collaborative/multi-user features in scope for Signal API?**
   - If YES: Add UC 18-21
   - If NO: Document as out of scope

2. **Should browser events be signal sources?**
   - Window resize, online status, etc.
   - If YES: Add UC 22
   - If NO: Consider separate API

3. **Is application-wide signal sharing needed?**
   - User context, theme, language
   - If YES: Add UC 24
   - If NO: Keep signals view-scoped

4. **Priority order for new use cases:**
   - Most valuable: UC 22 (responsive), UC 24 (user context)
   - Most complex: UC 18-21 (multi-user)
   - Most common: UC 23 (view title)

## Recommendation

**Phase 1 - Add Common Single-User Patterns:**
- UC 22: Responsive Layout (window size signal)
- UC 23: Dynamic View Title
- UC 24: Current User Signal

**Phase 2 - Multi-User (if in scope):**
- UC 18: Shared Message List
- UC 19: Collaborative Cursors
- UC 20: Race Condition Handling
- UC 21: Collaborative Editing with Locks

This would bring us from 10 to 13 use cases (Phase 1) or 17 use cases (both phases).
