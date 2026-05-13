package com.example.uc5;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.views.MainLayout;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.component.page.WebShareSupport;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

/**
 * UC5 — Share with completion feedback.
 * <p>
 * The native share sheet has three possible outcomes: the user picks a target
 * (success), the user dismisses the sheet ({@code AbortError}), or the share
 * fails for some other reason. Flow already logs rejection at debug level
 * inside {@link Page#share(String, String, String)}, but if the UI wants to
 * react — show a thank-you toast, or retry the share — the
 * {@link PendingJavaScriptResult} returned from {@code share()} is the right
 * handle. This view attaches {@code .then(ok, err)} and surfaces every
 * outcome in a small log.
 */
@Route(value = "uc5", layout = MainLayout.class)
@Menu(order = 5, title = "UC5 — Completion feedback")
public class ShareFeedbackView extends VerticalLayout {

    private static final DateTimeFormatter TIME = DateTimeFormatter
            .ofPattern("HH:mm:ss");

    private final Button shareButton = new Button("Share with feedback",
            VaadinIcon.SHARE.create());
    private final Div log = new Div();

    public ShareFeedbackView() {
        add(new H1("UC5 — Share with completion feedback"));
        add(new Paragraph("Hooks .then(ok, err) on the PendingJavaScriptResult "
                + "returned by Page.share(...). The browser resolves the "
                + "promise on success and rejects with an AbortError when "
                + "the user dismisses the sheet — we surface both in the "
                + "log below. On desktop you can fake either branch with "
                + "the buttons in the next section."));

        shareButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(shareButton, new H2("Outcome log"));

        log.addClassName("share-feedback-log");
        add(log);
        appendLog("(no share invoked yet)");

        add(new H2("Simulate without a browser"));
        Button fakeOk = new Button("Simulate success",
                e -> handleSuccess());
        Button fakeCancel = new Button("Simulate cancel",
                e -> handleError("AbortError: Share canceled"));
        Button fakeFail = new Button("Simulate failure",
                e -> handleError("NotAllowedError: Permission denied"));
        add(new Div(fakeOk, fakeCancel, fakeFail));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        Page page = attachEvent.getUI().getPage();
        Signal<WebShareSupport> support = page.shareSupportSignal();

        Signal.effect(this, () -> shareButton
                .setEnabled(support.get() == WebShareSupport.SUPPORTED));

        shareButton.addClickListener(e -> {
            PendingJavaScriptResult result = page.share(
                    "Web Share completion feedback demo",
                    "Try cancelling the share sheet to see the AbortError path.",
                    "https://vaadin.com/");
            result.then(ok -> handleSuccess(), err -> handleError(err));
        });
    }

    // Package-private outcome handlers so a browserless test can drive the
    // log without an actual share dialog.
    void handleSuccess() {
        appendLog(now() + "  ✓ shared");
    }

    void handleError(String error) {
        appendLog(now() + "  ✗ " + error);
    }

    private static String now() {
        return LocalTime.now().format(TIME);
    }

    private void appendLog(String line) {
        Div entry = new Div();
        entry.setText(line);
        log.addComponentAsFirst(entry);
    }
}
