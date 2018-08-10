package com.heaton.funnyvote;

public interface BaseView<T extends BasePresenter> {
    void setPresenter(T presenter);
}
