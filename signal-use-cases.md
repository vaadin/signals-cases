# Vaadin Signal API Use Cases

## Introduction

This document presents a comprehensive collection of UI use cases designed to validate the Vaadin Signal API proposal ([vaadin/platform#8366](https://github.com/vaadin/platform/issues/8366)). Signals provide a reactive, declarative approach to building UIs where component state automatically synchronizes with application state.

**What are Signals?**

Signals are reactive state holders that automatically notify dependent computations and UI bindings when their values change. The framework manages the dependency graph and lifecycle automatically, eliminating manual listener management and cleanup code.

**Purpose of These Use Cases**

These use cases serve multiple purposes:
- **API Validation**: Test that the signal API handles common reactive patterns elegantly
- **Gap Analysis**: Identify missing features or awkward patterns in the API design
- **Documentation Examples**: Provide practical examples for developers
- **Implementation Guide**: Help prioritize features and guide implementation decisions

**Use Case Structure**

Each use case follows this template:
- **Business Scenario**: The user story or business requirement
- **UI Description**: What the interface looks like and how users interact with it
- **Signal Patterns**: Types of signals and bindings used
- **Code Example**: Key implementation code using the signal API
- **API Validation Points**: What aspects of the API this use case tests

---

## Category 1: Basic Signal Bindings

### Use Case 1: User Profile Settings Panel

**Business Scenario**

A user profile settings page where enabling "Advanced Mode" reveals additional configuration options. The visibility of advanced settings should be reactive to the checkbox state.

**UI Description**

- Checkbox: "Enable Advanced Mode"
- Advanced settings panel (initially hidden)
  - Text field: "Custom Theme Path"
  - Dropdown: "Log Level"
  - Checkbox: "Enable Debug Features"

**Signal Patterns**

- **One-way binding**: Checkbox state → Panel visibility
- **Simple boolean signal**: Controls visibility of dependent components

**Code Example**

```java
public class ProfileSettingsView extends VerticalLayout {

    public ProfileSettingsView() {
        // Create signal for advanced mode state
        WritableSignal<Boolean> advancedModeSignal = Signal.create(false);

        // Advanced mode checkbox
        Checkbox advancedModeCheckbox = new Checkbox("Enable Advanced Mode");
        advancedModeCheckbox.bindValue(advancedModeSignal);

        // Advanced settings panel
        VerticalLayout advancedPanel = new VerticalLayout();
        advancedPanel.add(
            new TextField("Custom Theme Path"),
            new Select<>("Log Level", LogLevel.values()),
            new Checkbox("Enable Debug Features")
        );

        // Bind panel visibility to the signal
        advancedPanel.bindVisible(advancedModeSignal);

        add(advancedModeCheckbox, advancedPanel);
    }
}
```

**API Validation Points**

- ✓ `WritableSignal<Boolean>` creation with `Signal.create()`
- ✓ Two-way binding with `bindValue()` on Checkbox
- ✓ One-way visibility binding with `bindVisible()`
- ✓ Automatic lifecycle management (no manual cleanup needed)

---

### Use Case 2: Form Field Synchronization

**Business Scenario**

A form where multiple read-only fields display the same computed value (e.g., a username generated from first and last name). All displays should update automatically when the source value changes.

**UI Description**

- Text field: "First Name"
- Text field: "Last Name"
- Read-only text fields showing generated username in multiple locations:
  - Header: "Welcome, [username]"
  - Preview section: "Your public profile: [username]"
  - Confirmation: "Account will be created for: [username]"

**Signal Patterns**

- **Two-way bindings**: Input fields → signals
- **Computed signal**: Derived from multiple source signals
- **Multiple one-way bindings**: Computed signal → multiple text displays

**Code Example**

```java
public class UserRegistrationForm extends FormLayout {

    public UserRegistrationForm() {
        // Create signals for input fields
        WritableSignal<String> firstNameSignal = Signal.create("");
        WritableSignal<String> lastNameSignal = Signal.create("");

        // Input fields with two-way binding
        TextField firstNameField = new TextField("First Name");
        firstNameField.bindValue(firstNameSignal);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.bindValue(lastNameSignal);

        // Computed signal for username
        Signal<String> usernameSignal = Signal.compute(() -> {
            String first = firstNameSignal.getValue();
            String last = lastNameSignal.getValue();
            if (first.isEmpty() || last.isEmpty()) {
                return "";
            }
            return (first + "." + last).toLowerCase();
        });

        // Multiple displays bound to the same computed signal
        H3 welcomeHeader = new H3();
        welcomeHeader.bindText(usernameSignal.map(u -> "Welcome, " + u));

        Span profilePreview = new Span();
        profilePreview.bindText(usernameSignal.map(u -> "Your public profile: " + u));

        Span confirmation = new Span();
        confirmation.bindText(usernameSignal.map(u -> "Account will be created for: " + u));

        add(firstNameField, lastNameField,
            welcomeHeader, profilePreview, confirmation);
    }
}
```

**API Validation Points**

- ✓ Multiple `WritableSignal` instances for independent input fields
- ✓ `Signal.compute()` for derived state from multiple signals
- ✓ `signal.map()` for transforming signal values
- ✓ Multiple components binding to the same computed signal
- ✓ Automatic dependency tracking and update propagation

---

### Use Case 3: Dynamic Button State

**Business Scenario**

A form submission button that should only be enabled when all required fields are filled and valid. The button text should also change based on the form state (e.g., "Save" vs "Saving..." during submission).

**UI Description**

- Form fields: Email, Password, Confirm Password
- Submit button:
  - Disabled when form is invalid
  - Text changes: "Create Account" → "Creating..." → "Success!"
  - Different theme variants based on state

**Signal Patterns**

- **Computed boolean signal**: Form validity from multiple field signals
- **State signal**: Tracks submission state (idle, submitting, success, error)
- **Multiple bindings on one component**: enabled state, text, theme

**Code Example**

```java
public class RegistrationFormView extends VerticalLayout {

    enum SubmissionState { IDLE, SUBMITTING, SUCCESS, ERROR }

    public RegistrationFormView() {
        // Field signals
        WritableSignal<String> emailSignal = Signal.create("");
        WritableSignal<String> passwordSignal = Signal.create("");
        WritableSignal<String> confirmPasswordSignal = Signal.create("");
        WritableSignal<SubmissionState> submissionStateSignal =
            Signal.create(SubmissionState.IDLE);

        // Computed validity signal
        Signal<Boolean> isValidSignal = Signal.compute(() -> {
            String email = emailSignal.getValue();
            String password = passwordSignal.getValue();
            String confirm = confirmPasswordSignal.getValue();

            return email.contains("@")
                && password.length() >= 8
                && password.equals(confirm);
        });

        // Form fields
        EmailField emailField = new EmailField("Email");
        emailField.bindValue(emailSignal);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.bindValue(passwordSignal);

        PasswordField confirmField = new PasswordField("Confirm Password");
        confirmField.bindValue(confirmPasswordSignal);

        // Submit button with multiple signal bindings
        Button submitButton = new Button();

        // Bind enabled state: enabled when valid AND not submitting
        submitButton.bindEnabled(Signal.compute(() ->
            isValidSignal.getValue()
            && submissionStateSignal.getValue() == SubmissionState.IDLE
        ));

        // Bind button text based on submission state
        submitButton.bindText(submissionStateSignal.map(state -> switch(state) {
            case IDLE -> "Create Account";
            case SUBMITTING -> "Creating...";
            case SUCCESS -> "Success!";
            case ERROR -> "Retry";
        }));

        // Bind theme variant
        submitButton.bindThemeName(submissionStateSignal.map(state ->
            state == SubmissionState.SUCCESS ? "success" : "primary"
        ));

        submitButton.addClickListener(e -> {
            submissionStateSignal.setValue(SubmissionState.SUBMITTING);
            // Simulate async submission
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.setValue(SubmissionState.SUCCESS)
                    ));
                } catch (Exception ex) {
                    getUI().ifPresent(ui -> ui.access(() ->
                        submissionStateSignal.setValue(SubmissionState.ERROR)
                    ));
                }
            });
        });

        add(emailField, passwordField, confirmField, submitButton);
    }
}
```

**API Validation Points**

- ✓ Complex computed signals with multiple dependencies
- ✓ Multiple bindings on a single component (`bindEnabled`, `bindText`, `bindThemeName`)
- ✓ `signal.map()` with complex transformations (switch expressions)
- ✓ Integration with async operations
- ✓ Signal updates from different threads (with UI.access())

---

## Category 2: Computed Signals & Derivations

### Use Case 4: E-commerce Product Configurator

**Business Scenario**

A product configuration page where users select options (size, color, quantity) and the page displays the real-time price, availability status, and product image. Changes to any option should immediately update all dependent displays.

**UI Description**

- Dropdown: "Size" (S, M, L, XL)
- Dropdown: "Color" (Red, Blue, Green)
- Number field: "Quantity"
- Display elements:
  - Product image (changes with color/size)
  - Price display (base price × quantity, with size adjustments)
  - Availability badge (In Stock / Limited / Out of Stock)
  - Estimated delivery date

**Signal Patterns**

- **Multiple source signals**: size, color, quantity
- **Multiple computed signals**: price, availability, image URL, delivery date
- **Signal composition**: Some computations depend on other computed signals
- **Data lookups**: Signals that fetch data based on combinations

**Code Example**

```java
public class ProductConfiguratorView extends VerticalLayout {

    record ProductVariant(String size, String color,
                         BigDecimal basePrice, int stockLevel) {}

    private final Map<String, ProductVariant> inventory = loadInventory();

    public ProductConfiguratorView() {
        // Source signals
        WritableSignal<String> sizeSignal = Signal.create("M");
        WritableSignal<String> colorSignal = Signal.create("Blue");
        WritableSignal<Integer> quantitySignal = Signal.create(1);

        // Computed: Current product variant
        Signal<ProductVariant> variantSignal = Signal.compute(() -> {
            String key = sizeSignal.getValue() + "-" + colorSignal.getValue();
            return inventory.getOrDefault(key,
                new ProductVariant("M", "Blue", BigDecimal.ZERO, 0));
        });

        // Computed: Total price (depends on variant and quantity)
        Signal<BigDecimal> totalPriceSignal = Signal.compute(() -> {
            BigDecimal basePrice = variantSignal.getValue().basePrice();
            int quantity = quantitySignal.getValue();
            return basePrice.multiply(BigDecimal.valueOf(quantity));
        });

        // Computed: Availability status
        Signal<String> availabilitySignal = Signal.compute(() -> {
            int stock = variantSignal.getValue().stockLevel();
            int requested = quantitySignal.getValue();

            if (stock == 0) return "Out of Stock";
            if (stock < requested) return "Limited Stock (" + stock + " available)";
            if (stock < 10) return "Low Stock";
            return "In Stock";
        });

        // Computed: Product image URL
        Signal<String> imageUrlSignal = Signal.compute(() -> {
            String size = sizeSignal.getValue();
            String color = colorSignal.getValue();
            return "/images/products/shirt-" +
                   color.toLowerCase() + "-" + size + ".jpg";
        });

        // Computed: Estimated delivery
        Signal<String> deliverySignal = Signal.compute(() -> {
            int stock = variantSignal.getValue().stockLevel();
            int requested = quantitySignal.getValue();
            LocalDate estimatedDate = stock >= requested
                ? LocalDate.now().plusDays(3)
                : LocalDate.now().plusDays(14);
            return "Estimated delivery: " + estimatedDate.format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        });

        // UI Components
        Select<String> sizeSelect = new Select<>("Size", "S", "M", "L", "XL");
        sizeSelect.bindValue(sizeSignal);

        Select<String> colorSelect = new Select<>("Color", "Red", "Blue", "Green");
        colorSelect.bindValue(colorSignal);

        IntegerField quantityField = new IntegerField("Quantity");
        quantityField.setValue(1);
        quantityField.setMin(1);
        quantityField.bindValue(quantitySignal);

        // Product image
        Image productImage = new Image();
        productImage.bindAttribute("src", imageUrlSignal);
        productImage.setWidth("300px");

        // Price display
        H2 priceDisplay = new H2();
        priceDisplay.bindText(totalPriceSignal.map(price ->
            "$" + price.setScale(2, RoundingMode.HALF_UP)));

        // Availability badge
        Span availabilityBadge = new Span();
        availabilityBadge.bindText(availabilitySignal);
        availabilityBadge.bindThemeName(availabilitySignal.map(status -> {
            if (status.contains("Out of Stock")) return "badge error";
            if (status.contains("Limited") || status.contains("Low")) return "badge contrast";
            return "badge success";
        }));

        // Delivery estimate
        Span deliveryInfo = new Span();
        deliveryInfo.bindText(deliverySignal);

        // Add to cart button - disabled when out of stock
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.bindEnabled(availabilitySignal.map(
            status -> !status.contains("Out of Stock")
        ));

        add(
            new HorizontalLayout(sizeSelect, colorSelect, quantityField),
            productImage,
            priceDisplay,
            availabilityBadge,
            deliveryInfo,
            addToCartButton
        );
    }

    private Map<String, ProductVariant> loadInventory() {
        // Mock data
        return Map.of(
            "M-Blue", new ProductVariant("M", "Blue", new BigDecimal("29.99"), 50),
            "L-Blue", new ProductVariant("L", "Blue", new BigDecimal("31.99"), 30),
            "M-Red", new ProductVariant("M", "Red", new BigDecimal("29.99"), 5),
            "L-Red", new ProductVariant("L", "Red", new BigDecimal("31.99"), 0)
            // ... more variants
        );
    }
}
```

**API Validation Points**

- ✓ Multiple independent source signals
- ✓ Computed signals deriving from multiple sources
- ✓ Cascading computations (signals depending on other computed signals)
- ✓ Complex transformations with `signal.map()`
- ✓ `bindAttribute()` for dynamic HTML attributes
- ✓ Conditional logic in computed signals
- ✓ Performance: Multiple components reacting to shared signals

---

### Use Case 5: Dynamic Pricing Calculator

**Business Scenario**

A pricing calculator for a service where the final price depends on multiple factors: base service, add-ons, quantity, discount code, and tax rate. All price components should be displayed in real-time as users adjust inputs.

**UI Description**

- Dropdown: "Service Plan" (Basic, Professional, Enterprise)
- Checkboxes: Add-ons (Support, Training, Custom Integration)
- Number field: "Number of Users"
- Text field: "Discount Code"
- Checkbox: "Tax-Exempt Organization"
- Price breakdown display:
  - Base price
  - Add-ons total
  - Subtotal
  - Discount amount
  - Tax
  - **Final total**

**Signal Patterns**

- **Multiple input signals**: plan, add-ons, users, discount, tax-exempt
- **Computation chain**: base → add-ons → subtotal → discount → tax → total
- **Conditional logic**: Tax and discount calculations depend on multiple factors
- **Real-time validation**: Invalid discount codes

**Code Example**

```java
public class PricingCalculatorView extends VerticalLayout {

    enum Plan { BASIC(10), PROFESSIONAL(25), ENTERPRISE(50);
        final int pricePerUser;
        Plan(int pricePerUser) { this.pricePerUser = pricePerUser; }
    }

    record AddOn(String name, int price) {}

    private final Map<String, Double> discountCodes = Map.of(
        "SAVE10", 0.10,
        "SAVE20", 0.20,
        "WELCOME", 0.15
    );

    public PricingCalculatorView() {
        // Input signals
        WritableSignal<Plan> planSignal = Signal.create(Plan.BASIC);
        WritableSignal<Set<AddOn>> addOnsSignal = Signal.create(new HashSet<>());
        WritableSignal<Integer> usersSignal = Signal.create(1);
        WritableSignal<String> discountCodeSignal = Signal.create("");
        WritableSignal<Boolean> taxExemptSignal = Signal.create(false);

        // Computed: Base price
        Signal<BigDecimal> basePriceSignal = Signal.compute(() -> {
            int pricePerUser = planSignal.getValue().pricePerUser;
            int users = usersSignal.getValue();
            return BigDecimal.valueOf(pricePerUser * users);
        });

        // Computed: Add-ons total
        Signal<BigDecimal> addOnsTotalSignal = Signal.compute(() -> {
            int users = usersSignal.getValue();
            return addOnsSignal.getValue().stream()
                .map(addOn -> BigDecimal.valueOf(addOn.price() * users))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        });

        // Computed: Subtotal
        Signal<BigDecimal> subtotalSignal = Signal.compute(() ->
            basePriceSignal.getValue().add(addOnsTotalSignal.getValue())
        );

        // Computed: Discount validity and amount
        Signal<Boolean> discountValidSignal = Signal.compute(() ->
            discountCodes.containsKey(discountCodeSignal.getValue().toUpperCase())
        );

        Signal<BigDecimal> discountAmountSignal = Signal.compute(() -> {
            if (!discountValidSignal.getValue()) {
                return BigDecimal.ZERO;
            }
            String code = discountCodeSignal.getValue().toUpperCase();
            double rate = discountCodes.get(code);
            return subtotalSignal.getValue().multiply(BigDecimal.valueOf(rate));
        });

        // Computed: After discount
        Signal<BigDecimal> afterDiscountSignal = Signal.compute(() ->
            subtotalSignal.getValue().subtract(discountAmountSignal.getValue())
        );

        // Computed: Tax
        Signal<BigDecimal> taxSignal = Signal.compute(() -> {
            if (taxExemptSignal.getValue()) {
                return BigDecimal.ZERO;
            }
            return afterDiscountSignal.getValue().multiply(BigDecimal.valueOf(0.08));
        });

        // Computed: Final total
        Signal<BigDecimal> totalSignal = Signal.compute(() ->
            afterDiscountSignal.getValue().add(taxSignal.getValue())
        );

        // UI Components
        Select<Plan> planSelect = new Select<>("Service Plan", Plan.values());
        planSelect.bindValue(planSignal);

        CheckboxGroup<AddOn> addOnsGroup = new CheckboxGroup<>("Add-ons");
        addOnsGroup.setItems(
            new AddOn("24/7 Support", 5),
            new AddOn("Training", 10),
            new AddOn("Custom Integration", 20)
        );
        addOnsGroup.addValueChangeListener(e ->
            addOnsSignal.setValue(e.getValue())
        );

        IntegerField usersField = new IntegerField("Number of Users");
        usersField.setValue(1);
        usersField.setMin(1);
        usersField.bindValue(usersSignal);

        TextField discountCodeField = new TextField("Discount Code");
        discountCodeField.bindValue(discountCodeSignal);
        // Show validation feedback
        discountCodeField.bindHelperText(Signal.compute(() -> {
            String code = discountCodeSignal.getValue();
            if (code.isEmpty()) return "";
            return discountValidSignal.getValue()
                ? "✓ Valid code applied"
                : "✗ Invalid discount code";
        }));

        Checkbox taxExemptCheckbox = new Checkbox("Tax-Exempt Organization");
        taxExemptCheckbox.bindValue(taxExemptSignal);

        // Price breakdown display
        VerticalLayout priceBreakdown = new VerticalLayout();
        priceBreakdown.addClassName("price-breakdown");

        Span basePrice = new Span();
        basePrice.bindText(basePriceSignal.map(p ->
            "Base: $" + p.setScale(2, RoundingMode.HALF_UP)));

        Span addOnsTotal = new Span();
        addOnsTotal.bindText(addOnsTotalSignal.map(p ->
            "Add-ons: $" + p.setScale(2, RoundingMode.HALF_UP)));
        addOnsTotal.bindVisible(addOnsTotalSignal.map(p ->
            p.compareTo(BigDecimal.ZERO) > 0));

        Span subtotal = new Span();
        subtotal.bindText(subtotalSignal.map(p ->
            "Subtotal: $" + p.setScale(2, RoundingMode.HALF_UP)));

        Span discount = new Span();
        discount.bindText(discountAmountSignal.map(p ->
            "Discount: -$" + p.setScale(2, RoundingMode.HALF_UP)));
        discount.bindVisible(discountAmountSignal.map(p ->
            p.compareTo(BigDecimal.ZERO) > 0));
        discount.addClassName("discount-amount");

        Span tax = new Span();
        tax.bindText(taxSignal.map(p ->
            "Tax (8%): $" + p.setScale(2, RoundingMode.HALF_UP)));
        tax.bindVisible(taxSignal.map(p ->
            p.compareTo(BigDecimal.ZERO) > 0));

        H2 total = new H2();
        total.bindText(totalSignal.map(p ->
            "Total: $" + p.setScale(2, RoundingMode.HALF_UP)));
        total.addClassName("total-price");

        priceBreakdown.add(basePrice, addOnsTotal, subtotal, discount, tax,
                          new Hr(), total);

        add(
            new H3("Configure Your Plan"),
            planSelect,
            addOnsGroup,
            usersField,
            discountCodeField,
            taxExemptCheckbox,
            priceBreakdown
        );
    }
}
```

**API Validation Points**

- ✓ Complex computation chains with dependent signals
- ✓ Conditional computations (tax exempt, discount validation)
- ✓ `bindHelperText()` for dynamic field feedback
- ✓ Combining `bindText()` and `bindVisible()` on same components
- ✓ Signal performance with multiple rapid calculations
- ✓ Working with complex data types (Set, BigDecimal, enums)

---

## Category 3: Conditional Rendering

### Use Case 6: Dynamic Form with Conditional Subforms

**Business Scenario**

An insurance quote form where different types of insurance (Auto, Home, Life) require completely different sets of fields. When the user selects an insurance type from a dropdown, the form should show only the relevant fields for that type.

**UI Description**

- Dropdown: "Insurance Type" (Auto, Home, Life)
- **Auto Insurance subform** (shown when Auto selected):
  - Vehicle make, model, year
  - Annual mileage
  - Primary use (commute, business, pleasure)
- **Home Insurance subform** (shown when Home selected):
  - Property type (house, condo, apartment)
  - Year built
  - Square footage
  - Security system installed?
- **Life Insurance subform** (shown when Life selected):
  - Coverage amount
  - Term length
  - Beneficiary information
  - Medical history checkbox

**Signal Patterns**

- **Discriminated union pattern**: One signal determines which entire subtree to render
- **Multiple conditional visibility bindings**: Each subform bound to computed signal
- **Isolated state**: Each subform has its own signals that are only relevant when visible

**Code Example**

```java
public class InsuranceQuoteForm extends VerticalLayout {

    enum InsuranceType { AUTO, HOME, LIFE }

    public InsuranceQuoteForm() {
        // Main type selector
        WritableSignal<InsuranceType> insuranceTypeSignal =
            Signal.create(InsuranceType.AUTO);

        Select<InsuranceType> typeSelect = new Select<>("Insurance Type",
            InsuranceType.values());
        typeSelect.bindValue(insuranceTypeSignal);

        // Auto insurance subform
        FormLayout autoForm = createAutoInsuranceForm();
        autoForm.bindVisible(insuranceTypeSignal.map(
            type -> type == InsuranceType.AUTO
        ));

        // Home insurance subform
        FormLayout homeForm = createHomeInsuranceForm();
        homeForm.bindVisible(insuranceTypeSignal.map(
            type -> type == InsuranceType.HOME
        ));

        // Life insurance subform
        FormLayout lifeForm = createLifeInsuranceForm();
        lifeForm.bindVisible(insuranceTypeSignal.map(
            type -> type == InsuranceType.LIFE
        ));

        add(typeSelect, autoForm, homeForm, lifeForm);
    }

    private FormLayout createAutoInsuranceForm() {
        FormLayout form = new FormLayout();

        WritableSignal<String> makeSignal = Signal.create("");
        WritableSignal<String> modelSignal = Signal.create("");
        WritableSignal<Integer> yearSignal = Signal.create(2024);

        TextField makeField = new TextField("Vehicle Make");
        makeField.bindValue(makeSignal);

        TextField modelField = new TextField("Vehicle Model");
        modelField.bindValue(modelSignal);

        IntegerField yearField = new IntegerField("Year");
        yearField.bindValue(yearSignal);

        IntegerField mileageField = new IntegerField("Annual Mileage");
        Select<String> useSelect = new Select<>("Primary Use",
            "Commute", "Business", "Pleasure");

        form.add(makeField, modelField, yearField, mileageField, useSelect);
        return form;
    }

    private FormLayout createHomeInsuranceForm() {
        FormLayout form = new FormLayout();

        Select<String> propertyType = new Select<>("Property Type",
            "House", "Condo", "Apartment");
        IntegerField yearBuilt = new IntegerField("Year Built");
        IntegerField squareFootage = new IntegerField("Square Footage");
        Checkbox securitySystem = new Checkbox("Security System Installed");

        form.add(propertyType, yearBuilt, squareFootage, securitySystem);
        return form;
    }

    private FormLayout createLifeInsuranceForm() {
        FormLayout form = new FormLayout();

        IntegerField coverageAmount = new IntegerField("Coverage Amount ($)");
        Select<String> termLength = new Select<>("Term Length",
            "10 years", "20 years", "30 years", "Whole life");
        TextField beneficiary = new TextField("Beneficiary Name");
        Checkbox medicalHistory = new Checkbox(
            "I have pre-existing medical conditions");

        form.add(coverageAmount, termLength, beneficiary, medicalHistory);
        return form;
    }
}
```

**API Validation Points**

- ✓ Conditional rendering based on enum/discriminated union
- ✓ Multiple mutually exclusive components bound to same signal
- ✓ Component lifecycle: hidden components don't execute unnecessary updates
- ✓ State isolation: subform signals only active when visible
- ✓ Clean separation of concerns: each subform self-contained

---

### Use Case 7: Progressive Disclosure with Nested Conditions

**Business Scenario**

A job application form where additional questions appear based on previous answers, with multiple levels of nesting. For example, if applicant says they need visa sponsorship, show visa-related fields, and if they select H1-B visa type, show H1-B-specific fields.

**UI Description**

- Checkbox: "I require visa sponsorship"
  - → If YES, show:
    - Dropdown: "Visa Type" (H1-B, L-1, O-1, Other)
      - → If "H1-B", show:
        - "Have you been H1-B cap-exempt before?"
        - "Current H1-B status" dropdown
      - → If "Other", show:
        - Text field: "Please specify visa type"
- Checkbox: "I have employment gaps in the last 5 years"
  - → If YES, show:
    - Text area: "Please explain employment gaps"

**Signal Patterns**

- **Nested conditional visibility**: Visibility depends on multiple ancestor conditions
- **Cascading state reset**: When parent condition becomes false, child fields should reset
- **Complex boolean logic**: Multiple conditions combined with AND/OR

**Code Example**

```java
public class JobApplicationForm extends VerticalLayout {

    enum VisaType { H1B, L1, O1, OTHER }

    public JobApplicationForm() {
        // Visa sponsorship signals
        WritableSignal<Boolean> needsVisaSponsorshipSignal = Signal.create(false);
        WritableSignal<VisaType> visaTypeSignal = Signal.create(VisaType.H1B);
        WritableSignal<Boolean> h1bCapExemptSignal = Signal.create(false);

        // Employment gap signals
        WritableSignal<Boolean> hasEmploymentGapsSignal = Signal.create(false);
        WritableSignal<String> gapExplanationSignal = Signal.create("");

        // Level 1: Visa sponsorship checkbox
        Checkbox visaSponsorshipCheckbox = new Checkbox(
            "I require visa sponsorship");
        visaSponsorshipCheckbox.bindValue(needsVisaSponsorshipSignal);

        // Level 2: Visa type selector (shown if needs sponsorship)
        VerticalLayout visaSection = new VerticalLayout();
        visaSection.bindVisible(needsVisaSponsorshipSignal);

        Select<VisaType> visaTypeSelect = new Select<>("Visa Type",
            VisaType.values());
        visaTypeSelect.bindValue(visaTypeSignal);

        // Level 3a: H1-B specific fields (shown if needs sponsorship AND H1-B selected)
        VerticalLayout h1bSection = new VerticalLayout();
        h1bSection.bindVisible(Signal.compute(() ->
            needsVisaSponsorshipSignal.getValue()
            && visaTypeSignal.getValue() == VisaType.H1B
        ));

        Checkbox h1bCapExemptCheckbox = new Checkbox(
            "Have you been H1-B cap-exempt before?");
        h1bCapExemptCheckbox.bindValue(h1bCapExemptSignal);

        Select<String> h1bStatusSelect = new Select<>("Current H1-B Status",
            "First time applicant",
            "Currently on H1-B",
            "Previously had H1-B",
            "H1-B transfer");

        h1bSection.add(h1bCapExemptCheckbox, h1bStatusSelect);

        // Level 3b: Other visa type field (shown if needs sponsorship AND Other selected)
        TextField otherVisaField = new TextField("Please specify visa type");
        otherVisaField.bindVisible(Signal.compute(() ->
            needsVisaSponsorshipSignal.getValue()
            && visaTypeSignal.getValue() == VisaType.OTHER
        ));

        visaSection.add(visaTypeSelect, h1bSection, otherVisaField);

        // Employment gaps section (independent condition)
        Checkbox employmentGapsCheckbox = new Checkbox(
            "I have employment gaps in the last 5 years");
        employmentGapsCheckbox.bindValue(hasEmploymentGapsSignal);

        TextArea gapExplanationArea = new TextArea(
            "Please explain employment gaps");
        gapExplanationArea.bindVisible(hasEmploymentGapsSignal);
        gapExplanationArea.bindValue(gapExplanationSignal);

        // Reset child state when parent conditions change
        needsVisaSponsorshipSignal.addListener((oldVal, newVal) -> {
            if (!newVal) {
                // Reset visa-related fields when unchecked
                visaTypeSignal.setValue(VisaType.H1B);
                h1bCapExemptSignal.setValue(false);
            }
        });

        add(
            visaSponsorshipCheckbox,
            visaSection,
            new Hr(),
            employmentGapsCheckbox,
            gapExplanationArea
        );
    }
}
```

**API Validation Points**

- ✓ Nested conditional visibility with `Signal.compute()` combining multiple signals
- ✓ Boolean logic in computed signals (AND conditions)
- ✓ State management: resetting child state when parent condition changes
- ✓ `signal.addListener()` for side effects beyond UI binding
- ✓ Multiple independent conditional branches in same form
- ✓ Performance: deeply nested conditions with minimal re-computation

---

### Use Case 8: Permission-Based Component Visibility

**Business Scenario**

An admin dashboard where UI elements (buttons, sections, menu items) are shown or hidden based on the current user's role and permissions. Permissions can change dynamically (e.g., when switching between user accounts or when permissions are updated).

**UI Description**

- User role indicator at top
- Admin-only section: "User Management"
  - View users button (visible to Admin, Manager)
  - Delete users button (visible to Admin only)
  - Export data button (visible to Admin, Auditor)
- Manager-only section: "Team Reports"
- Regular user section: "My Profile" (visible to all)

**Signal Patterns**

- **Security-driven visibility**: UI structure determined by permissions
- **Multiple permission checks**: Different components check different permissions
- **Dynamic permission updates**: Permissions can change at runtime
- **Derived permission signals**: Complex permission logic (role + feature flags)

**Code Example**

```java
public class AdminDashboardView extends VerticalLayout {

    enum Role { ADMIN, MANAGER, AUDITOR, USER }

    record UserPermissions(
        Role role,
        Set<String> featureFlags
    ) {
        boolean canManageUsers() {
            return role == Role.ADMIN || role == Role.MANAGER;
        }

        boolean canDeleteUsers() {
            return role == Role.ADMIN;
        }

        boolean canExportData() {
            return (role == Role.ADMIN || role == Role.AUDITOR)
                && featureFlags.contains("data_export");
        }

        boolean canViewReports() {
            return role == Role.ADMIN || role == Role.MANAGER;
        }
    }

    public AdminDashboardView() {
        // Current user permissions signal (can be updated from elsewhere)
        WritableSignal<UserPermissions> permissionsSignal = Signal.create(
            new UserPermissions(Role.USER, Set.of())
        );

        // Derived permission signals
        Signal<Boolean> canManageUsersSignal = permissionsSignal.map(
            UserPermissions::canManageUsers
        );
        Signal<Boolean> canDeleteUsersSignal = permissionsSignal.map(
            UserPermissions::canDeleteUsers
        );
        Signal<Boolean> canExportDataSignal = permissionsSignal.map(
            UserPermissions::canExportData
        );
        Signal<Boolean> canViewReportsSignal = permissionsSignal.map(
            UserPermissions::canViewReports
        );

        // Role indicator
        Span roleIndicator = new Span();
        roleIndicator.bindText(permissionsSignal.map(p ->
            "Current Role: " + p.role().name()
        ));
        roleIndicator.addClassName("role-badge");

        // User Management section
        VerticalLayout userManagementSection = new VerticalLayout();
        userManagementSection.bindVisible(canManageUsersSignal);

        H3 userMgmtHeader = new H3("User Management");

        Button viewUsersButton = new Button("View Users");
        viewUsersButton.bindVisible(canManageUsersSignal);

        Button deleteUsersButton = new Button("Delete Users");
        deleteUsersButton.bindVisible(canDeleteUsersSignal);
        deleteUsersButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button exportDataButton = new Button("Export Data");
        exportDataButton.bindVisible(canExportDataSignal);

        userManagementSection.add(userMgmtHeader, viewUsersButton,
            deleteUsersButton, exportDataButton);

        // Team Reports section
        VerticalLayout reportsSection = new VerticalLayout();
        reportsSection.bindVisible(canViewReportsSignal);

        H3 reportsHeader = new H3("Team Reports");
        Button viewReportsButton = new Button("View Reports");

        reportsSection.add(reportsHeader, viewReportsButton);

        // My Profile section (always visible)
        VerticalLayout profileSection = new VerticalLayout();
        H3 profileHeader = new H3("My Profile");
        Button editProfileButton = new Button("Edit Profile");
        profileSection.add(profileHeader, editProfileButton);

        // Simulate role switching for demo
        Select<Role> roleSwitch = new Select<>("Switch Role (Demo)",
            Role.values());
        roleSwitch.addValueChangeListener(e -> {
            Role newRole = e.getValue();
            permissionsSignal.setValue(new UserPermissions(
                newRole,
                newRole == Role.ADMIN ? Set.of("data_export") : Set.of()
            ));
        });

        add(
            roleIndicator,
            roleSwitch,
            new Hr(),
            userManagementSection,
            reportsSection,
            profileSection
        );
    }
}
```

**API Validation Points**

- ✓ Security-driven UI with permission-based visibility
- ✓ Multiple derived signals from single permission source
- ✓ Complex permission logic encapsulated in domain objects
- ✓ Runtime permission updates affecting multiple components
- ✓ Combination of always-visible and conditionally-visible sections
- ✓ Signal composition for authorization patterns

---

## Category 4: List & Collection Rendering

### Use Case 9: Filtered and Sorted Data Grid

**Business Scenario**

A product inventory dashboard with a data grid showing products. Users can filter by category, search by name, and toggle between showing all products or only those in stock. The grid updates reactively as filters change.

**UI Description**

- Search field: "Search products..."
- Dropdown: "Category" (All, Electronics, Clothing, Food)
- Checkbox: "Show in-stock only"
- Data grid displaying filtered products
- Count indicator: "Showing X of Y products"

**Signal Patterns**

- **Collection signal**: List of products
- **Multiple filter signals**: search term, category, in-stock filter
- **Computed filtered list**: Combines all filters
- **Reactive list rendering**: Grid updates when filtered list changes

**Code Example**

```java
public class ProductInventoryView extends VerticalLayout {

    record Product(
        String id,
        String name,
        String category,
        int stockLevel,
        BigDecimal price
    ) {}

    public ProductInventoryView() {
        // Source data signal
        WritableSignal<List<Product>> allProductsSignal = Signal.create(
            loadProducts()
        );

        // Filter signals
        WritableSignal<String> searchTermSignal = Signal.create("");
        WritableSignal<String> categorySignal = Signal.create("All");
        WritableSignal<Boolean> inStockOnlySignal = Signal.create(false);

        // Computed: Filtered products
        Signal<List<Product>> filteredProductsSignal = Signal.compute(() -> {
            List<Product> products = allProductsSignal.getValue();
            String searchTerm = searchTermSignal.getValue().toLowerCase();
            String category = categorySignal.getValue();
            boolean inStockOnly = inStockOnlySignal.getValue();

            return products.stream()
                .filter(p -> {
                    // Name filter
                    if (!searchTerm.isEmpty() &&
                        !p.name().toLowerCase().contains(searchTerm)) {
                        return false;
                    }
                    // Category filter
                    if (!"All".equals(category) &&
                        !p.category().equals(category)) {
                        return false;
                    }
                    // Stock filter
                    if (inStockOnly && p.stockLevel() == 0) {
                        return false;
                    }
                    return true;
                })
                .toList();
        });

        // Computed: Result count text
        Signal<String> countTextSignal = Signal.compute(() -> {
            int filtered = filteredProductsSignal.getValue().size();
            int total = allProductsSignal.getValue().size();
            return String.format("Showing %d of %d products", filtered, total);
        });

        // Filter UI
        TextField searchField = new TextField();
        searchField.setPlaceholder("Search products...");
        searchField.bindValue(searchTermSignal);
        searchField.setClearButtonVisible(true);

        Select<String> categorySelect = new Select<>("Category",
            "All", "Electronics", "Clothing", "Food");
        categorySelect.bindValue(categorySignal);

        Checkbox inStockCheckbox = new Checkbox("Show in-stock only");
        inStockCheckbox.bindValue(inStockOnlySignal);

        HorizontalLayout filters = new HorizontalLayout(
            searchField, categorySelect, inStockCheckbox
        );

        // Result count
        Span countIndicator = new Span();
        countIndicator.bindText(countTextSignal);
        countIndicator.addClassName("result-count");

        // Data grid
        Grid<Product> productGrid = new Grid<>(Product.class, false);
        productGrid.addColumn(Product::name).setHeader("Name");
        productGrid.addColumn(Product::category).setHeader("Category");
        productGrid.addColumn(Product::stockLevel).setHeader("Stock");
        productGrid.addColumn(p -> "$" + p.price()).setHeader("Price");

        // Bind grid items to filtered list signal
        productGrid.bindItems(filteredProductsSignal);

        add(filters, countIndicator, productGrid);
    }

    private List<Product> loadProducts() {
        return List.of(
            new Product("1", "Laptop", "Electronics", 15,
                new BigDecimal("999.99")),
            new Product("2", "T-Shirt", "Clothing", 0,
                new BigDecimal("19.99")),
            new Product("3", "Coffee Beans", "Food", 50,
                new BigDecimal("12.99")),
            new Product("4", "Monitor", "Electronics", 8,
                new BigDecimal("299.99")),
            new Product("5", "Jeans", "Clothing", 25,
                new BigDecimal("49.99"))
        );
    }
}
```

**API Validation Points**

- ✓ `bindItems()` for reactive grid/list data binding
- ✓ Collection signals with computed filtering
- ✓ Multiple filter signals combined in single computation
- ✓ Performance: efficient list filtering with signals
- ✓ Derived count/statistics from filtered data
- ✓ Integration with Vaadin Grid component

---

### Use Case 10: Dynamic Task List with Real-time Updates

**Business Scenario**

A task management view where users can add, complete, and remove tasks. The list shows tasks grouped by status (pending/completed), with counts and progress indicators that update automatically.

**UI Description**

- Input field and "Add Task" button
- Progress bar showing completion percentage
- Statistics: "X pending, Y completed, Z% complete"
- Pending tasks list (with checkbox to mark complete)
- Completed tasks list (with delete button)

**Signal Patterns**

- **Mutable collection signal**: List that can be modified
- **Computed derived lists**: Pending and completed sublists
- **Aggregate computations**: Counts and percentages
- **Dynamic component children**: Task components added/removed reactively

**Code Example**

```java
public class TaskListView extends VerticalLayout {

    record Task(String id, String title, boolean completed) {
        Task toggleComplete() {
            return new Task(id, title, !completed);
        }
    }

    public TaskListView() {
        // Task list signal
        WritableSignal<List<Task>> tasksSignal = Signal.create(
            new ArrayList<>()
        );

        // New task input
        WritableSignal<String> newTaskTitleSignal = Signal.create("");

        // Computed: Pending tasks
        Signal<List<Task>> pendingTasksSignal = Signal.compute(() ->
            tasksSignal.getValue().stream()
                .filter(t -> !t.completed())
                .toList()
        );

        // Computed: Completed tasks
        Signal<List<Task>> completedTasksSignal = Signal.compute(() ->
            tasksSignal.getValue().stream()
                .filter(Task::completed)
                .toList()
        );

        // Computed: Statistics
        Signal<Integer> totalCountSignal = tasksSignal.map(List::size);
        Signal<Integer> pendingCountSignal = pendingTasksSignal.map(List::size);
        Signal<Integer> completedCountSignal = completedTasksSignal.map(List::size);

        Signal<Double> completionPercentageSignal = Signal.compute(() -> {
            int total = totalCountSignal.getValue();
            if (total == 0) return 0.0;
            int completed = completedCountSignal.getValue();
            return (completed * 100.0) / total;
        });

        // Add task UI
        TextField newTaskField = new TextField();
        newTaskField.setPlaceholder("Enter task title...");
        newTaskField.bindValue(newTaskTitleSignal);

        Button addButton = new Button("Add Task");
        addButton.bindEnabled(newTaskTitleSignal.map(t -> !t.trim().isEmpty()));
        addButton.addClickListener(e -> {
            String title = newTaskTitleSignal.getValue().trim();
            if (!title.isEmpty()) {
                List<Task> current = new ArrayList<>(tasksSignal.getValue());
                current.add(new Task(UUID.randomUUID().toString(), title, false));
                tasksSignal.setValue(current);
                newTaskTitleSignal.setValue("");
            }
        });

        HorizontalLayout addTaskLayout = new HorizontalLayout(
            newTaskField, addButton
        );

        // Progress bar
        ProgressBar progressBar = new ProgressBar();
        progressBar.bindValue(completionPercentageSignal.map(p -> p / 100.0));

        // Statistics display
        Span statsDisplay = new Span();
        statsDisplay.bindText(Signal.compute(() -> {
            int pending = pendingCountSignal.getValue();
            int completed = completedCountSignal.getValue();
            double percentage = completionPercentageSignal.getValue();
            return String.format("%d pending, %d completed, %.0f%% complete",
                pending, completed, percentage);
        }));

        // Pending tasks section
        VerticalLayout pendingSection = new VerticalLayout();
        H4 pendingHeader = new H4("Pending Tasks");

        VerticalLayout pendingTasksList = new VerticalLayout();
        pendingTasksList.bindComponentChildren(pendingTasksSignal, task -> {
            HorizontalLayout taskItem = new HorizontalLayout();

            Checkbox completeCheckbox = new Checkbox(task.title());
            completeCheckbox.addValueChangeListener(e -> {
                List<Task> current = new ArrayList<>(tasksSignal.getValue());
                int index = current.indexOf(task);
                if (index >= 0) {
                    current.set(index, task.toggleComplete());
                    tasksSignal.setValue(current);
                }
            });

            taskItem.add(completeCheckbox);
            return taskItem;
        });

        pendingSection.add(pendingHeader, pendingTasksList);

        // Completed tasks section
        VerticalLayout completedSection = new VerticalLayout();
        H4 completedHeader = new H4("Completed Tasks");

        VerticalLayout completedTasksList = new VerticalLayout();
        completedTasksList.bindComponentChildren(completedTasksSignal, task -> {
            HorizontalLayout taskItem = new HorizontalLayout();

            Span taskTitle = new Span(task.title());
            taskTitle.getStyle().set("text-decoration", "line-through");

            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> {
                List<Task> current = new ArrayList<>(tasksSignal.getValue());
                current.remove(task);
                tasksSignal.setValue(current);
            });

            taskItem.add(taskTitle, deleteButton);
            taskItem.setAlignItems(Alignment.CENTER);
            return taskItem;
        });

        completedSection.add(completedHeader, completedTasksList);

        add(
            addTaskLayout,
            progressBar,
            statsDisplay,
            new Hr(),
            pendingSection,
            completedSection
        );
    }
}
```

**API Validation Points**

- ✓ `bindComponentChildren()` for dynamic list rendering
- ✓ Mutable collection signals with immutable updates
- ✓ Multiple derived lists from single source
- ✓ Aggregate computations (counts, percentages)
- ✓ `bindValue()` on ProgressBar with signal transformation
- ✓ Component factory functions for list items
- ✓ Handling add/remove/update operations on collections

---

## Category 5: Cross-Component Interactions

### Use Case 11: Cascading Location Selector

**Business Scenario**

A shipping address form with cascading dropdowns where selecting a country loads the available states/provinces, and selecting a state loads the available cities. Each level depends on the previous selection, and data is loaded asynchronously.

**UI Description**

- Dropdown: "Country" (USA, Canada, Mexico)
- Dropdown: "State/Province" (populated based on country)
- Dropdown: "City" (populated based on state)
- Display: Full formatted address updates as selections are made

**Signal Patterns**

- **Cascading dependencies**: Each dropdown's options depend on parent selection
- **Async data loading**: Cities/states loaded from API when parent changes
- **Loading states**: Show loading indicators during data fetch
- **Resetting cascade**: When parent changes, child selections clear

**Code Example**

```java
public class ShippingAddressForm extends FormLayout {

    record Country(String code, String name) {}
    record State(String code, String name, String countryCode) {}
    record City(String name, String stateCode) {}

    public ShippingAddressForm() {
        // Selection signals
        WritableSignal<Country> selectedCountrySignal = Signal.create(null);
        WritableSignal<State> selectedStateSignal = Signal.create(null);
        WritableSignal<City> selectedCitySignal = Signal.create(null);

        // Loading state signals
        WritableSignal<Boolean> loadingStatesSignal = Signal.create(false);
        WritableSignal<Boolean> loadingCitiesSignal = Signal.create(false);

        // Available options signals
        WritableSignal<List<Country>> countriesSignal = Signal.create(
            loadCountries()
        );
        WritableSignal<List<State>> statesSignal = Signal.create(List.of());
        WritableSignal<List<City>> citiesSignal = Signal.create(List.of());

        // Formatted address display
        Signal<String> formattedAddressSignal = Signal.compute(() -> {
            City city = selectedCitySignal.getValue();
            State state = selectedStateSignal.getValue();
            Country country = selectedCountrySignal.getValue();

            if (city == null || state == null || country == null) {
                return "Please complete your address selection";
            }
            return String.format("%s, %s, %s",
                city.name(), state.name(), country.name());
        });

        // Country selector
        Select<Country> countrySelect = new Select<>("Country");
        countrySelect.setItemLabelGenerator(Country::name);
        countrySelect.bindItems(countriesSignal);
        countrySelect.bindValue(selectedCountrySignal);

        // When country changes, load states and clear dependent selections
        selectedCountrySignal.addListener((oldVal, newVal) -> {
            if (newVal != null) {
                selectedStateSignal.setValue(null);
                selectedCitySignal.setValue(null);
                loadingStatesSignal.setValue(true);

                // Simulate async load
                CompletableFuture.supplyAsync(() -> loadStates(newVal.code()))
                    .thenAccept(states -> {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            statesSignal.setValue(states);
                            citiesSignal.setValue(List.of());
                            loadingStatesSignal.setValue(false);
                        }));
                    });
            } else {
                statesSignal.setValue(List.of());
                citiesSignal.setValue(List.of());
            }
        });

        // State selector
        Select<State> stateSelect = new Select<>("State/Province");
        stateSelect.setItemLabelGenerator(State::name);
        stateSelect.bindItems(statesSignal);
        stateSelect.bindValue(selectedStateSignal);
        stateSelect.bindEnabled(Signal.compute(() ->
            !loadingStatesSignal.getValue()
            && selectedCountrySignal.getValue() != null
            && !statesSignal.getValue().isEmpty()
        ));

        // Loading indicator for states
        Span stateLoadingIndicator = new Span("Loading states...");
        stateLoadingIndicator.bindVisible(loadingStatesSignal);

        // When state changes, load cities
        selectedStateSignal.addListener((oldVal, newVal) -> {
            if (newVal != null) {
                selectedCitySignal.setValue(null);
                loadingCitiesSignal.setValue(true);

                CompletableFuture.supplyAsync(() -> loadCities(newVal.code()))
                    .thenAccept(cities -> {
                        getUI().ifPresent(ui -> ui.access(() -> {
                            citiesSignal.setValue(cities);
                            loadingCitiesSignal.setValue(false);
                        }));
                    });
            } else {
                citiesSignal.setValue(List.of());
            }
        });

        // City selector
        Select<City> citySelect = new Select<>("City");
        citySelect.setItemLabelGenerator(City::name);
        citySelect.bindItems(citiesSignal);
        citySelect.bindValue(selectedCitySignal);
        citySelect.bindEnabled(Signal.compute(() ->
            !loadingCitiesSignal.getValue()
            && selectedStateSignal.getValue() != null
            && !citiesSignal.getValue().isEmpty()
        ));

        // Loading indicator for cities
        Span cityLoadingIndicator = new Span("Loading cities...");
        cityLoadingIndicator.bindVisible(loadingCitiesSignal);

        // Address display
        Div addressDisplay = new Div();
        addressDisplay.bindText(formattedAddressSignal);
        addressDisplay.addClassName("formatted-address");

        add(
            countrySelect,
            stateSelect, stateLoadingIndicator,
            citySelect, cityLoadingIndicator,
            addressDisplay
        );
    }

    private List<Country> loadCountries() {
        return List.of(
            new Country("US", "United States"),
            new Country("CA", "Canada"),
            new Country("MX", "Mexico")
        );
    }

    private List<State> loadStates(String countryCode) {
        // Simulate API delay
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        return switch(countryCode) {
            case "US" -> List.of(
                new State("CA", "California", "US"),
                new State("NY", "New York", "US"),
                new State("TX", "Texas", "US")
            );
            case "CA" -> List.of(
                new State("ON", "Ontario", "CA"),
                new State("BC", "British Columbia", "CA"),
                new State("QC", "Quebec", "CA")
            );
            default -> List.of();
        };
    }

    private List<City> loadCities(String stateCode) {
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        return switch(stateCode) {
            case "CA" -> List.of(
                new City("Los Angeles", "CA"),
                new City("San Francisco", "CA"),
                new City("San Diego", "CA")
            );
            case "NY" -> List.of(
                new City("New York City", "NY"),
                new City("Buffalo", "NY"),
                new City("Albany", "NY")
            );
            default -> List.of();
        };
    }
}
```

**API Validation Points**

- ✓ Cascading signal dependencies with state reset
- ✓ `signal.addListener()` for triggering side effects (data loading)
- ✓ Async data loading with signal updates
- ✓ Loading state management with signals
- ✓ `bindEnabled()` with complex computed conditions
- ✓ `bindItems()` with dynamically changing lists
- ✓ Thread safety: updating signals from async tasks with UI.access()

---

### Use Case 12: Shopping Cart with Real-time Totals

**Business Scenario**

An e-commerce shopping cart where users can adjust quantities of multiple items. The subtotal, shipping, tax, and grand total update automatically as quantities change. Discount codes and shipping options affect the final price.

**UI Description**

- List of cart items, each with:
  - Product name and price
  - Quantity spinner
  - Line total (price × quantity)
  - Remove button
- Discount code input field
- Shipping method selector (Standard, Express, Overnight)
- Price summary:
  - Subtotal
  - Discount
  - Shipping
  - Tax
  - Grand Total

**Signal Patterns**

- **Collection of signals**: Each cart item has quantity signal
- **Multi-source computation**: Total depends on all item quantities, discount, shipping
- **Validation**: Discount code validation affects pricing
- **Complex business logic**: Tax calculation, shipping costs

**Code Example**

```java
public class ShoppingCartView extends VerticalLayout {

    record CartItem(
        String id,
        String name,
        BigDecimal price,
        WritableSignal<Integer> quantitySignal
    ) {
        Signal<BigDecimal> lineTotal() {
            return quantitySignal.map(qty ->
                price.multiply(BigDecimal.valueOf(qty))
            );
        }
    }

    enum ShippingMethod {
        STANDARD(new BigDecimal("5.00")),
        EXPRESS(new BigDecimal("15.00")),
        OVERNIGHT(new BigDecimal("30.00"));

        final BigDecimal cost;
        ShippingMethod(BigDecimal cost) { this.cost = cost; }
    }

    public ShoppingCartView() {
        // Cart items with individual quantity signals
        WritableSignal<List<CartItem>> cartItemsSignal = Signal.create(
            new ArrayList<>(List.of(
                new CartItem("1", "Laptop", new BigDecimal("999.99"),
                    Signal.create(1)),
                new CartItem("2", "Mouse", new BigDecimal("29.99"),
                    Signal.create(2)),
                new CartItem("3", "Keyboard", new BigDecimal("79.99"),
                    Signal.create(1))
            ))
        );

        // Other pricing factors
        WritableSignal<String> discountCodeSignal = Signal.create("");
        WritableSignal<ShippingMethod> shippingMethodSignal =
            Signal.create(ShippingMethod.STANDARD);

        // Computed: Subtotal (sum of all line totals)
        Signal<BigDecimal> subtotalSignal = Signal.compute(() ->
            cartItemsSignal.getValue().stream()
                .map(item -> item.lineTotal().getValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        // Computed: Discount validation and amount
        Signal<Boolean> discountValidSignal = Signal.compute(() -> {
            String code = discountCodeSignal.getValue();
            return "SAVE10".equals(code) || "SAVE20".equals(code);
        });

        Signal<BigDecimal> discountAmountSignal = Signal.compute(() -> {
            if (!discountValidSignal.getValue()) return BigDecimal.ZERO;

            String code = discountCodeSignal.getValue();
            double rate = "SAVE20".equals(code) ? 0.20 : 0.10;
            return subtotalSignal.getValue().multiply(BigDecimal.valueOf(rate));
        });

        // Computed: After discount
        Signal<BigDecimal> afterDiscountSignal = Signal.compute(() ->
            subtotalSignal.getValue().subtract(discountAmountSignal.getValue())
        );

        // Computed: Shipping cost (free if over $100 after discount)
        Signal<BigDecimal> shippingCostSignal = Signal.compute(() -> {
            BigDecimal afterDiscount = afterDiscountSignal.getValue();
            if (afterDiscount.compareTo(new BigDecimal("100")) >= 0) {
                return BigDecimal.ZERO;
            }
            return shippingMethodSignal.getValue().cost;
        });

        // Computed: Tax (8% on subtotal after discount)
        Signal<BigDecimal> taxSignal = afterDiscountSignal.map(amount ->
            amount.multiply(new BigDecimal("0.08"))
        );

        // Computed: Grand total
        Signal<BigDecimal> grandTotalSignal = Signal.compute(() ->
            afterDiscountSignal.getValue()
                .add(shippingCostSignal.getValue())
                .add(taxSignal.getValue())
        );

        // Render cart items
        VerticalLayout cartItemsLayout = new VerticalLayout();
        cartItemsLayout.bindComponentChildren(cartItemsSignal, item -> {
            HorizontalLayout itemLayout = new HorizontalLayout();
            itemLayout.setAlignItems(Alignment.CENTER);

            Span nameAndPrice = new Span(
                item.name() + " - $" + item.price()
            );

            IntegerField quantityField = new IntegerField();
            quantityField.setWidth("80px");
            quantityField.setMin(1);
            quantityField.setMax(99);
            quantityField.bindValue(item.quantitySignal());

            Span lineTotal = new Span();
            lineTotal.bindText(item.lineTotal().map(total ->
                "$" + total.setScale(2, RoundingMode.HALF_UP)
            ));
            lineTotal.getStyle().set("font-weight", "bold");

            Button removeButton = new Button("Remove");
            removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_ERROR);
            removeButton.addClickListener(e -> {
                List<CartItem> current = new ArrayList<>(
                    cartItemsSignal.getValue()
                );
                current.remove(item);
                cartItemsSignal.setValue(current);
            });

            itemLayout.add(nameAndPrice, quantityField, lineTotal, removeButton);
            return itemLayout;
        });

        // Discount code section
        TextField discountCodeField = new TextField("Discount Code");
        discountCodeField.bindValue(discountCodeSignal);
        discountCodeField.bindHelperText(Signal.compute(() -> {
            String code = discountCodeSignal.getValue();
            if (code.isEmpty()) return "";
            return discountValidSignal.getValue()
                ? "✓ Discount applied"
                : "✗ Invalid code";
        }));

        // Shipping method selector
        Select<ShippingMethod> shippingSelect =
            new Select<>("Shipping Method", ShippingMethod.values());
        shippingSelect.bindValue(shippingMethodSignal);

        // Free shipping indicator
        Span freeShippingIndicator = new Span(
            "🎉 Free shipping on orders over $100!");
        freeShippingIndicator.bindVisible(shippingCostSignal.map(
            cost -> cost.compareTo(BigDecimal.ZERO) == 0
        ));
        freeShippingIndicator.addClassName("free-shipping-badge");

        // Price summary
        VerticalLayout priceSummary = new VerticalLayout();
        priceSummary.addClassName("price-summary");

        Span subtotalDisplay = new Span();
        subtotalDisplay.bindText(subtotalSignal.map(v ->
            "Subtotal: $" + v.setScale(2, RoundingMode.HALF_UP)));

        Span discountDisplay = new Span();
        discountDisplay.bindText(discountAmountSignal.map(v ->
            "Discount: -$" + v.setScale(2, RoundingMode.HALF_UP)));
        discountDisplay.bindVisible(discountAmountSignal.map(
            v -> v.compareTo(BigDecimal.ZERO) > 0));

        Span shippingDisplay = new Span();
        shippingDisplay.bindText(shippingCostSignal.map(v ->
            "Shipping: $" + v.setScale(2, RoundingMode.HALF_UP)));

        Span taxDisplay = new Span();
        taxDisplay.bindText(taxSignal.map(v ->
            "Tax: $" + v.setScale(2, RoundingMode.HALF_UP)));

        H3 grandTotalDisplay = new H3();
        grandTotalDisplay.bindText(grandTotalSignal.map(v ->
            "Total: $" + v.setScale(2, RoundingMode.HALF_UP)));

        priceSummary.add(
            subtotalDisplay,
            discountDisplay,
            shippingDisplay,
            taxDisplay,
            new Hr(),
            grandTotalDisplay
        );

        add(
            new H2("Shopping Cart"),
            cartItemsLayout,
            new Hr(),
            discountCodeField,
            shippingSelect,
            freeShippingIndicator,
            priceSummary
        );
    }
}
```

**API Validation Points**

- ✓ Collection of items with individual signals
- ✓ Aggregating signals from collection items
- ✓ Complex multi-stage computation chains
- ✓ Conditional pricing logic (free shipping threshold)
- ✓ Dynamic collection updates (add/remove items)
- ✓ Multiple UI elements reacting to same signal
- ✓ Combining `bindText()` and `bindVisible()` for conditional display

---

### Use Case 13: Master-Detail Invoice View

**Business Scenario**

An invoices list where selecting an invoice displays its line items, customer information, and payment status. The detail view updates reactively when a different invoice is selected.

**UI Description**

- Left side: List of invoices (invoice number, date, total, status)
- Right side (detail panel):
  - Customer information
  - Invoice metadata (date, due date, status)
  - Line items table
  - Payment history
  - Action buttons (Mark Paid, Send Reminder, Download PDF)

**Signal Patterns**

- **Master-detail navigation**: Selection signal drives detail display
- **Derived data**: Detail panel content computed from selection
- **Conditional actions**: Buttons enabled based on invoice status
- **Null state handling**: Empty state when no selection

**Code Example**

```java
public class InvoicesView extends HorizontalLayout {

    record Invoice(
        String id,
        String invoiceNumber,
        String customerName,
        LocalDate issueDate,
        LocalDate dueDate,
        InvoiceStatus status,
        BigDecimal total,
        List<LineItem> lineItems
    ) {}

    record LineItem(String description, int quantity, BigDecimal unitPrice) {
        BigDecimal total() {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    enum InvoiceStatus { DRAFT, SENT, PAID, OVERDUE }

    public InvoicesView() {
        // Selected invoice signal
        WritableSignal<Invoice> selectedInvoiceSignal = Signal.create(null);

        // Invoice list
        List<Invoice> allInvoices = loadInvoices();

        VerticalLayout invoiceListPanel = new VerticalLayout();
        invoiceListPanel.setWidth("300px");
        invoiceListPanel.add(new H3("Invoices"));

        for (Invoice invoice : allInvoices) {
            Button invoiceButton = new Button(
                invoice.invoiceNumber() + " - $" + invoice.total()
            );
            invoiceButton.setWidthFull();

            // Highlight selected invoice
            invoiceButton.bindThemeName(selectedInvoiceSignal.map(selected ->
                invoice.equals(selected) ? "primary" : ""
            ));

            invoiceButton.addClickListener(e ->
                selectedInvoiceSignal.setValue(invoice)
            );

            invoiceListPanel.add(invoiceButton);
        }

        // Detail panel
        VerticalLayout detailPanel = new VerticalLayout();
        detailPanel.setWidthFull();

        // Show/hide detail panel based on selection
        detailPanel.bindVisible(selectedInvoiceSignal.map(inv -> inv != null));

        // Invoice header
        H2 invoiceHeader = new H2();
        invoiceHeader.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? "Invoice " + inv.invoiceNumber() : ""
        ));

        // Customer info
        Span customerName = new Span();
        customerName.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? "Customer: " + inv.customerName() : ""
        ));

        // Dates
        Span issueDateDisplay = new Span();
        issueDateDisplay.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? "Issued: " + inv.issueDate() : ""
        ));

        Span dueDateDisplay = new Span();
        dueDateDisplay.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? "Due: " + inv.dueDate() : ""
        ));

        // Status badge
        Span statusBadge = new Span();
        statusBadge.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? inv.status().name() : ""
        ));
        statusBadge.bindThemeName(selectedInvoiceSignal.map(inv -> {
            if (inv == null) return "";
            return switch(inv.status()) {
                case PAID -> "badge success";
                case OVERDUE -> "badge error";
                case SENT -> "badge";
                case DRAFT -> "badge contrast";
            };
        }));

        // Line items grid
        Grid<LineItem> lineItemsGrid = new Grid<>(LineItem.class, false);
        lineItemsGrid.addColumn(LineItem::description).setHeader("Description");
        lineItemsGrid.addColumn(LineItem::quantity).setHeader("Quantity");
        lineItemsGrid.addColumn(item -> "$" + item.unitPrice())
            .setHeader("Unit Price");
        lineItemsGrid.addColumn(item -> "$" + item.total())
            .setHeader("Total");

        lineItemsGrid.bindItems(selectedInvoiceSignal.map(inv ->
            inv != null ? inv.lineItems() : List.of()
        ));

        // Total
        H3 totalDisplay = new H3();
        totalDisplay.bindText(selectedInvoiceSignal.map(inv ->
            inv != null ? "Total: $" + inv.total() : ""
        ));

        // Action buttons
        HorizontalLayout actionButtons = new HorizontalLayout();

        Button markPaidButton = new Button("Mark as Paid");
        markPaidButton.bindEnabled(selectedInvoiceSignal.map(inv ->
            inv != null && inv.status() != InvoiceStatus.PAID
        ));

        Button sendReminderButton = new Button("Send Reminder");
        sendReminderButton.bindEnabled(selectedInvoiceSignal.map(inv ->
            inv != null &&
            (inv.status() == InvoiceStatus.SENT ||
             inv.status() == InvoiceStatus.OVERDUE)
        ));

        Button downloadButton = new Button("Download PDF");
        downloadButton.bindEnabled(selectedInvoiceSignal.map(inv ->
            inv != null && inv.status() != InvoiceStatus.DRAFT
        ));

        actionButtons.add(markPaidButton, sendReminderButton, downloadButton);

        // Empty state
        VerticalLayout emptyState = new VerticalLayout();
        emptyState.bindVisible(selectedInvoiceSignal.map(inv -> inv == null));
        emptyState.add(new Span("Select an invoice to view details"));
        emptyState.setAlignItems(Alignment.CENTER);
        emptyState.setJustifyContentMode(JustifyContentMode.CENTER);

        detailPanel.add(
            invoiceHeader,
            customerName,
            new HorizontalLayout(issueDateDisplay, dueDateDisplay, statusBadge),
            lineItemsGrid,
            totalDisplay,
            actionButtons,
            emptyState
        );

        add(invoiceListPanel, detailPanel);
        setSizeFull();
    }

    private List<Invoice> loadInvoices() {
        return List.of(
            new Invoice("1", "INV-001", "Acme Corp", LocalDate.now(),
                LocalDate.now().plusDays(30), InvoiceStatus.SENT,
                new BigDecimal("1250.00"),
                List.of(
                    new LineItem("Consulting Services", 10,
                        new BigDecimal("100.00")),
                    new LineItem("Software License", 1,
                        new BigDecimal("250.00"))
                )),
            new Invoice("2", "INV-002", "TechStart Inc", LocalDate.now(),
                LocalDate.now().minusDays(5), InvoiceStatus.OVERDUE,
                new BigDecimal("850.00"),
                List.of(
                    new LineItem("Development", 8,
                        new BigDecimal("100.00")),
                    new LineItem("Hosting", 1,
                        new BigDecimal("50.00"))
                ))
        );
    }
}
```

**API Validation Points**

- ✓ Master-detail pattern with selection signal
- ✓ Conditional rendering of entire panel sections
- ✓ `bindThemeName()` for dynamic styling based on state
- ✓ Multiple UI elements deriving from single selection signal
- ✓ Null-safe signal transformations with `map()`
- ✓ `bindItems()` with derived signal
- ✓ Button enabled states based on complex conditions
- ✓ Empty state handling

---

## Category 6: Complex Forms

### Use Case 14: Multi-Step Wizard with Validation

**Business Scenario**

A multi-step registration wizard where users progress through steps (Personal Info → Company Info → Plan Selection → Review). Each step must be valid before proceeding. The wizard shows progress and allows navigation back to previous steps.

**UI Description**

- Progress indicator (steps 1-4)
- Current step content (changes based on active step)
- "Previous" and "Next" buttons
- "Submit" button on final step
- Validation messages for each field
- Summary on review step

**Signal Patterns**

- **State machine**: Current step as signal
- **Step validation**: Each step has computed validity signal
- **Navigation guards**: Can't proceed if current step invalid
- **Aggregated data**: Final submission combines all step data

**Code Example**

```java
public class RegistrationWizardView extends VerticalLayout {

    enum WizardStep { PERSONAL_INFO, COMPANY_INFO, PLAN_SELECTION, REVIEW }

    record PersonalInfo(String firstName, String lastName, String email) {}
    record CompanyInfo(String companyName, String industry, int employeeCount) {}
    record PlanSelection(String planType, boolean annualBilling) {}

    public RegistrationWizardView() {
        // Current step signal
        WritableSignal<WizardStep> currentStepSignal =
            Signal.create(WizardStep.PERSONAL_INFO);

        // Step 1: Personal Info signals and validation
        WritableSignal<String> firstNameSignal = Signal.create("");
        WritableSignal<String> lastNameSignal = Signal.create("");
        WritableSignal<String> emailSignal = Signal.create("");

        Signal<Boolean> step1ValidSignal = Signal.compute(() ->
            !firstNameSignal.getValue().trim().isEmpty() &&
            !lastNameSignal.getValue().trim().isEmpty() &&
            emailSignal.getValue().contains("@")
        );

        // Step 2: Company Info signals and validation
        WritableSignal<String> companyNameSignal = Signal.create("");
        WritableSignal<String> industrySignal = Signal.create("");
        WritableSignal<Integer> employeeCountSignal = Signal.create(1);

        Signal<Boolean> step2ValidSignal = Signal.compute(() ->
            !companyNameSignal.getValue().trim().isEmpty() &&
            !industrySignal.getValue().trim().isEmpty() &&
            employeeCountSignal.getValue() > 0
        );

        // Step 3: Plan Selection signals and validation
        WritableSignal<String> planTypeSignal = Signal.create("");
        WritableSignal<Boolean> annualBillingSignal = Signal.create(false);

        Signal<Boolean> step3ValidSignal = Signal.compute(() ->
            !planTypeSignal.getValue().isEmpty()
        );

        // Progress indicator
        HorizontalLayout progressIndicator = new HorizontalLayout();
        for (WizardStep step : WizardStep.values()) {
            Span stepIndicator = new Span((step.ordinal() + 1) + ". " +
                formatStepName(step));

            stepIndicator.bindClassName("step-active",
                currentStepSignal.map(current -> current == step)
            );
            stepIndicator.bindClassName("step-completed",
                currentStepSignal.map(current -> current.ordinal() > step.ordinal())
            );

            progressIndicator.add(stepIndicator);
        }

        // Step 1: Personal Info Form
        FormLayout personalInfoForm = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        firstNameField.bindValue(firstNameSignal);
        firstNameField.setRequired(true);

        TextField lastNameField = new TextField("Last Name");
        lastNameField.bindValue(lastNameSignal);
        lastNameField.setRequired(true);

        EmailField emailField = new EmailField("Email");
        emailField.bindValue(emailSignal);
        emailField.setRequired(true);

        personalInfoForm.add(firstNameField, lastNameField, emailField);
        personalInfoForm.bindVisible(currentStepSignal.map(
            step -> step == WizardStep.PERSONAL_INFO
        ));

        // Step 2: Company Info Form
        FormLayout companyInfoForm = new FormLayout();

        TextField companyNameField = new TextField("Company Name");
        companyNameField.bindValue(companyNameSignal);

        TextField industryField = new TextField("Industry");
        industryField.bindValue(industrySignal);

        IntegerField employeeCountField = new IntegerField("Number of Employees");
        employeeCountField.bindValue(employeeCountSignal);
        employeeCountField.setMin(1);

        companyInfoForm.add(companyNameField, industryField, employeeCountField);
        companyInfoForm.bindVisible(currentStepSignal.map(
            step -> step == WizardStep.COMPANY_INFO
        ));

        // Step 3: Plan Selection Form
        VerticalLayout planSelectionForm = new VerticalLayout();

        RadioButtonGroup<String> planTypeGroup = new RadioButtonGroup<>("Select Plan");
        planTypeGroup.setItems("Basic ($10/mo)", "Pro ($25/mo)",
            "Enterprise ($50/mo)");
        planTypeGroup.addValueChangeListener(e ->
            planTypeSignal.setValue(e.getValue())
        );

        Checkbox annualBillingCheckbox = new Checkbox(
            "Pay annually (save 20%)");
        annualBillingCheckbox.bindValue(annualBillingSignal);

        planSelectionForm.add(planTypeGroup, annualBillingCheckbox);
        planSelectionForm.bindVisible(currentStepSignal.map(
            step -> step == WizardStep.PLAN_SELECTION
        ));

        // Step 4: Review
        VerticalLayout reviewPanel = new VerticalLayout();
        reviewPanel.bindVisible(currentStepSignal.map(
            step -> step == WizardStep.REVIEW
        ));

        H3 reviewHeader = new H3("Review Your Information");

        Span personalInfoSummary = new Span();
        personalInfoSummary.bindText(Signal.compute(() ->
            String.format("Name: %s %s\nEmail: %s",
                firstNameSignal.getValue(),
                lastNameSignal.getValue(),
                emailSignal.getValue())
        ));

        Span companyInfoSummary = new Span();
        companyInfoSummary.bindText(Signal.compute(() ->
            String.format("Company: %s\nIndustry: %s\nEmployees: %d",
                companyNameSignal.getValue(),
                industrySignal.getValue(),
                employeeCountSignal.getValue())
        ));

        Span planSummary = new Span();
        planSummary.bindText(Signal.compute(() ->
            String.format("Plan: %s\nBilling: %s",
                planTypeSignal.getValue(),
                annualBillingSignal.getValue() ? "Annual" : "Monthly")
        ));

        reviewPanel.add(reviewHeader, personalInfoSummary,
            companyInfoSummary, planSummary);

        // Navigation buttons
        HorizontalLayout navigationButtons = new HorizontalLayout();

        Button previousButton = new Button("Previous");
        previousButton.bindEnabled(currentStepSignal.map(
            step -> step != WizardStep.PERSONAL_INFO
        ));
        previousButton.addClickListener(e -> {
            WizardStep current = currentStepSignal.getValue();
            if (current.ordinal() > 0) {
                currentStepSignal.setValue(
                    WizardStep.values()[current.ordinal() - 1]
                );
            }
        });

        Button nextButton = new Button("Next");
        nextButton.bindVisible(currentStepSignal.map(
            step -> step != WizardStep.REVIEW
        ));
        nextButton.bindEnabled(Signal.compute(() -> {
            WizardStep current = currentStepSignal.getValue();
            return switch(current) {
                case PERSONAL_INFO -> step1ValidSignal.getValue();
                case COMPANY_INFO -> step2ValidSignal.getValue();
                case PLAN_SELECTION -> step3ValidSignal.getValue();
                default -> false;
            };
        }));
        nextButton.addClickListener(e -> {
            WizardStep current = currentStepSignal.getValue();
            if (current.ordinal() < WizardStep.values().length - 1) {
                currentStepSignal.setValue(
                    WizardStep.values()[current.ordinal() + 1]
                );
            }
        });

        Button submitButton = new Button("Submit Registration");
        submitButton.bindVisible(currentStepSignal.map(
            step -> step == WizardStep.REVIEW
        ));
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        navigationButtons.add(previousButton, nextButton, submitButton);

        add(
            progressIndicator,
            personalInfoForm,
            companyInfoForm,
            planSelectionForm,
            reviewPanel,
            navigationButtons
        );
    }

    private String formatStepName(WizardStep step) {
        return step.name().replace("_", " ");
    }
}
```

**API Validation Points**

- ✓ State machine implementation with signal
- ✓ Step-specific validation signals
- ✓ Navigation guards based on computed validity
- ✓ Conditional visibility for wizard steps
- ✓ `bindClassName()` for dynamic styling
- ✓ Aggregated data from multiple step signals
- ✓ Complex `bindEnabled()` logic with switch expressions

---

## Summary: API Feature Coverage Matrix

This matrix shows which signal API features are exercised by each use case:

| Use Case | bindValue | bindVisible | bindEnabled | bindText | bindThemeName | bindClassName | bindHelperText | bindAttribute | bindItems | bindComponentChildren | Signal.create | Signal.compute | signal.map | signal.addListener | Async Operations | Collections | Validation |
|----------|-----------|-------------|-------------|----------|---------------|---------------|----------------|---------------|-----------|----------------------|---------------|----------------|------------|-------------------|------------------|-------------|------------|
| 1. Profile Settings | ✓ | ✓ | | | | | | | | | ✓ | | | | | | |
| 2. Form Field Sync | ✓ | | | ✓ | | | | | | | ✓ | ✓ | ✓ | | | | |
| 3. Dynamic Button State | ✓ | | ✓ | ✓ | ✓ | | | | | | ✓ | ✓ | ✓ | | ✓ | | ✓ |
| 4. Product Configurator | ✓ | | ✓ | ✓ | ✓ | | | ✓ | | | ✓ | ✓ | ✓ | | | | |
| 5. Pricing Calculator | ✓ | ✓ | | ✓ | | | ✓ | | | | ✓ | ✓ | ✓ | | | | ✓ |
| 6. Conditional Subforms | ✓ | ✓ | | | | | | | | | ✓ | | ✓ | | | | |
| 7. Nested Conditions | ✓ | ✓ | | | | | | | | | ✓ | ✓ | | ✓ | | | |
| 8. Permission-Based UI | | ✓ | | ✓ | | | | | | | ✓ | | ✓ | | | | |
| 9. Filtered Data Grid | ✓ | | | ✓ | | | | | ✓ | | ✓ | ✓ | ✓ | | | ✓ | |
| 10. Dynamic Task List | ✓ | | ✓ | ✓ | | | | | | ✓ | ✓ | ✓ | ✓ | | | ✓ | |
| 11. Cascading Selector | ✓ | ✓ | ✓ | ✓ | | | | | ✓ | | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | |
| 12. Shopping Cart | ✓ | ✓ | | ✓ | | | ✓ | | | ✓ | ✓ | ✓ | ✓ | | | ✓ | ✓ |
| 13. Master-Detail | | ✓ | ✓ | ✓ | ✓ | | | | ✓ | | ✓ | | ✓ | | | | |
| 14. Multi-Step Wizard | ✓ | ✓ | ✓ | ✓ | | ✓ | | | | | ✓ | ✓ | ✓ | | | | ✓ |

### API Feature Usage Summary

**Most Used Features:**
- `Signal.create()` - 14/14 use cases (100%)
- `signal.map()` - 13/14 use cases (93%)
- `bindVisible()` - 10/14 use cases (71%)
- `bindValue()` - 10/14 use cases (71%)
- `bindText()` - 10/14 use cases (71%)
- `Signal.compute()` - 10/14 use cases (71%)

**Moderately Used Features:**
- `bindEnabled()` - 6/14 use cases (43%)
- `bindThemeName()` - 4/14 use cases (29%)
- `bindItems()` - 4/14 use cases (29%)

**Specialized Features:**
- `bindComponentChildren()` - 3/14 use cases (21%)
- `signal.addListener()` - 3/14 use cases (21%)
- `bindHelperText()` - 2/14 use cases (14%)
- `bindClassName()` - 2/14 use cases (14%)
- `bindAttribute()` - 1/14 use cases (7%)

**Pattern Coverage:**
- **Collections**: 6/14 use cases (43%)
- **Validation**: 6/14 use cases (43%)
- **Async Operations**: 2/14 use cases (14%)

---

## Observations and Recommendations

### Strengths of the Signal API

1. **Declarative Binding Syntax**: The `bindXxx()` methods provide a clean, readable way to connect signals to UI components.

2. **Automatic Dependency Tracking**: `Signal.compute()` automatically tracks dependencies, eliminating manual subscription management.

3. **Comprehensive Binding Methods**: The API covers most common UI binding scenarios (visibility, enabled state, text, value, etc.).

4. **Signal Composition**: The `map()` method enables elegant signal transformations and composition.

5. **Lifecycle Management**: The framework handles automatic cleanup when components are detached.

### Potential API Improvements

1. **Signal Combination Utility**: Consider adding `Signal.combine(signal1, signal2, ..., combiner)` for common multi-signal computations:
   ```java
   Signal<String> fullName = Signal.combine(
       firstNameSignal,
       lastNameSignal,
       (first, last) -> first + " " + last
   );
   ```

2. **Debouncing/Throttling**: Add built-in support for debouncing rapid signal changes:
   ```java
   Signal<String> debouncedSearch = searchSignal.debounce(300, TimeUnit.MILLISECONDS);
   ```

3. **Signal Validation**: Built-in validation support could simplify form validation:
   ```java
   ValidatedSignal<String> emailSignal = Signal.create("")
       .withValidator(email -> email.contains("@"), "Invalid email");
   ```

4. **List Signal Operations**: More ergonomic APIs for list mutations:
   ```java
   ListSignal<Item> items = Signal.createList();
   items.add(newItem);  // Instead of items.setValue(new ArrayList<>(items.getValue()))
   ```

5. **Async Signal Creation**: Helper for creating signals from async operations:
   ```java
   Signal<Data> dataSignal = Signal.fromFuture(fetchDataAsync());
   ```

6. **Signal Effects**: Explicit API for side effects:
   ```java
   Signal.effect(() -> {
       // Runs when any accessed signal changes
       logAnalytics(userActionSignal.getValue());
   });
   ```

### Testing Recommendations

When implementing the signal API, ensure tests cover:

1. **Memory Management**: Verify that bindings are cleaned up when components are detached
2. **Performance**: Test with large numbers of signals and bindings
3. **Thread Safety**: Validate async signal updates with UI.access()
4. **Circular Dependencies**: Handle or detect circular signal dependencies
5. **Null Safety**: Ensure null values in signals are handled gracefully
6. **Batching**: Verify that multiple signal changes trigger only one UI update

### Integration Points to Validate

1. **Vaadin Components**: Ensure all standard components support signal bindings
2. **Custom Components**: Provide clear patterns for adding signal support to custom components
3. **Binder Integration**: Consider how signals interact with existing Binder API
4. **Serialization**: Define behavior for signal state during view serialization
5. **Push**: Validate that signals work correctly with server push enabled

---

## Conclusion

These 14 use cases demonstrate that the Vaadin Signal API can handle a wide range of reactive patterns in business applications, from simple one-way bindings to complex multi-step forms with validation.

The API excels at:
- Eliminating boilerplate event listener code
- Providing declarative, readable component bindings
- Automatically managing dependencies and lifecycles
- Supporting both simple and complex reactive patterns

By implementing and testing against these use cases, the Vaadin team can ensure the signal API meets real-world requirements and identify any gaps or rough edges before release.

### Next Steps

1. **Prototype Implementation**: Implement 2-3 representative use cases to validate API ergonomics
2. **Performance Testing**: Benchmark signal performance with realistic component counts
3. **Developer Feedback**: Share use cases with Vaadin developers for feedback
4. **API Refinement**: Iterate on API design based on implementation learnings
5. **Documentation**: Use these examples as basis for signal API documentation

