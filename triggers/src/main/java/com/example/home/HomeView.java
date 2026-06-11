package com.example.home;

import com.example.common.BaseHomeView;
import com.example.uc1.CopyFieldValueView;
import com.example.uc10.CopyAndCountView;
import com.example.uc11.IdleWarningView;
import com.example.uc12.NetworkStatusView;
import com.example.uc13.CrossTabBroadcastView;
import com.example.uc14.LongPressDeleteView;
import com.example.uc15.ScrollIntoViewView;
import com.example.uc16.HighlightView;
import com.example.uc17.RightClickCoordsView;
import com.example.uc18.AccessibleSaveView;
import com.example.uc19.ClientFilterView;
import com.example.uc2.CopyCodeSnippetView;
import com.example.uc20.ResponsiveCardsView;
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
                        + "shortcut, a resize, an idle timeout, a cross-tab "
                        + "broadcast, …), reads zero or more Inputs, and runs "
                        + "one or more Actions — all inside the original "
                        + "browser handler. The later UCs demonstrate the "
                        + "extension SPI: custom Triggers, custom Actions, and "
                        + "custom Inputs you write against the public abstract "
                        + "classes.");

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
                "Subclass Action and emit JS via toJs — no @JsModule.",
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
        cards.add(homeCard("UC11", "Idle warning",
                "Custom IdleTrigger fires after 5s of no activity; SetSignalAction toggles a state badge.",
                IdleWarningView.class));
        cards.add(homeCard("UC12", "Network status",
                "Custom NetworkStatusTrigger + client-side action that reads navigator.onLine.",
                NetworkStatusView.class));
        cards.add(homeCard("UC13", "Cross-tab broadcast",
                "Custom BroadcastChannelTrigger forwards messages from other tabs into a signal.",
                CrossTabBroadcastView.class));
        cards.add(homeCard("UC14", "Long-press to delete",
                "Custom LongPressTrigger composes pointer events with a timer.",
                LongPressDeleteView.class));
        cards.add(homeCard("UC15", "Scroll into view",
                "Smallest custom Action — captures a target, calls scrollIntoView.",
                ScrollIntoViewView.class));
        cards.add(homeCard("UC16", "Highlight",
                "Custom Action with constructor-time colour and duration as JS captures.",
                HighlightView.class));
        cards.add(homeCard("UC17", "Right-click coords",
                "Custom PointInput returns {x,y}; Jackson decodes into a Point record server-side.",
                RightClickCoordsView.class));
        cards.add(homeCard("UC18", "Accessibility announce",
                "Custom AnnounceAction writes into an aria-live region for screen readers.",
                AccessibleSaveView.class));
        cards.add(homeCard("UC19", "Client-side filter",
                "Server renders the full list once; a custom action filters rows in JS.",
                ClientFilterView.class));
        cards.add(homeCard("UC20", "Responsive cards",
                "SizeTrigger + custom ClassByWidthAction swap a card grid between 1, 2, 3 columns.",
                ResponsiveCardsView.class));
        add(cards);
    }
}
