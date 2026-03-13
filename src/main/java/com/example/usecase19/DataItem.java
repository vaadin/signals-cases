package com.example.usecase19;

import com.example.usecase14.LoadingState;
import org.jspecify.annotations.Nullable;

public record DataItem(String id, String name, LoadingState.State state,
        @Nullable String data, @Nullable String error, int simulatedDelayMs) {
}
