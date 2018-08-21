package com.heaton.funnyvote;

public interface BasePresenter<T> {

    void takeView(T view);

    void dropView();
}
