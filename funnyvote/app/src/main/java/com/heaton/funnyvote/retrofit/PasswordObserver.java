package com.heaton.funnyvote.retrofit;

import android.text.TextUtils;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import rx.Observer;

public abstract class PasswordObserver<T> implements Observer<T> {
    public String errorMessage = "";
    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof HttpException) {
            ResponseBody body = ((HttpException) e).response().errorBody();
            try {
                errorMessage = body.string();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(errorMessage) && errorMessage.equals("error_invalid_password")) {
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
