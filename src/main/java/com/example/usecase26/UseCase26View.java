package com.example.usecase26;

import jakarta.annotation.security.PermitAll;

import java.util.List;

import com.example.MissingAPI;
import com.example.views.MainLayout;
import org.jspecify.annotations.Nullable;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;

@Route(value = "use-case-26", layout = MainLayout.class)
@PageTitle("Use Case 26: Lazy Component Creation")
@Menu(order = 26, title = "UC 26: Lazy Creation")
@PermitAll
public class UseCase26View extends VerticalLayout {

    public UseCase26View() {
        setSpacing(true);
        setPadding(true);

        var title = new H2(
                "Use Case 26: Lazy Component Creation with International Address Form");

        var description = new Paragraph(
                "This use case demonstrates lazy component creation using bindVisible().onChange(). "
                        + "Address form fields are only instantiated when a country is first selected, "
                        + "avoiding upfront creation of heavy components like ComboBoxes with many items. "
                        + "US/Germany/UK use create-once-keep pattern; Japan uses create-and-destroy pattern.");

        // Signals
        var countrySignal = new ValueSignal<@Nullable Country>(null);
        Signal<Boolean> showUS = countrySignal.map(c -> c == Country.US);
        Signal<Boolean> showDE = countrySignal.map(c -> c == Country.GERMANY);
        Signal<Boolean> showJP = countrySignal.map(c -> c == Country.JAPAN);
        Signal<Boolean> showUK = countrySignal.map(c -> c == Country.UK);
        var creationLog = new ListSignal<String>();

        // Country selector
        var countrySelect = new ComboBox<Country>("Country", Country.values());
        countrySelect.setPlaceholder("Select a country...");
        countrySelect.bindValue(countrySignal, countrySignal::set);

        // Pattern A: Create once, keep (US)
        var usWrapper = new Div();
        MissingAPI.lazyPopulate(usWrapper, showUS, w -> {
            populateUSForm(w);
            creationLog.insertLast("Created US address form");
        });

        // Pattern A: Create once, keep (Germany)
        var deWrapper = new Div();
        MissingAPI.lazyPopulate(deWrapper, showDE, w -> {
            populateGermanForm(w);
            creationLog.insertLast("Created Germany address form");
        });

        // Pattern B: Create and destroy (Japan)
        var jpWrapper = new Div();
        MissingAPI.lazyPopulateRecreating(jpWrapper, showJP, w -> {
            populateJapanForm(w);
            creationLog.insertLast("Created Japan address form");
        }, w -> creationLog.insertLast("Destroyed Japan address form"));

        // Pattern A: Create once, keep (UK)
        var ukWrapper = new Div();
        MissingAPI.lazyPopulate(ukWrapper, showUK, w -> {
            populateUKForm(w);
            creationLog.insertLast("Created UK address form");
        });

        // Creation log panel
        var logPanel = new Div();
        logPanel.getStyle().set("background-color", "#f5f5f5")
                .set("padding", "1em").set("border-radius", "4px")
                .set("margin-top", "1em");
        logPanel.add(new H3("Creation Log"));
        var logEntries = new Div();
        logEntries.bindChildren(creationLog,
                entrySignal -> new Span(entrySignal.get()));
        logPanel.add(logEntries);

        add(title, description, countrySelect, usWrapper, deWrapper, jpWrapper,
                ukWrapper, logPanel);
    }

    private void populateUSForm(Div wrapper) {
        var layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(new H3("US Address"));
        layout.add(new TextField("Street"));
        layout.add(new TextField("Apt/Suite"));
        layout.add(new TextField("City"));
        var stateCombo = new ComboBox<String>("State", List.of("Alabama",
                "Alaska", "Arizona", "Arkansas", "California", "Colorado",
                "Connecticut", "Delaware", "Florida", "Georgia", "Hawaii",
                "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky",
                "Louisiana", "Maine", "Maryland", "Massachusetts", "Michigan",
                "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska",
                "Nevada", "New Hampshire", "New Jersey", "New Mexico",
                "New York", "North Carolina", "North Dakota", "Ohio",
                "Oklahoma", "Oregon", "Pennsylvania", "Rhode Island",
                "South Carolina", "South Dakota", "Tennessee", "Texas", "Utah",
                "Vermont", "Virginia", "Washington", "West Virginia",
                "Wisconsin", "Wyoming"));
        layout.add(stateCombo);
        layout.add(new TextField("ZIP"));
        wrapper.add(layout);
    }

    private void populateGermanForm(Div wrapper) {
        var layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(new H3("German Address"));
        layout.add(new TextField("Straße"));
        layout.add(new TextField("Hausnummer"));
        layout.add(new TextField("PLZ"));
        layout.add(new TextField("Ort"));
        var bundeslandCombo = new ComboBox<String>("Bundesland",
                List.of("Baden-Württemberg", "Bayern", "Berlin", "Brandenburg",
                        "Bremen", "Hamburg", "Hessen", "Mecklenburg-Vorpommern",
                        "Niedersachsen", "Nordrhein-Westfalen",
                        "Rheinland-Pfalz", "Saarland", "Sachsen",
                        "Sachsen-Anhalt", "Schleswig-Holstein", "Thüringen"));
        layout.add(bundeslandCombo);
        wrapper.add(layout);
    }

    private void populateJapanForm(Div wrapper) {
        var layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(new H3("Japan Address"));
        layout.add(new TextField("Postal Code"));
        var prefectureCombo = new ComboBox<String>("Prefecture",
                List.of("Hokkaido", "Aomori", "Iwate", "Miyagi", "Akita",
                        "Yamagata", "Fukushima", "Ibaraki", "Tochigi", "Gunma",
                        "Saitama", "Chiba", "Tokyo", "Kanagawa", "Niigata",
                        "Toyama", "Ishikawa", "Fukui", "Yamanashi", "Nagano",
                        "Gifu", "Shizuoka", "Aichi", "Mie", "Shiga", "Kyoto",
                        "Osaka", "Hyogo", "Nara", "Wakayama", "Tottori",
                        "Shimane", "Okayama", "Hiroshima", "Yamaguchi",
                        "Tokushima", "Kagawa", "Ehime", "Kochi", "Fukuoka",
                        "Saga", "Nagasaki", "Kumamoto", "Oita", "Miyazaki",
                        "Kagoshima", "Okinawa"));
        layout.add(prefectureCombo);
        layout.add(new TextField("City"));
        layout.add(new TextField("Ward"));
        layout.add(new TextField("Block"));
        wrapper.add(layout);
    }

    private void populateUKForm(Div wrapper) {
        var layout = new VerticalLayout();
        layout.setPadding(false);
        layout.add(new H3("UK Address"));
        layout.add(new TextField("Address Line 1"));
        layout.add(new TextField("Address Line 2"));
        layout.add(new TextField("Town/City"));
        layout.add(new TextField("County"));
        layout.add(new TextField("Postcode"));
        wrapper.add(layout);
    }
}
