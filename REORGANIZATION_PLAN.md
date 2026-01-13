# Use Case Reorganization Plan

## Overlap Analysis

### Existing Use Cases (UC 1-24)

**UC 1-8**: Basic bindings, computed, conditional (Single-User)
**UC 9**: Filtered/Sorted Grid - in-memory filtering/sorting only
**UC 10**: Task List - local modifications, "real-time" = reactive UI updates
**UC 11-13**: Cross-component interactions (Single-User)
**UC 14-17**: Complex forms (Single-User)
- **UC 15**: Already covers conditional validation (password match, business account requiring company name)
- **UC 16**: ComponentToggle for view switching, mentions responsive but not focused on window size
**UC 18-21**: Collaboration (Multi-User)
**UC 22**: Window resize events as signal source (Single-User)
- **DISTINCT from UC 16**: UC 16 is about ComponentToggle API, UC 22 is about browser events
**UC 23-24**: Browser/Context (Single-User)

### Proposed New Use Cases - Overlap Check

✅ **UC 25: Async Loading States** - NEW (Loading/Success/Error pattern)
✅ **UC 26: Debounced Search** - NEW (performance, debouncing)
✅ **UC 27: Paginated Grid** - DISTINCT from UC 9 (server-side vs in-memory)
✅ **UC 28: Form Dirty State** - NEW (modification tracking)
✅ **UC 29: Multi-Select Grid** - DISTINCT from UC 17 (selection vs editing)
✅ **UC 30: Global Notifications** - NEW (toast queue)
✅ **UC 31: URL State** - NEW (router integration)
✅ **UC 32: Auto-Save** - NEW (periodic save)
✅ **UC 33: Server-Sent Events** - DISTINCT from UC 10 (SSE vs local updates)
✅ **UC 34: Undo/Redo** - NEW (history management)

### Overlaps Removed

❌ **Conditional Validation** - REMOVED (already covered by UC 15)
❌ **Persistence/Dark Mode** - REMOVED (not Signal API scope)

### Result: 10 New Distinct Use Cases

## Proposed Reorganization

### Category 1: Single-User / UI State (27 use cases)

#### Subcategory A: Basic Patterns (UC 1-3)
1. User Profile Settings Panel - visibility toggle
2. Form Field Synchronization - computed values
3. Dynamic Button State - enable/disable

#### Subcategory B: Computed & Derived Values (UC 4-5)
4. E-commerce Product Configurator - complex computation
5. Dynamic Pricing Calculator - formula-based calculation

#### Subcategory C: Conditional Rendering (UC 6-8)
6. Dynamic Form with Conditional Subforms - show/hide sections
7. Progressive Disclosure with Nested Conditions - nested visibility
8. Permission-Based Component Visibility - role-based UI

#### Subcategory D: Lists & Collections (UC 9-10)
9. Filtered and Sorted Data Grid - in-memory filtering
10. Dynamic Task List - local modifications

#### Subcategory E: Cross-Component (UC 11-13)
11. Cascading Location Selector - dependent dropdowns
12. Shopping Cart with Real-time Totals - cart management
13. Master-Detail Invoice View - master-detail pattern

#### Subcategory F: Forms & Validation (UC 14-15)
14. Multi-Step Wizard with Validation - wizard pattern
15. Form with Binder Integration - Binder + validation

#### Subcategory G: Advanced UI Patterns (UC 16-17)
16. Responsive Dashboard with ComponentToggle - view switching
17. Employee Management Grid - dynamic editability

#### Subcategory H: Browser Integration (UC 18-20)
18. Responsive Layout with Window Size - browser events
19. Dynamic View Title - page title binding
20. Current User Signal - app-wide context

#### Subcategory I: Async & Performance (UC 21-23)
21. **NEW** Async Data Loading with States - Loading/Success/Error
22. **NEW** Debounced Search - performance optimization
23. **NEW** Paginated Data Grid - server-side pagination

#### Subcategory J: Form Management (UC 24-25)
24. **NEW** Form Dirty State Tracking - modification detection
25. **NEW** Form with Auto-Save - periodic save

#### Subcategory K: Selection & Bulk Operations (UC 26)
26. **NEW** Grid with Multi-Select and Bulk Actions - selection management

#### Subcategory L: System Integration (UC 27-28)
27. **NEW** Global Notification System - toast queue
28. **NEW** Search with URL State - router integration

#### Subcategory M: Advanced State Management (UC 29-30)
29. **NEW** Live Dashboard with Server-Sent Events - SSE integration
30. **NEW** Text Editor with Undo/Redo - history management

### Category 2: Multi-User / Collaboration (4 use cases)

#### MUC 1-4
MUC 1. Shared Chat/Message List - append-only collaboration (renamed from old UC 18)
MUC 2. Collaborative Cursor Positions - real-time position sharing (renamed from old UC 19)
MUC 3. Competitive Button Click Game - conflict resolution (renamed from old UC 20)
MUC 4. Collaborative Form Editing with Locking - field-level locking (renamed from old UC 21)

## Final Count

- **Single-User / UI State**: 30 use cases (UC 1-30)
- **Multi-User / Collaboration**: 4 use cases (MUC 1-4)
- **Total**: 34 use cases

## Implementation Status

### Current State
- UC 1-17: ✅ Implemented (Basic patterns, computed, conditional, lists, cross-component, forms, advanced UI)
- UC 18-21: ✅ Implemented (Collaboration: chat, cursors, click game, form locking)
- UC 22-24: ✅ Implemented (Browser integration: window size, title, user context)

### Files to Rename/Renumber
**Phase 1: Move browser integration up**
- Current UseCase22View.java → New UseCase18View.java (Window Size)
- Current UseCase23View.java → New UseCase19View.java (View Title)
- Current UseCase24View.java → New UseCase20View.java (User Context)

**Phase 2: Rename collaboration with MUC prefix**
- Current UseCase18View.java → New MUC01View.java (Shared Chat)
- Current UseCase19View.java → New MUC02View.java (Collaborative Cursors)
- Current UseCase20View.java → New MUC03View.java (Click Game)
- Current UseCase21View.java → New MUC04View.java (Form Locking)

### To Implement (10 new use cases)
- UC 21: ⏳ Async Data Loading with States
- UC 22: ⏳ Debounced Search
- UC 23: ⏳ Paginated Data Grid
- UC 24: ⏳ Form Dirty State Tracking
- UC 25: ⏳ Form with Auto-Save
- UC 26: ⏳ Grid with Multi-Select and Bulk Actions
- UC 27: ⏳ Global Notification System
- UC 28: ⏳ Search with URL State
- UC 29: ⏳ Live Dashboard with Server-Sent Events
- UC 30: ⏳ Text Editor with Undo/Redo

## Changes Required

1. **Delete stub file**:
   - Remove UseCase25View.java (incorrectly created earlier)

2. **Rename files to avoid conflicts**:
   - To avoid file conflicts during renaming, use temp names first
   - Current UC 18-21 → Temp names (TempMUC01-04)
   - Current UC 22-24 → New UC 18-20
   - Temp MUC 01-04 → New MUC01-04View.java
   - Update @Route (muc-01, muc-02, etc.), @PageTitle, @Menu for all renamed files

3. **Update signal-use-cases.md**:
   - Restructure with two main categories
   - Add 10 new use case descriptions (UC 21-30)
   - Update browser integration section (now UC 18-20)
   - Update collaboration section (now MUC 1-4)

4. **Implement new UC 21-30**:
   - Create 10 new view files
   - Add proper routes (use-case-21 through use-case-30)
   - Add menus and page titles
   - Implement with MissingAPI helpers as needed

5. **Update CollaborativeSignals and UserSessionRegistry** (if needed):
   - No changes needed, already application-scoped

6. **Test compilation**:
   - Ensure all files compile after renaming
   - Verify routes work correctly
