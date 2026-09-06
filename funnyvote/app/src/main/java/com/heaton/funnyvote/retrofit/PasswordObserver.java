package com.heaton.funnyvote.retrofit;

import io.reactivex.observers.DisposableObserver;

public abstract class PasswordObserver<T> extends DisposableObserver<T> {
    public String errorMessage = "";

    @Override
    public void onComplete() {
    }

    @Override
    public void onError(Throwable e) {
        if (e != null && ("error_invalid_password".equals(e.getMessage()) ||
                (e.getMessage() != null && e.getMessage().contains("error_invalid_password")))) {
            onPasswordInValid();
        } else {
            onFailure(e);
        }
    }

    @Override
    public void onNext(T t) {
        onSuccess(t);
    }

    public abstract void onFailure(Throwable e);
    public abstract void onSuccess(T o);
    public abstract void onPasswordInValid();
}
