package com.example.usecase04;

import jakarta.annotation.security.PermitAll;

import java.util.List;

import com.example.views.MainLayout;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

@Route(value = "use-case-04", layout = MainLayout.class)
@PageTitle("Use Case 4: Filtered and Sorted Data Grid")
@Menu(order = 4, title = "UC 4: Filtered Data Grid")
@PermitAll
public class UseCase04View extends VerticalLayout {

    public UseCase04View() {
        setSpacing(true);
        setPadding(true);

        H2 title = new H2("Use Case 4: Filtered and Sorted Data Grid");

        Paragraph description = new Paragraph(
                "This use case demonstrates a Grid with DataView-based filtering. "
                        + "Filter by category, search by name/ID, and toggle in-stock only. "
                        + "Signals are used to bind filter controls, while DataView handles the actual filtering. "
                        + "This shows proper integration of signals with Vaadin's DataView API.");

        // Create signals for filter inputs
        ValueSignal<String> categoryFilterSignal = new ValueSignal<>("All");
        ValueSignal<String> searchTermSignal = new ValueSignal<>("");
        ValueSignal<Boolean> inStockOnlySignal = new ValueSignal<>(false);

        // Load all products and set up Grid with DataView
        List<Product> allProducts = loadProducts();

        // Data grid with ListDataView for filtering
        Grid<Product> grid = new Grid<>(Product.class);
        grid.setColumns("id", "name", "category", "price", "stock");

        // Use setItems to get a GridListDataView for filtering
        GridListDataView<Product> dataView = grid.setItems(allProducts);

        // Set up filtering using DataView - filters are applied based on signal
        // values
        dataView.setFilter(product -> {
            String category = categoryFilterSignal.peek();
            String searchTerm = searchTermSignal.peek().toLowerCase();
            boolean inStockOnly = inStockOnlySignal.peek();

            boolean categoryMatch = category.equals("All")
                    || product.category().equals(category);
            boolean searchMatch = searchTerm.isEmpty()
                    || product.name().toLowerCase().contains(searchTerm)
                    || product.id().toLowerCase().contains(searchTerm);
            boolean stockMatch = !inStockOnly || product.stock() > 0;

            return categoryMatch && searchMatch && stockMatch;
        });

        // Filter UI components with signal bindings
        ComboBox<String> categoryFilter = new ComboBox<>("Category", List.of(
                "All", "Electronics", "Clothing", "Books", "Home & Garden"));
        categoryFilter.bindValue(categoryFilterSignal,
                categoryFilterSignal::set);

        TextField searchField = new TextField("Search");
        searchField.setPlaceholder("Search by name or ID");
        searchField.bindValue(searchTermSignal, searchTermSignal::set);

        Checkbox inStockCheckbox = new Checkbox("Show in-stock items only");
        inStockCheckbox.bindValue(inStockOnlySignal, inStockOnlySignal::set);

        // Use Signal.effect to refresh DataView when any filter signal changes
        Signal.effect(grid, () -> {
            // Read all signal values to register dependencies
            categoryFilterSignal.get();
            searchTermSignal.get();
            inStockOnlySignal.get();
            // Refresh the DataView to reapply filters
            dataView.refreshAll();
        });

        add(title, description, categoryFilter, searchField, inStockCheckbox,
                grid);
    }

    private List<Product> loadProducts() {
        // Stub implementation - returns mock data
        return List.of(new Product("P001", "Laptop", "Electronics", 999.99, 15),
                new Product("P002", "T-Shirt", "Clothing", 19.99, 50),
                new Product("P003", "Java Programming Book", "Books", 49.99, 0),
                new Product("P004", "Garden Hose", "Home & Garden", 29.99, 30),
                new Product("P005", "Wireless Mouse", "Electronics", 25.99, 0),
                new Product("P006", "Jeans", "Clothing", 59.99, 20),
                new Product("P007", "Fiction Novel", "Books", 14.99, 100),
                new Product("P008", "Plant Pot", "Home & Garden", 12.99, 45),
                new Product("P009", "Keyboard", "Electronics", 79.99, 8),
                new Product("P010", "Winter Jacket", "Clothing", 129.99, 5));
    }
}
