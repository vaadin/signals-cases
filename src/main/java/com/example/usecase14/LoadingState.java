package com.example.usecase14;

import org.jspecify.annotations.Nullable;

/**
 * Represents the state of an async operation
 */
public class LoadingState<T> {
    private State state;
    private @Nullable T data;
    private @Nullable String error;

    public enum State {
        IDLE, LOADING, SUCCESS, ERROR
    }

    // Default constructor for Jackson
    public LoadingState() {
        this.state = State.IDLE;
        this.data = null;
        this.error = null;
    }

    // Constructor for internal use
    private LoadingState(State state, @Nullable T data, @Nullable String error) {
        this.state = state;
        this.data = data;
        this.error = error;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setData(@Nullable T data) {
        this.data = data;
    }

    public void setError(@Nullable String error) {
        this.error = error;
    }

    public static <T> LoadingState<T> idle() {
        return new LoadingState<>(State.IDLE, null, null);
    }

    public static <T> LoadingState<T> loading() {
        return new LoadingState<>(State.LOADING, null, null);
    }

    public static <T> LoadingState<T> success(T data) {
        return new LoadingState<>(State.SUCCESS, data, null);
    }

    public static <T> LoadingState<T> error(String message) {
        return new LoadingState<>(State.ERROR, null, message);
    }

    public State getState() {
        return state;
    }

    public boolean isIdle() {
        return state == State.IDLE;
    }

    public boolean isLoading() {
        return state == State.LOADING;
    }

    public boolean isSuccess() {
        return state == State.SUCCESS;
    }

    public boolean isError() {
        return state == State.ERROR;
    }

    public @Nullable T getData() {
        return data;
    }

    public @Nullable String getError() {
        return error;
    }
}
