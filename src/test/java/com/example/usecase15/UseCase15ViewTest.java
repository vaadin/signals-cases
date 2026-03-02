package com.example.usecase15;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = UseCase15View.class)
@WithMockUser
class UseCase15ViewTest extends SpringBrowserlessTest {

    @Test
    void viewRendersWithSearchField() {
        navigate(UseCase15View.class);

        assertTrue($view(TextField.class).all().stream()
                .anyMatch(f -> "Search Products".equals(f.getLabel())));
    }

    @Test
    void searchFieldPlaceholderPresent() {
        navigate(UseCase15View.class);

        TextField searchField = $view(TextField.class).all().stream()
                .filter(f -> "Search Products".equals(f.getLabel()))
                .findFirst().orElseThrow();
        assertEquals("Type to search...", searchField.getPlaceholder());
    }
}
