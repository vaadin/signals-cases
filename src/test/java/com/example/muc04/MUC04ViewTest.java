package com.example.muc04;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.browserless.ViewPackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ViewPackages(classes = MUC04View.class)
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MUC04ViewTest extends SpringBrowserlessTest {

    @Autowired
    private MUC04Signals muc04Signals;

    private TextField getFieldByLabel(String label) {
        return $view(TextField.class).all().stream()
                .filter(f -> label.equals(f.getLabel())).findFirst()
                .orElseThrow();
    }

    @Test
    void viewRendersWithThreeEditableFields() {
        navigate(MUC04View.class);

        assertEquals(3, $view(TextField.class).all().size());
        assertTrue($view(Button.class).all().stream()
                .anyMatch(b -> "Save Changes".equals(b.getText())));
    }

    @Test
    void fieldsEnabledWhenNoLocks() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        $view(TextField.class).all()
                .forEach(f -> assertTrue(f.isEnabled(),
                        "Field '" + f.getLabel() + "' should be enabled"));
    }

    @Test
    void otherUserLockingFieldDisablesItForCurrentUser() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // Simulate User B locking the Company Name field
        muc04Signals.lockField("companyName", "otherUser", "otherSession");
        runPendingSignalsTasks();

        // Company Name should be disabled for the current user
        TextField companyNameField = getFieldByLabel("Company Name");
        assertFalse(companyNameField.isEnabled(),
                "Field locked by other user should be disabled");

        // Other fields should remain enabled
        assertTrue(getFieldByLabel("Address").isEnabled());
        assertTrue(getFieldByLabel("Phone Number").isEnabled());
    }

    @Test
    void lockShowsWhoIsEditing() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // Simulate User B locking the Address field
        muc04Signals.lockField("address", "otherUser", "otherSession");
        runPendingSignalsTasks();

        // Helper text should indicate who is editing
        TextField addressField = getFieldByLabel("Address");
        assertTrue(
                addressField.getHelperText() != null && addressField
                        .getHelperText().contains("otherUser"),
                "Helper text should show the locking user's name");
    }

    @Test
    void unlockingFieldReenablesIt() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // User B locks the field
        muc04Signals.lockField("phone", "otherUser", "otherSession");
        runPendingSignalsTasks();
        assertFalse(getFieldByLabel("Phone Number").isEnabled());

        // User B unlocks the field
        muc04Signals.unlockField("phone", "otherUser", "otherSession");
        runPendingSignalsTasks();

        assertTrue(getFieldByLabel("Phone Number").isEnabled(),
                "Field should be re-enabled after unlock");
    }

    @Test
    void editingFieldUpdatesSharedSignalValue() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // User A types a company name
        TextField companyNameField = getFieldByLabel("Company Name");
        test(companyNameField).setValue("Acme Corp");
        runPendingSignalsTasks();

        // The shared signal should reflect the value (visible to all users)
        assertEquals("Acme Corp", muc04Signals.getCompanyNameSignal().get());
    }

    @Test
    void sharedValueFromOtherUserAppearsInField() {
        navigate(MUC04View.class);
        runPendingSignalsTasks();

        // Simulate User B setting the address via the shared signal
        muc04Signals.getAddressSignal().set("123 Main St");
        runPendingSignalsTasks();

        // User A's view should show the updated value
        TextField addressField = getFieldByLabel("Address");
        assertEquals("123 Main St", addressField.getValue());
    }
}
