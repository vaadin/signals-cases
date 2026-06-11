package com.example.uc3;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.dom.Element;

/**
 * Tiny {@link Component} wrapping a native {@code <select>} element so the
 * {@code com.vaadin.flow.component.trigger.internal.PropertyInput} can read
 * its {@code value} property (PropertyInput only accepts a Component, not a
 * raw Element).
 */
@Tag("select")
class NativeSelect extends Component {

    NativeSelect addOption(String value, String label) {
        Element opt = new Element("option");
        opt.setAttribute("value", value);
        opt.setText(label);
        getElement().appendChild(opt);
        return this;
    }
}
