package com.heaton.funnyvote;

public interface BasePresenter {
    default void subscribe() {}
    default void unsubscribe() {}

    default void start() {
        subscribe();
    }
}
