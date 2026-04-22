package com.example.home;

import com.example.uc1.OneShotOnClickView;
import com.example.uc2.TrackingView;
import com.example.uc3.AutoFetchView;
import com.example.uc4.DenialView;
import com.example.uc5.DetailedDataView;
import com.example.uc6.OptionsView;
import com.example.uc7.FormFieldView;
import com.example.views.MainLayout;

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
        add(new H1("Geolocation API — use cases"));
        add(new Paragraph(
                "Each link below exercises one use case of the Vaadin Flow Geolocation API."));

        UnorderedList list = new UnorderedList();
        list.add(item("UC1", "One-shot request on user click",
                OneShotOnClickView.class));
        list.add(item("UC2", "Continuous tracking with reactive signal",
                TrackingView.class));
        list.add(item("UC3", "Auto-fetch on view load, gated on permission",
                AutoFetchView.class));
        list.add(item("UC4", "Handling denial, failure and unavailability",
                DenialView.class));
        list.add(item("UC5", "Reading detailed position data",
                DetailedDataView.class));
        list.add(item("UC6", "Tuning precision, freshness and battery",
                OptionsView.class));
        list.add(item("UC7", "Capturing a location as part of a form",
                FormFieldView.class));
        add(list);
    }

    private ListItem item(String tag, String description,
            Class<? extends com.vaadin.flow.component.Component> target) {
        ListItem li = new ListItem();
        li.add(new Div(new RouterLink(tag + " — " + description, target)));
        return li;
    }
}
