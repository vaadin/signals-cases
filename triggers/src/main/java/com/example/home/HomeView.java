package com.example.home;

import com.example.common.BaseHomeView;
import com.example.uc1.CopyFieldValueView;
import com.example.uc10.CopyAndCountView;
import com.example.uc2.CopyCodeSnippetView;
import com.example.uc3.CopySelectValueView;
import com.example.uc4.ShareUrlView;
import com.example.uc5.CustomActionView;
import com.example.uc6.ShortcutCopyView;
import com.example.uc7.SubmitAndDisableView;
import com.example.uc8.LiveSignalCounterView;
import com.example.uc9.JsTriggerView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, title = "Home")
public class HomeView extends BaseHomeView {

    public HomeView() {
        super("Trigger / Action API — use cases",
                "Each card below exercises one use case of the "
                        + "com.vaadin.flow.component.trigger.internal API. A "
                        + "Trigger fires on the client (a click, a keyboard "
                        + "shortcut, a double-click, …), reads zero or more Inputs "
                        + "(DOM properties, server-side Signals, JS expressions, "
                        + "literals), and runs one or more Actions — all inside "
                        + "the original user-gesture handler so gesture-gated "
                        + "browser APIs (clipboard, fullscreen, share) work "
                        + "without a server round-trip. Server-side mirrors and "
                        + "callbacks land afterwards over the regular Flow channel.");

        Div cards = new Div();
        cards.addClassName("home-cards");
        cards.add(homeCard("UC1", "Copy field value",
                "Click a button, copy the current value of a text field.",
                CopyFieldValueView.class));
        cards.add(homeCard("UC2", "Copy code snippet",
                "Copy a <pre> block's textContent — PropertyInput works on any element.",
                CopyCodeSnippetView.class));
        cards.add(homeCard("UC3", "Copy from a select",
                "Read the currently-selected option's value via PropertyInput.",
                CopySelectValueView.class));
        cards.add(homeCard("UC4", "Share-URL widget",
                "Server-side ValueSignal copied via SignalInput — no round-trip.",
                ShareUrlView.class));
        cards.add(homeCard("UC5", "Custom action",
                "Subclass Action and emit JS via appendStatement — no @JsModule.",
                CustomActionView.class));
        cards.add(homeCard("UC6", "Shortcut copy",
                "Ctrl/Cmd-C fires a (local) ShortcutTrigger that copies a field value.",
                ShortcutCopyView.class));
        cards.add(homeCard("UC7", "Submit + disable",
                "Enter clicks Send via a synthetic ClickAction, then disables it.",
                SubmitAndDisableView.class));
        cards.add(homeCard("UC8", "Live signal",
                "Counter mutates server-side; SignalInput mirrors on change.",
                LiveSignalCounterView.class));
        cards.add(homeCard("UC9", "Double-click copy",
                "DoubleClickTrigger fires WriteToClipboardAction on dblclick.",
                JsTriggerView.class));
        cards.add(homeCard("UC10", "Server callback",
                "Click copies AND runs a server callback via WriteToClipboardAction's onCopied.",
                CopyAndCountView.class));
        add(cards);
    }
}
