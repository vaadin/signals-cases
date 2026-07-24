package com.example.uc6;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.clipboard.ClipboardSimulator;
import com.vaadin.flow.component.contextmenu.ContextMenuTester;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ViewPackages(classes = CopyFromContextMenuView.class)
class CopyFromContextMenuViewTest extends SpringBrowserlessTest {

    @Test
    void clickingMenuItem_writesTokenToClipboard() {
        CopyFromContextMenuView view = navigate(CopyFromContextMenuView.class);

        ContextMenuTester<?> menu = test(view.menu);
        menu.open();
        menu.clickItem("Copy value");

        assertEquals(CopyFromContextMenuView.SECRET_TOKEN,
                ClipboardSimulator.current().text());
    }
}
