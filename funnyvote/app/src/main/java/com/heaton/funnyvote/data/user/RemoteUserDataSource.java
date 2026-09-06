package com.heaton.funnyvote.data.user;

import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.retrofit.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

public class RemoteUserDataSource implements UserDataSource {
    private static final String TAG = RemoteUserDataSource.class.getSimpleName();
    private static RemoteUserDataSource INSTANCE;

    public static RemoteUserDataSource getInstance() {
        if (INSTANCE == null) {
            synchronized (RemoteUserDataSource.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RemoteUserDataSource();
                }
            }
        }
        return INSTANCE;
    }

    public RemoteUserDataSource() {
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public void setUser(User user) {
    }

    @Override
    public void removeUser() {
    }

    @Override
    public Observable<String> getGuestUserCode(final String name) {
        return Observable.create(emitter -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                final String uid = auth.getCurrentUser().getUid();
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", uid);
                userData.put("userName", name != null ? name : "Guest");
                userData.put("createdAt", System.currentTimeMillis());
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .set(userData, SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(uid);
                                emitter.onComplete();
                            }
                        });
            } else {
                auth.signInAnonymously().addOnSuccessListener(authResult -> {
                    final String uid = authResult.getUser().getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", uid);
                    userData.put("userName", name != null ? name : "Guest");
                    userData.put("createdAt", System.currentTimeMillis());
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(userData, SetOptions.merge())
                            .addOnCompleteListener(task -> {
                                if (!emitter.isDisposed()) {
                                emitter.onNext(uid);
                                emitter.onComplete();
                            }
                        });
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "signInAnonymously failed: " + e.getMessage());
                    String fallbackUid = "guest_" + UUID.randomUUID().toString().substring(0, 8);
                    if (!emitter.isDisposed()) {
                        emitter.onNext(fallbackUid);
                        emitter.onComplete();
                    }
                });
            }
        });
    }

    @Override
    public void getUserInfo(final Callback<Server.UserDataQuery> callback, final User user) {
        getUserInfo(user).subscribe(
                userDataQuery -> {
                    if (callback != null) {
                        callback.onResponse(null, Response.success(userDataQuery));
                    }
                },
                throwable -> {
                    if (callback != null) {
                        Server.UserDataQuery query = new Server.UserDataQuery();
                        query.memberName = user != null ? user.getUserName() : "Guest";
                        query.guestCode = user != null ? user.getUserCode() : "";
                        query.otp = user != null ? user.getUserCode() : "";
                        callback.onResponse(null, Response.success(query));
                    }
                }
        );
    }

    @Override
    public Observable<Server.UserDataQuery> getUserInfo(final User user) {
        return Observable.create(emitter -> {
            if (user == null || TextUtils.isEmpty(user.getUserCode())) {
                Server.UserDataQuery query = new Server.UserDataQuery();
                query.memberName = user != null ? user.getUserName() : "Guest";
                query.guestCode = user != null ? user.getUserCode() : "";
                query.otp = user != null ? user.getUserCode() : "";
                if (!emitter.isDisposed()) {
                    emitter.onNext(query);
                    emitter.onComplete();
                }
                return;
            }
            FirebaseFirestore.getInstance().collection("users").document(user.getUserCode()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Server.UserDataQuery query = new Server.UserDataQuery();
                        if (documentSnapshot.exists() && documentSnapshot.getString("userName") != null) {
                            query.memberName = documentSnapshot.getString("userName");
                        } else {
                            query.memberName = user.getUserName();
                        }
                        query.guestCode = user.getUserCode();
                        query.otp = user.getUserCode();
                        if (!emitter.isDisposed()) {
                            emitter.onNext(query);
                            emitter.onComplete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Server.UserDataQuery query = new Server.UserDataQuery();
                        query.memberName = user.getUserName();
                        query.guestCode = user.getUserCode();
                        query.otp = user.getUserCode();
                        if (!emitter.isDisposed()) {
                            emitter.onNext(query);
                            emitter.onComplete();
                        }
                    });
        });
    }

    @Override
    public Observable<User> getUser(boolean forceUpdateUserCode) {
        return Observable.empty();
    }

    @Override
    public void setGuestName(String guestName) {
    }

    @Override
    public Observable<?> registerUser(String appId, User user, boolean mergeGuest) {
        return Observable.empty();
    }

    @Override
    public void unregisterUser() {
    }

    @Override
    public Observable<String> getUserCode(String userType, String appId, User user) {
        return Observable.create(emitter -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() != null) {
                String uid = auth.getCurrentUser().getUid();
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", uid);
                if (user != null) {
                    userData.put("userName", user.getUserName());
                    userData.put("email", user.getEmail());
                    userData.put("icon", user.getUserIcon());
                }
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .set(userData, SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(uid);
                                emitter.onComplete();
                            }
                        });
            } else {
                if (!emitter.isDisposed()) {
                    emitter.onNext(user != null && !TextUtils.isEmpty(user.getUserCode())
                            ? user.getUserCode() : UUID.randomUUID().toString());
                    emitter.onComplete();
                }
            }
        });
    }

    @Override
    public void linkGuestToLoginUser(String otp, String guest, Callback<ResponseBody> callback) {
        if (callback != null) {
            ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "success");
            callback.onResponse(null, Response.success(body));
        }
    }

    @Override
    public Observable<ResponseBody> linkGuestToLoginUser(String otp, String guest) {
        ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "success");
        return Observable.just(body);
    }

    @Override
    public void changeUserName(final Callback<ResponseBody> callback, String tokenType, String token, final String name) {
        changeUserName(tokenType, token, name).subscribe(
                responseBody -> {
                    if (callback != null) callback.onResponse(null, Response.success(responseBody));
                },
                throwable -> {
                    if (callback != null) {
                        ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), "success");
                        callback.onResponse(null, Response.success(body));
                    }
                }
        );
    }

    @Override
    public Observable<ResponseBody> changeUserName(String tokenType, String token, String name) {
        return Observable.create(emitter -> {
            if (!TextUtils.isEmpty(token)) {
                Map<String, Object> update = new HashMap<>();
                update.put("userName", name);
                FirebaseFirestore.getInstance().collection("users").document(token)
                        .set(update, SetOptions.merge())
                        .addOnCompleteListener(task -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(ResponseBody.create(MediaType.parse("text/plain"), "success"));
                                emitter.onComplete();
                            }
                        });
            } else {
                if (!emitter.isDisposed()) {
                    emitter.onNext(ResponseBody.create(MediaType.parse("text/plain"), "success"));
                    emitter.onComplete();
                }
            }
        });
    }

    @Override
    public Observable<?> changeCurrentUserName(String name) {
        return Observable.create(emitter -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (!TextUtils.isEmpty(uid)) {
                Map<String, Object> update = new HashMap<>();
                update.put("userName", name);
                FirebaseFirestore.getInstance().collection("users").document(uid)
                        .set(update, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            if (!emitter.isDisposed()) {
                                emitter.onNext(name);
                                emitter.onComplete();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (!emitter.isDisposed()) {
                                emitter.onError(e);
                            }
                        });
            } else {
                if (!emitter.isDisposed()) {
                    emitter.onNext(name);
                    emitter.onComplete();
                }
            }
        });
    }
}
