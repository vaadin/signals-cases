package com.example.uc5;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.FullscreenSession;
import com.vaadin.flow.component.page.FullscreenSessionState;
import com.vaadin.flow.component.page.FullscreenState;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

/**
 * UC5 — Kiosk: visitor sign-in with PIN-protected staff exit.
 * <p>
 * A realistic kiosk: the page is fullscreened with
 * {@link com.vaadin.flow.component.page.Page#requestFullscreen()} (the
 * canonical use case for whole-document fullscreen — locking out browser
 * chrome on dedicated kiosk hardware). Inside, visitors sign in via a small
 * three-screen flow (landing → form → confirmation). A "Staff" button in the
 * kiosk header opens an inline PIN prompt; the correct PIN ({@value #STAFF_PIN})
 * calls {@link FullscreenSession#exit()} for an expected exit
 * ({@link FullscreenSessionState#EXITED_BY_CODE EXITED_BY_CODE}); pressing
 * Escape leaves unexpectedly ({@link FullscreenSessionState#EXITED_BY_USER
 * EXITED_BY_USER}) and surfaces a warning.
 * <p>
 * Developer chrome (heading, intro, control row, activity log) is hidden via
 * {@code bindVisible} while the page is fullscreen so the kiosk UI fills the
 * viewport like a real kiosk.
 */
@Route(value = "uc5", layout = MainLayout.class)
@Menu(order = 5, title = "UC5 — Kiosk")
public class KioskExitDetectionView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss");
    static final String STAFF_PIN = "1234";

    private enum Screen {
        LANDING, FORM, CONFIRMATION
    }

    private final ValueSignal<Screen> screenSignal = new ValueSignal<>(
            Screen.LANDING);
    private final ValueSignal<String> lastVisitorName = new ValueSignal<>("");
    private final ValueSignal<Boolean> staffPromptOpen = new ValueSignal<>(
            false);
    private final ValueSignal<String> staffError = new ValueSignal<>("");

    private final H1 heading = new H1("UC5 — Kiosk: visitor sign-in");
    private final Paragraph intro = new Paragraph(
            "Click ‘Enter kiosk’ to lock the page into a visitor sign-in "
                    + "flow with Page#requestFullscreen(). Inside, the Staff "
                    + "button opens a PIN prompt (PIN: " + STAFF_PIN + ") that "
                    + "calls FullscreenSession#exit() — an expected exit. "
                    + "Pressing Escape leaves unexpectedly and is recorded "
                    + "with a warning in the activity log.");
    private final HorizontalLayout controlsRow = new HorizontalLayout();
    private final Paragraph logHeading = new Paragraph("Activity log:");
    private final Div log = new Div();

    private final Span stateBadge = new Span();
    private final Span unexpectedWarning = new Span();
    private final Div stage = new Div();

    private final TextField nameField = new TextField("Your name");
    private final Select<Integer> partySizeField = new Select<>();
    private final Select<String> purposeField = new Select<>();
    private final PasswordField pinField = new PasswordField("Enter staff PIN");

    private @Nullable FullscreenSession session;

    public KioskExitDetectionView() {
        add(heading, intro);

        stateBadge.addClassName("status-badge");
        unexpectedWarning.addClassName("unexpected-warning");

        Button enter = new Button("Enter kiosk", e -> getUI().ifPresent(ui -> {
            unexpectedWarning.setText("");
            staffPromptOpen.set(false);
            staffError.set("");
            screenSignal.set(Screen.LANDING);
            FullscreenSession s = ui.getPage().requestFullscreen();
            session = s;
            bindSession(s);
        }));
        enter.addThemeVariants(ButtonVariant.PRIMARY);

        Button clear = new Button("Clear log", e -> log.removeAll());

        controlsRow.add(enter, clear, stateBadge, unexpectedWarning);
        controlsRow.setAlignItems(Alignment.CENTER);
        add(controlsRow);

        stage.addClassName("kiosk-stage");
        stage.add(buildKioskShell());
        add(stage);

        log.addClassName("kiosk-log");
        add(logHeading, log);
    }

    private Div buildKioskShell() {
        Div shell = new Div();
        shell.addClassName("kiosk-shell");

        Span title = new Span("Acme HQ Visitor Sign-In");
        title.addClassName("kiosk-title");
        Button staff = new Button("Staff", e -> {
            staffError.set("");
            pinField.clear();
            staffPromptOpen.set(true);
        });
        staff.addClassName("kiosk-staff-btn");
        HorizontalLayout header = new HorizontalLayout(title, staff);
        header.addClassName("kiosk-header");
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        Div content = new Div();
        content.addClassName("kiosk-content");

        Div landingPanel = buildLandingPanel();
        Div formPanel = buildFormPanel();
        Div confirmationPanel = buildConfirmationPanel();
        Div staffPanel = buildStaffPanel();

        landingPanel.bindVisible(screenSignal.map(s -> s == Screen.LANDING)
                .map(b -> b && !staffPromptOpen.get()));
        formPanel.bindVisible(screenSignal.map(s -> s == Screen.FORM)
                .map(b -> b && !staffPromptOpen.get()));
        confirmationPanel
                .bindVisible(screenSignal.map(s -> s == Screen.CONFIRMATION)
                        .map(b -> b && !staffPromptOpen.get()));
        staffPanel.bindVisible(staffPromptOpen);

        content.add(landingPanel, formPanel, confirmationPanel, staffPanel);
        shell.add(header, content);
        return shell;
    }

    private Div buildLandingPanel() {
        Div panel = new Div();
        panel.addClassName("kiosk-panel");
        H2 title = new H2("Welcome to Acme HQ");
        Paragraph subtitle = new Paragraph("Touch Start to sign in.");
        Button start = new Button("Start sign-in", e -> {
            nameField.clear();
            partySizeField.setValue(1);
            purposeField.clear();
            screenSignal.set(Screen.FORM);
        });
        start.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.LARGE);
        panel.add(title, subtitle, start);
        return panel;
    }

    private Div buildFormPanel() {
        Div panel = new Div();
        panel.addClassName("kiosk-panel");
        H2 title = new H2("Sign in");

        nameField.setRequiredIndicatorVisible(true);
        partySizeField.setLabel("Party size");
        partySizeField.setItems(1, 2, 3, 4, 5);
        partySizeField.setValue(1);
        purposeField.setLabel("Purpose of visit");
        purposeField.setItems("Meeting", "Interview", "Delivery", "Other");

        FormLayout form = new FormLayout(nameField, partySizeField,
                purposeField);

        Button cancel = new Button("Cancel",
                e -> screenSignal.set(Screen.LANDING));
        Button submit = new Button("Submit", e -> submitForm());
        submit.addThemeVariants(ButtonVariant.PRIMARY);
        HorizontalLayout actions = new HorizontalLayout(cancel, submit);
        actions.setJustifyContentMode(JustifyContentMode.END);

        panel.add(title, form, actions);
        return panel;
    }

    private Div buildConfirmationPanel() {
        Div panel = new Div();
        panel.addClassName("kiosk-panel");
        H2 title = new H2();
        title.bindText(lastVisitorName.map(n -> "Thanks, "
                + (n == null || n.isBlank() ? "visitor" : n) + "!"));
        Paragraph subtitle = new Paragraph(
                "Please take a seat. Reception will call you shortly.");
        Button newVisitor = new Button("New visitor",
                e -> screenSignal.set(Screen.LANDING));
        newVisitor.addThemeVariants(ButtonVariant.PRIMARY, ButtonVariant.LARGE);
        panel.add(title, subtitle, newVisitor);
        return panel;
    }

    private Div buildStaffPanel() {
        Div panel = new Div();
        panel.addClassName("kiosk-panel");
        panel.addClassName("kiosk-staff-panel");
        H2 title = new H2("Staff exit");
        Span error = new Span();
        error.addClassName("unexpected-warning");
        error.bindText(staffError);

        Button cancel = new Button("Cancel", e -> {
            staffPromptOpen.set(false);
            staffError.set("");
        });
        Button confirm = new Button("Confirm", e -> attemptStaffExit());
        confirm.addThemeVariants(ButtonVariant.PRIMARY);
        HorizontalLayout actions = new HorizontalLayout(cancel, confirm);
        actions.setJustifyContentMode(JustifyContentMode.END);

        panel.add(title, pinField, error, actions);
        return panel;
    }

    private void submitForm() {
        if (nameField.isEmpty()) {
            nameField.setInvalid(true);
            nameField.setErrorMessage("Name is required");
            return;
        }
        nameField.setInvalid(false);
        if (purposeField.getValue() == null) {
            purposeField.setInvalid(true);
            purposeField.setErrorMessage("Select a purpose");
            return;
        }
        purposeField.setInvalid(false);
        String name = nameField.getValue().trim();
        Integer partySize = partySizeField.getValue();
        String purpose = purposeField.getValue();
        lastVisitorName.set(name);
        appendLog("Signed in: " + name + " (party of " + partySize + ", "
                + purpose + ")", null);
        screenSignal.set(Screen.CONFIRMATION);
    }

    private void attemptStaffExit() {
        if (STAFF_PIN.equals(pinField.getValue())) {
            appendLog("Staff exit confirmed", null);
            staffPromptOpen.set(false);
            staffError.set("");
            FullscreenSession s = session;
            if (s != null) {
                s.exit();
            }
        } else {
            appendLog("Failed staff exit attempt", "unexpected");
            staffError.set("Incorrect PIN");
            pinField.clear();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        Signal<FullscreenState> fs = ui.getPage().fullscreenSignal();
        Signal<Boolean> isFullscreen = fs
                .map(s -> s == FullscreenState.FULLSCREEN);
        Signal<Boolean> notFullscreen = isFullscreen.map(b -> !b);

        stateBadge.bindText(fs.map(KioskExitDetectionView::badgeText));
        stateBadge.bindClassName("unsupported",
                fs.map(s -> s == FullscreenState.UNSUPPORTED));
        stage.bindClassName("live", isFullscreen);

        // Hide developer chrome while the kiosk is live so the kiosk UI
        // dominates the viewport like a real dedicated terminal.
        heading.bindVisible(notFullscreen);
        intro.bindVisible(notFullscreen);
        controlsRow.bindVisible(notFullscreen);
        logHeading.bindVisible(notFullscreen);
        log.bindVisible(notFullscreen);
    }

    private void bindSession(FullscreenSession s) {
        Signal.effect(this, () -> {
            FullscreenSessionState state = s.stateSignal().get();
            switch (state) {
            case ACTIVE -> appendLog("Entered kiosk mode", null);
            case EXITED_BY_USER -> {
                appendLog("Exit (UNEXPECTED — user pressed Escape)",
                        "unexpected");
                unexpectedWarning.setText("Kiosk exited unexpectedly!");
            }
            case EXITED_BY_CODE -> appendLog("Exit (expected)", null);
            case REJECTED -> appendLog("Request REJECTED: "
                    + s.error().orElse("no error message"), "unexpected");
            case PENDING -> {
                // initial state; nothing to log yet
            }
            }
        });
    }

    private void appendLog(String message, @Nullable String cls) {
        Div line = new Div();
        line.setText("[" + LocalTime.now().format(TIME) + "] " + message);
        if (cls != null) {
            line.addClassName(cls);
        }
        log.addComponentAsFirst(line);
    }

    private static String badgeText(FullscreenState state) {
        // FULLSCREEN: chrome (badge included) is hidden while kiosk is live,
        // so the FULLSCREEN-specific copy was invisible. Keep the idle text.
        return switch (state) {
        case FULLSCREEN, NOT_FULLSCREEN -> "Ready — click Enter kiosk to start";
        case UNSUPPORTED -> "Fullscreen not supported in this browser";
        case UNKNOWN -> "Detecting fullscreen support…";
        };
    }
}
