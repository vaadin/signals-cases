package com.example.usecase19;

import org.jspecify.annotations.Nullable;

import com.example.usecase14.LoadingState;

public record DataItem(String id, String name, LoadingState.State state,
        @Nullable String data, @Nullable String error, int simulatedDelayMs) {
}
