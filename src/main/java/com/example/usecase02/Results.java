package com.example.usecase02;

import com.vaadin.flow.signals.WritableSignal;
import com.vaadin.flow.signals.local.ValueSignal;

public class Results {
    WritableSignal<String> text = new ValueSignal<>("");
    WritableSignal<Boolean> visible = new ValueSignal<>(false);
}
