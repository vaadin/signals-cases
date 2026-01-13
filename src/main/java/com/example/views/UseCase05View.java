package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.button.Button;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Route(value = "use-case-05", layout = MainLayout.class)
@PageTitle("Use Case 5: Dynamic Pricing Calculator")
@Menu(order = 21, title = "UC 5: Pricing Calculator")
public class UseCase05View extends VerticalLayout {

    enum ServiceType { BASIC, STANDARD, PREMIUM }
    enum AddOn { SUPPORT, BACKUP, ANALYTICS, CDN }

    record DiscountCode(String code, BigDecimal percentage) {}

    public UseCase05View() {
        // Create signals for pricing inputs
        WritableSignal<ServiceType> serviceSignal = new ValueSignal<>(ServiceType.BASIC);
        WritableSignal<Set<AddOn>> addOnsSignal = new ValueSignal<>(Set.of());
        WritableSignal<Integer> quantitySignal = new ValueSignal<>(1);
        WritableSignal<String> discountCodeSignal = new ValueSignal<>("");
        WritableSignal<BigDecimal> taxRateSignal = new ValueSignal<>(new BigDecimal("0.08"));

        // UI components
        ComboBox<ServiceType> serviceSelect = new ComboBox<>("Base Service", ServiceType.values());
        serviceSelect.setValue(ServiceType.BASIC);
        MissingAPI.bindValue(serviceSelect, serviceSignal);

        CheckboxGroup<AddOn> addOnsCheckbox = new CheckboxGroup<>("Add-ons", AddOn.values());
        MissingAPI.bindValue(addOnsCheckbox, addOnsSignal);

        IntegerField quantityField = new IntegerField("Quantity");
        quantityField.setValue(1);
        quantityField.setMin(1);
        MissingAPI.bindValue(quantityField, quantitySignal);

        TextField discountField = new TextField("Discount Code");
        MissingAPI.bindValue(discountField, discountCodeSignal);

        TextField taxRateField = new TextField("Tax Rate (%)");
        taxRateField.setValue("8");

        // Computed signals for price breakdown
        Signal<BigDecimal> basePrice = Signal.computed(() -> getServicePrice(serviceSignal.value()));

        Signal<BigDecimal> addOnsPrice = Signal.computed(() ->
            addOnsSignal.value().stream()
                .map(this::getAddOnPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        Signal<BigDecimal> subtotal = Signal.computed(() -> {
            BigDecimal base = basePrice.value();
            BigDecimal addOns = addOnsPrice.value();
            int quantity = quantitySignal.value();
            return base.add(addOns).multiply(BigDecimal.valueOf(quantity));
        });

        Signal<BigDecimal> discountAmount = Signal.computed(() -> {
            String code = discountCodeSignal.value();
            DiscountCode discount = validateDiscountCode(code);
            if (discount != null) {
                return subtotal.value().multiply(discount.percentage()).divide(new BigDecimal("100"));
            }
            return BigDecimal.ZERO;
        });

        Signal<BigDecimal> subtotalAfterDiscount = Signal.computed(() ->
            subtotal.value().subtract(discountAmount.value())
        );

        Signal<BigDecimal> tax = Signal.computed(() ->
            subtotalAfterDiscount.value().multiply(taxRateSignal.value())
        );

        Signal<BigDecimal> total = Signal.computed(() ->
            subtotalAfterDiscount.value().add(tax.value())
        );

        // Display components
        Div priceBreakdown = new Div();
        priceBreakdown.add(new H3("Price Breakdown"));

        Span basePriceLabel = new Span();
        MissingAPI.bindText(basePriceLabel, basePrice.map(p -> "Base Service: $" + p));

        Span addOnsPriceLabel = new Span();
        MissingAPI.bindText(addOnsPriceLabel, addOnsPrice.map(p -> "Add-ons: $" + p));

        Span subtotalLabel = new Span();
        MissingAPI.bindText(subtotalLabel, subtotal.map(p -> "Subtotal: $" + p.setScale(2, RoundingMode.HALF_UP)));

        Span discountLabel = new Span();
        MissingAPI.bindText(discountLabel, discountAmount.map(p -> "Discount: -$" + p.setScale(2, RoundingMode.HALF_UP)));
        discountLabel.bindVisible(discountAmount.map(p -> p.compareTo(BigDecimal.ZERO) > 0));

        Span taxLabel = new Span();
        MissingAPI.bindText(taxLabel, tax.map(p -> "Tax: $" + p.setScale(2, RoundingMode.HALF_UP)));

        Span totalLabel = new Span();
        MissingAPI.bindText(totalLabel, total.map(p -> "Total: $" + p.setScale(2, RoundingMode.HALF_UP)));
        totalLabel.getStyle().set("font-weight", "bold");
        totalLabel.getStyle().set("font-size", "1.5em");

        priceBreakdown.add(basePriceLabel, addOnsPriceLabel, subtotalLabel, discountLabel, taxLabel, totalLabel);

        add(serviceSelect, addOnsCheckbox, quantityField, discountField, taxRateField, priceBreakdown);
    }

    private BigDecimal getServicePrice(ServiceType service) {
        return switch (service) {
            case BASIC -> new BigDecimal("9.99");
            case STANDARD -> new BigDecimal("19.99");
            case PREMIUM -> new BigDecimal("39.99");
        };
    }

    private BigDecimal getAddOnPrice(AddOn addOn) {
        return switch (addOn) {
            case SUPPORT -> new BigDecimal("5.00");
            case BACKUP -> new BigDecimal("3.00");
            case ANALYTICS -> new BigDecimal("7.00");
            case CDN -> new BigDecimal("4.00");
        };
    }

    private DiscountCode validateDiscountCode(String code) {
        // Stub implementation - returns mock discount codes
        return switch (code.toUpperCase()) {
            case "SAVE10" -> new DiscountCode("SAVE10", new BigDecimal("10"));
            case "SAVE20" -> new DiscountCode("SAVE20", new BigDecimal("20"));
            case "HALF" -> new DiscountCode("HALF", new BigDecimal("50"));
            default -> null;
        };
    }
}
