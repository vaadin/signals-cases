package com.example.views;

import com.example.MissingAPI;


// Note: This code uses the proposed Signal API and will not compile yet

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.signals.Signal;
import com.vaadin.signals.WritableSignal;
import com.vaadin.signals.ValueSignal;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "use-case-07", layout = MainLayout.class)
@PageTitle("Use Case 7: Progressive Disclosure with Nested Conditions")
@Menu(order = 31, title = "UC 7: Nested Conditions")
public class UseCase07View extends VerticalLayout {

    enum VisaType { H1B, L1, O1, OTHER }

    public UseCase07View() {
        // Base question: needs visa sponsorship
        WritableSignal<Boolean> needsVisaSignal = new ValueSignal<>(false);
        Checkbox needsVisaCheckbox = new Checkbox("Do you require visa sponsorship?");
        MissingAPI.bindValue(needsVisaCheckbox, needsVisaSignal);

        // Level 1: Visa-related fields (shown when needsVisa is true)
        VerticalLayout visaSection = new VerticalLayout();
        WritableSignal<VisaType> visaTypeSignal = new ValueSignal<>(VisaType.H1B);

        ComboBox<VisaType> visaTypeSelect = new ComboBox<>("Visa Type", VisaType.values());
        visaTypeSelect.setValue(VisaType.H1B);
        MissingAPI.bindValue(visaTypeSelect, visaTypeSignal);

        TextField currentVisaStatus = new TextField("Current Visa Status");

        visaSection.add(visaTypeSelect, currentVisaStatus);
        visaSection.bindVisible(needsVisaSignal);

        // Level 2: H1-B specific fields (shown when visa type is H1B)
        VerticalLayout h1bSection = new VerticalLayout();
        WritableSignal<Boolean> hasH1BPreviouslySignal = new ValueSignal<>(false);

        Checkbox hasH1BPreviouslyCheckbox = new Checkbox("Have you held an H1-B visa before?");
        MissingAPI.bindValue(hasH1BPreviouslyCheckbox, hasH1BPreviouslySignal);

        TextField h1bSpecialtyOccupation = new TextField("Specialty Occupation");

        h1bSection.add(hasH1BPreviouslyCheckbox, h1bSpecialtyOccupation);
        h1bSection.bindVisible(Signal.computed(() ->
            needsVisaSignal.value() && visaTypeSignal.value() == VisaType.H1B
        ));

        // Level 3: Previous H1-B details (shown when has H1-B previously)
        VerticalLayout previousH1BSection = new VerticalLayout();
        previousH1BSection.add(
            new TextField("Previous Employer"),
            new TextField("Previous Petition Number"),
            new TextField("Previous H1-B Start Date")
        );
        previousH1BSection.bindVisible(Signal.computed(() ->
            needsVisaSignal.value() &&
            visaTypeSignal.value() == VisaType.H1B &&
            hasH1BPreviouslySignal.value()
        ));

        // Level 2: L1 specific fields (shown when visa type is L1)
        VerticalLayout l1Section = new VerticalLayout();
        l1Section.add(
            new TextField("Parent Company Name"),
            new TextField("Years with Parent Company"),
            new ComboBox<>("L1 Category", List.of("L1-A (Manager)", "L1-B (Specialized Knowledge)"))
        );
        l1Section.bindVisible(Signal.computed(() ->
            needsVisaSignal.value() && visaTypeSignal.value() == VisaType.L1
        ));

        // Level 2: O1 specific fields (shown when visa type is O1)
        VerticalLayout o1Section = new VerticalLayout();
        o1Section.add(
            new TextField("Field of Extraordinary Ability"),
            new TextField("Major Awards/Recognition"),
            new TextField("Publications or Media Coverage")
        );
        o1Section.bindVisible(Signal.computed(() ->
            needsVisaSignal.value() && visaTypeSignal.value() == VisaType.O1
        ));

        add(needsVisaCheckbox, visaSection, h1bSection, previousH1BSection, l1Section, o1Section);
    }
}
