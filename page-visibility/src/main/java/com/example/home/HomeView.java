package com.example.home;

import com.example.uc1.UpdateWhenActiveView;
import com.example.uc2.PresenceAvatarsView;
import com.example.uc3.NotificationGatingView;
import com.example.uc4.RefreshStaleDataView;
import com.example.views.MainLayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, title = "Home")
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("Page Visibility API — use cases"));
        add(new Paragraph(
                "Each link below exercises one use case of Page#pageVisibilitySignal(). "
                        + "The signal reports VISIBLE, VISIBLE_NOT_FOCUSED, HIDDEN or UNKNOWN "
                        + "and reactively notifies the server whenever the user's tab "
                        + "visibility or focus changes."));

        UnorderedList list = new UnorderedList();
        list.add(item("UC1",
                "Update when active — pause server work while hidden",
                UpdateWhenActiveView.class));
        list.add(item("UC2", "Presence / \"away\" status across browsers",
                PresenceAvatarsView.class));
        list.add(item("UC3", "Notification gating with Web Push",
                NotificationGatingView.class));
        list.add(item("UC4", "Refresh stale data when user returns",
                RefreshStaleDataView.class));
        add(list);
    }

    private ListItem item(String tag, String description,
            Class<? extends Component> target) {
        ListItem li = new ListItem();
        li.add(new Div(new RouterLink(tag + " — " + description, target)));
        return li;
    }
}
