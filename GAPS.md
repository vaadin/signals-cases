# Signal API Features Not Covered in Use Cases

This document lists features **explicitly mentioned** in [vaadin/platform#8366](https://github.com/vaadin/platform/issues/8366) that are **NOT** demonstrated in the current use case collection.

---

## 1. bindRequired()

**Issue Quote:**
> "CheckboxGroup::bindRequired(Signal<Boolean>)"

**Not Covered:** Dynamic required field status based on signals.

**Potential Use Case:**
```java
// Field becomes required based on another field's value
Checkbox isBusinessCheckbox = new Checkbox("Business Account");
isBusinessCheckbox.bindValue(isBusinessSignal);

TextField taxIdField = new TextField("Tax ID");
taxIdField.bindRequired(isBusinessSignal);  // NOT COVERED
```

---

## 2. Component-Specific Two-Way Bindings

**Issue Quote:**
> "Details::setOpened(boolean), AppLayout::setDrawerOpened(boolean), Checkbox::setIndeterminate(boolean), MenuItemBase::setChecked(boolean), Crud::setDirty(boolean)"

**Not Covered:**
- `Details::setOpened()` - Expandable/collapsible sections
- `AppLayout::setDrawerOpened()` - Navigation drawer state
- `Checkbox::setIndeterminate()` - Three-state checkboxes
- `MenuItemBase::setChecked()` - Menu item selection state
- `Crud::setDirty()` - CRUD component dirty state

**Potential Use Case:**
```java
WritableSignal<Boolean> detailsOpenSignal = Signal.create(false);

Details advancedSettings = new Details("Advanced Settings");
advancedSettings.setOpened(detailsOpenSignal);  // NOT COVERED

// Signal updates when user expands/collapses
// UI updates when signal changes programmatically
```

---

## 3. Binder Integration

**Issue Quotes:**
> "Binder should provide a readonly signal that contains the latest BinderValidationStatus state"

> "adding a signal-aware value getter to Binding"

**Not Covered:**
- `Binder::getValidationStatus()` returning `Signal<BinderValidationStatus<T>>`
- `Binding::value()` - Signal-aware value getter for cross-field validation
- Using `readBean()`, `writeBean()` within reactive effects

**Potential Use Case:**
```java
Binder<Person> binder = new Binder<>(Person.class);

// Get validation status as signal
Signal<BinderValidationStatus<Person>> validationSignal =
    binder.getValidationStatus();  // NOT COVERED

Button saveButton = new Button("Save");
saveButton.bindEnabled(validationSignal.map(status -> status.isOk()));

// Cross-field validation using Binding::value()
binder.forField(confirmPasswordField)
    .withValidator(confirm ->
        confirm.equals(passwordBinding.value()),  // NOT COVERED
        "Passwords must match"
    )
    .bind(Person::getPassword, Person::setPassword);
```

---

## 4. bindElementChildren()

**Issue Quote:**
> "HasComponents interface shall provide a void bindComponentChildren method along with a similar bindElementChildren method in Element"

**Not Covered:** Element-level (lower than component-level) child binding.

**What This Means:**
- We covered `bindComponentChildren()` for components
- But `bindElementChildren()` for direct DOM element manipulation is not demonstrated

**Potential Use Case:**
```java
Element customContainer = new Element("div");

Signal<List<Element>> elementsSignal = Signal.create(List.of());
customContainer.bindElementChildren(elementsSignal);  // NOT COVERED
```

---

## 5. Reactive Provider APIs for Complex Components

**Issue Quotes:**
> "Grid::setDropFilter(SerializablePredicate<T> dropFilter)"

> "GridContextMenu::setDynamicContentHandler(...)"

> "GridPro.EditColumn::setCellEditableProvider(...)"

> "Select::setItemEnabledProvider(...)"

**Not Covered:**
- `Grid::setItemSelectableProvider()` - Dynamic row selectability
- `Grid::setDropFilter()` - Dynamic drag-and-drop filtering
- `GridContextMenu::setDynamicContentHandler()` - Dynamic context menu content
- `GridPro.EditColumn::setCellEditableProvider()` - Dynamic cell editability
- `Select::setItemEnabledProvider()` - Dynamic item enabled state

**Potential Use Case:**
```java
Grid<Employee> grid = new Grid<>(Employee.class);

WritableSignal<Boolean> editModeSignal = Signal.create(false);

// Only certain cells editable when in edit mode
grid.getEditColumn("salary")
    .setCellEditableProvider(employee ->
        editModeSignal.value() &&  // NOT COVERED
        employee.canEditSalary()
    );

// Dynamic item selection based on permissions
grid.setItemSelectableProvider(employee ->  // NOT COVERED
    permissionsSignal.value().canSelect(employee)
);
```

---

## 6. ComponentToggle with Selective Rendering

**Issue Quote:**
> "ComponentToggle picks a single component out of lazy-creating component instances...toggle.addExclusive(BigComponent::new, width -> width > 1000)"

**Not Covered:**
- `ComponentToggle<T>` class
- `.addExclusive()` - Mutually exclusive component rendering
- `.addFallback()` - Default component when no conditions match

**What This Means:**
We used multiple `bindVisible()` calls to show/hide components, but the issue proposes a dedicated utility for cleaner mutually exclusive rendering.

**Potential Use Case:**
```java
WritableSignal<String> viewModeSignal = Signal.create("list");

ComponentToggle<String> viewToggle = new ComponentToggle<>(viewModeSignal);  // NOT COVERED

viewToggle.addExclusive("list", ListView::new);     // NOT COVERED
viewToggle.addExclusive("grid", GridView::new);     // NOT COVERED
viewToggle.addExclusive("chart", ChartView::new);   // NOT COVERED
viewToggle.addFallback(EmptyView::new);             // NOT COVERED

add(viewToggle);
// Only one view instantiated and visible at a time
```

---

## Summary

### High-Priority Missing Features:
1. **`bindRequired()`** - Common in form validation
2. **Binder Integration** - Critical for existing Vaadin applications
3. **ComponentToggle** - Cleaner alternative to multiple `bindVisible()`

### Component-Specific Features:
4. **Two-way bindings** for Details, AppLayout, Checkbox, Menu, Crud
5. **Reactive provider APIs** for Grid, Select, Menu components

### Low-Level Features:
6. **`bindElementChildren()`** - Element-level child binding

### Recommendation:
Add **3 additional use cases** to provide complete coverage:

**Use Case 15: Form with Binder and Signal Validation**
- Demonstrate `Binder::getValidationStatus()` returning signal
- Show `Binding::value()` for cross-field validation
- Integration between existing Binder API and new signal API

**Use Case 16: Responsive Dashboard with ComponentToggle**
- Use `ComponentToggle` for mutually exclusive views
- Show `.addExclusive()` and `.addFallback()` patterns
- Demonstrate lazy component instantiation

**Use Case 17: Advanced Grid with Dynamic Editability**
- Use `setCellEditableProvider()` with signals
- Show `setItemSelectableProvider()` for conditional selection
- Demonstrate complex grid interactions with signal-based providers

Would you like me to create these three additional use cases?
