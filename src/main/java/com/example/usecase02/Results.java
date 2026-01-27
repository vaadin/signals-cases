package com.example.usecase02;

import com.vaadin.signals.local.ValueSignal;
import com.vaadin.signals.WritableSignal;

public class Results {
    WritableSignal<String> text = new ValueSignal<>("");
    WritableSignal<Boolean> visible = new ValueSignal<>(false);
}
