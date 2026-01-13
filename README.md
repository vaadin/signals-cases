# Vaadin Signal API Use Cases

This repository contains a comprehensive collection of UI use cases designed to validate and test the [Vaadin Signal API proposal](https://github.com/vaadin/platform/issues/8366).

## Overview

The Vaadin Signal API introduces reactive state management to Vaadin Flow applications. These use cases demonstrate how signals can simplify common UI patterns by automatically managing dependencies and eliminating boilerplate event listener code.

## Contents

- **`signal-use-cases.md`**: Main document containing **17 detailed use cases** organized by category
- **`GAPS.md`**: Analysis of API features from the proposal and coverage status

## Use Case Categories

1. **Basic Signal Bindings** (Use Cases 1-3)
   - One-way and two-way bindings
   - Simple state synchronization
   - Component enabled/disabled/visible states

2. **Computed Signals & Derivations** (Use Cases 4-5)
   - Multi-signal computations
   - Signal transformation pipelines
   - Complex business logic

3. **Conditional Rendering** (Use Cases 6-8)
   - Dynamic subforms
   - Progressive disclosure
   - Permission-based UI

4. **List & Collection Rendering** (Use Cases 9-10)
   - Filtered data grids
   - Dynamic lists with add/remove
   - Aggregate computations

5. **Cross-Component Interactions** (Use Cases 11-13)
   - Cascading dropdowns
   - Shopping cart with real-time calculations
   - Master-detail patterns

6. **Complex Forms** (Use Cases 14-15)
   - Multi-step wizards
   - Cross-field validation
   - Binder integration with signal-based validation

7. **Advanced Features** (Use Cases 16-17)
   - ComponentToggle for view switching
   - Reactive provider APIs for Grid/Select components

## How to Use These Use Cases

### For API Designers

1. **Validate API Completeness**: Check if the proposed signal API can handle all use cases elegantly
2. **Identify Gaps**: Look for patterns that are awkward or verbose with the current API
3. **Prioritize Features**: Use the feature coverage matrix to guide implementation priorities

### For Implementers

1. **Start Simple**: Begin with Use Cases 1-3 to understand basic signal patterns
2. **Build Incrementally**: Implement use cases in order of increasing complexity
3. **Test Edge Cases**: Pay special attention to lifecycle, memory management, and thread safety

### For Evaluators

1. **Compare Approaches**: For each use case, consider how it would be implemented:
   - Without signals (traditional event listeners)
   - With signals (reactive approach)
2. **Assess Readability**: Evaluate if signal-based code is more maintainable
3. **Consider Performance**: Analyze potential performance implications

## Key Learnings

### API Features by Usage Frequency

**Essential (70%+ usage):**
- `Signal.create()` - Creating signals (100%)
- `signal.map()` - Transforming signal values (94%)
- `bindValue()` - Two-way data binding (76%)
- `bindText()` - Dynamic text content (76%)
- `Signal.compute()` - Computed/derived signals (76%)
- `bindVisible()` - Conditional visibility (65%)

**Common (25-65% usage):**
- `bindEnabled()` - Dynamic enabled state (41%)
- `bindItems()` - List data binding (41%)
- `bindThemeName()` - Dynamic styling (29%)
- `bindComponentChildren()` - Dynamic component trees (29%)
- `signal.addListener()` - Side effects (24%)

**Specialized (<25% usage):**
- `bindClassName()` - CSS class management (18%)
- `bindHelperText()` - Field feedback (12%)
- `bindAttribute()` - HTML attribute binding (6%)
- `bindRequired()` - Dynamic required fields (6%)

**Advanced Features (single use case demonstrations):**
- **Binder Integration** - `getValidationStatus()`, `Binding::value()`
- **ComponentToggle** - `addExclusive()`, `addFallback()`
- **Reactive Providers** - `setCellEditableProvider()`, `setItemSelectableProvider()`, `setDropFilter()`, `setDynamicContentHandler()`

## Validation Checklist

Use this checklist to validate that the signal API implementation handles all critical scenarios:

- [ ] **Basic Bindings**
  - [ ] One-way bindings (text, visibility, enabled)
  - [ ] Two-way bindings (form fields)
  - [ ] Multiple components bound to same signal

- [ ] **Computed Signals**
  - [ ] Single-source transformations
  - [ ] Multi-source computations
  - [ ] Cascading computed signals

- [ ] **Collections**
  - [ ] Binding lists to grids/repeaters
  - [ ] Dynamic add/remove operations
  - [ ] Filtered/sorted derived lists

- [ ] **Lifecycle**
  - [ ] Automatic cleanup on component detach
  - [ ] No memory leaks with long-lived signals
  - [ ] Proper disposal of listeners

- [ ] **Performance**
  - [ ] Efficient dependency tracking
  - [ ] Batched UI updates
  - [ ] No unnecessary recomputations

- [ ] **Thread Safety**
  - [ ] Async signal updates with UI.access()
  - [ ] Concurrent signal modifications
  - [ ] Race condition handling

- [ ] **Edge Cases**
  - [ ] Null signal values
  - [ ] Empty collections
  - [ ] Circular dependencies (detection or prevention)

## Suggested Implementation Order

For teams implementing the signal API, we recommend this implementation order:

### Phase 1: Core Signal Infrastructure
1. `Signal.create()` - Writable signals
2. `signal.map()` - Signal transformations
3. `Signal.compute()` - Computed signals
4. Basic lifecycle management

### Phase 2: Essential Bindings
5. `bindValue()` - Two-way binding
6. `bindText()` - Text binding
7. `bindVisible()` - Visibility binding
8. `bindEnabled()` - Enabled state binding

### Phase 3: Advanced Bindings
9. `bindThemeName()` - Dynamic theming
10. `bindClassName()` - CSS classes
11. `bindHelperText()` - Field feedback
12. `bindAttribute()` - Generic attributes

### Phase 4: Collection Support
13. `bindItems()` - List binding
14. `bindComponentChildren()` - Dynamic components
15. Collection signal utilities

### Phase 5: Advanced Features
16. `signal.addListener()` - Side effects
17. Debouncing/throttling
18. Async signal helpers
19. Validation support

## Contributing

If you identify additional use cases or patterns that should be covered, please consider:

1. Ensuring the use case represents a real business requirement
2. Checking if it can be achieved by combining existing use cases
3. Documenting the specific signal patterns it would test
4. Including concrete code examples

## Related Resources

- [Vaadin Platform Issue #8366](https://github.com/vaadin/platform/issues/8366) - Original signal API proposal
- [Vaadin Flow Documentation](https://vaadin.com/docs/latest/flow)
- [Reactive Programming Patterns](https://reactivex.io/)

## Questions or Feedback?

For questions or feedback about these use cases:
- Open an issue on the Vaadin platform repository
- Discuss on the Vaadin Discord/Forum
- Contact the Vaadin development team

---

## Recent Updates

**Version 2.0** - Added 3 new use cases covering previously missing features:
- **Use Case 15**: Binder integration with `getValidationStatus()` and `Binding::value()`
- **Use Case 16**: ComponentToggle with `addExclusive()` and `addFallback()`
- **Use Case 17**: Reactive provider APIs (`setCellEditableProvider()`, `setItemSelectableProvider()`, `setDropFilter()`, `setDynamicContentHandler()`)

This brings total coverage to **17 use cases** addressing all major signal API features mentioned in the proposal.

---

**Version**: 2.0
**Last Updated**: 2026-01-13
**Status**: Complete coverage - ready for implementation validation
