package com.example.usecase19;

import org.jspecify.annotations.Nullable;

public record DataItem(String id, String name, LoadingState state,
        @Nullable String data, @Nullable String error, int simulatedDelayMs) {
}
