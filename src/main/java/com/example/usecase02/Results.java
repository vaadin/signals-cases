package com.example.usecase02;

import com.vaadin.flow.signals.local.ValueSignal;

public class Results {
    ValueSignal<String> text = new ValueSignal<>("");
    ValueSignal<Boolean> visible = new ValueSignal<>(false);
}
