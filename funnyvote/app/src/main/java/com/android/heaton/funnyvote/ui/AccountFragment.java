package com.android.heaton.funnyvote.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.retrofit.Server;
import com.bumptech.glide.Glide;
import com.bumptech.glide.util.ExceptionCatchingInputStream;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by chiu_mac on 2016/10/28.
 */

public class AccountFragment extends android.support.v4.app.Fragment
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = AccountFragment.class.getSimpleName();
    private static final int RC_GOOGLE_SIGN_IN = 101;

    //Facebook API
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private GraphRequest.Callback facebookProfileCallback = new GraphRequest.Callback() {
        @Override
        public void onCompleted(GraphResponse response) {
            try {
                JSONObject profile = response.getJSONObject();
                String name = profile.getString("name");
                String facebookID = profile.getString("id");
                String email = profile.has("email") ? profile.getString("email") : null;

                String link = null;
                if (profile.has("picture")) {
                    JSONObject picture = profile.getJSONObject("picture");
                    link = picture.getJSONObject("data").getString("url");
                }
                User user = new User(null, name, email, facebookID,
                        null, link, User.TYPE_FACEBOOK);
                addNewFBUser(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //Google Sign in API
    private GoogleApiClient googleApiClient;

    //Retrofit
    Retrofit retrofit;
    Server.UserService userService;

    //Views
    ImageView picImageView;
    TextView nameTextView;
    View googleSignInBtn;
    Button signoutBtn;
    Button facebookLoginBtn;
    ProgressBar loadingProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL).build();
        userService = retrofit.create(Server.UserService.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_account, null);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess");
                handleFacebookLogIn();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError:" + error.getMessage());
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "onCurrentAccessTokenChanged");
                if (currentAccessToken == null) {
                    removeUserProfile();
                }
            }
        };

        //Views
        facebookLoginBtn = (Button) view.findViewById(R.id.fb_login_button);
        facebookLoginBtn.setOnClickListener(this);
        nameTextView = (TextView) view.findViewById(R.id.profile_name);
        nameTextView.setOnClickListener(this);
        picImageView = (ImageView) view.findViewById(R.id.profile_picture);
        googleSignInBtn = view.findViewById(R.id.google_sign_in_button);
        googleSignInBtn.setOnClickListener(this);
        signoutBtn = (Button) view.findViewById(R.id.sign_out_button);
        signoutBtn.setOnClickListener(this);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_progress_bar);

        updateUI();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        accessTokenTracker.stopTracking();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIn(result);
        }
    }

    private void handleFacebookLogIn() {
        Bundle params = new Bundle();
        params.putString("fields", "email,name,picture.type(large)");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                facebookProfileCallback
        ).executeAsync();
    }

    private void handleGoogleSignIn(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully
            GoogleSignInAccount googleAccount = result.getSignInAccount();
            String name = googleAccount.getDisplayName();
            String googleID = googleAccount.getId();
            String email = googleAccount.getEmail();
            Uri picLink = googleAccount.getPhotoUrl();
            User user = new User(null, name, email, googleID, null, picLink.toString(), User.TYPE_GOOGLE);
            Log.d(TAG, "name:" + name);
            Log.d(TAG, "google ID:" + googleID);
            Log.d(TAG, "email:" + email);
            Log.d(TAG, "pic link:" + picLink.toString());
            addNewGoogleUser(user);
        }
    }

    private void saveUserProfile(User user) {
        Log.d(TAG, "saveUserProfile");
        SharedPreferences userPref = UserSharepreferenceController.getUserSp(getContext());
        if (userPref.getString(UserSharepreferenceController.KEY_TYPE, User.TYPE_GUEST).equals(User.TYPE_GUEST)) {
            DataLoader.getInstance(getContext()).linkTempUserToLoginUser(
                    userPref.getString(UserSharepreferenceController.KEY_USER_ID, ""), user);
        }
        SharedPreferences.Editor spEditor = userPref.edit();
        spEditor.putString(UserSharepreferenceController.KEY_NAME, user.getUserName());
        spEditor.putString(UserSharepreferenceController.KEY_USER_ID, user.getUserID());
        spEditor.putString(UserSharepreferenceController.KEY_TYPE, user.getType());
        spEditor.putString(UserSharepreferenceController.KEY_ICON, user.getUserIcon());
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            spEditor.putString(UserSharepreferenceController.KEY_EMAIL, user.getEmail());
        }
        spEditor.commit();
    }

    private void removeUserProfile() {
        UserSharepreferenceController.removeUser(getContext());
        addNewGuestUser();
    }

    private void updateUI() {
        SharedPreferences userPref = UserSharepreferenceController.getUserSp(getContext());
        if (userPref.contains(UserSharepreferenceController.KEY_NAME)) {
            String name = userPref.getString(UserSharepreferenceController.KEY_NAME
                    , getString(R.string.account_default_name));
            String link = userPref.getString(UserSharepreferenceController.KEY_ICON,"");
            nameTextView.setText(name);
            if (!link.isEmpty()) {
                loadProfileImage(link);
            }
            showUserProfile();
        } else {
            showLoginView();
        }
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "status:" + status.isSuccess());
                        if (status.isSuccess()) {
                            removeUserProfile();
                        }
                    }
                });
    }

    private void facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email"));
    }

    private void facebookLogout() {
        LoginManager.getInstance().logOut();
    }

    private void showUserProfile() {
        SharedPreferences userPref = getActivity().getSharedPreferences(UserSharepreferenceController.SHARED_PREF_USER, Context.MODE_PRIVATE);
        String type = userPref.getString(UserSharepreferenceController.KEY_TYPE, User.TYPE_GUEST);
        if (!type.equals(User.TYPE_GUEST)) {
            picImageView.setVisibility(View.VISIBLE);
            String name = userPref.getString(UserSharepreferenceController.KEY_NAME, getString(R.string.account_default_name));
            nameTextView.setText(name);
            nameTextView.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
            facebookLoginBtn.setVisibility(View.GONE);
            googleSignInBtn.setVisibility(View.GONE);
            signoutBtn.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
        } else {
            showLoginView();
        }
    }

    private void showLoginView() {
        SharedPreferences userPref = UserSharepreferenceController.getUserSp(getContext());
        String name;
        if (userPref.contains(UserSharepreferenceController.KEY_NAME)) {
            name = userPref.getString(UserSharepreferenceController.KEY_NAME, getString(R.string.account_default_name));
        } else {
            name = getString(R.string.account_default_name);
            userPref.edit().putString(UserSharepreferenceController.KEY_NAME, name).commit();
        }
        nameTextView.setText(name);
        nameTextView.setVisibility(View.VISIBLE);
        picImageView.setImageResource(R.drawable.ic_action_account_circle);
        picImageView.setVisibility(View.VISIBLE);
        facebookLoginBtn.setVisibility(View.VISIBLE);
        signoutBtn.setVisibility(View.GONE);
        googleSignInBtn.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
    }

    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        facebookLoginBtn.setVisibility(View.GONE);
        signoutBtn.setVisibility(View.GONE);
        googleSignInBtn.setVisibility(View.GONE);
        picImageView.setVisibility(View.GONE);
        nameTextView.setVisibility(View.GONE);
    }

    private void showNameEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View input = LayoutInflater.from(getContext()).inflate(R.layout.dialog_account_name_edit
                , null);
        final EditText editText = (EditText) input.findViewById(R.id.edtName);
        editText.setText(nameTextView.getText().toString());
        builder.setView(input);
        builder.setTitle(getString(R.string.account_dialog_new_name_title));
        builder.setPositiveButton(R.string.account_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences userPref = UserSharepreferenceController.getUserSp(getContext());
                userPref.edit().putString(UserSharepreferenceController.KEY_NAME, editText.getText().toString()).commit();
                nameTextView.setText(editText.getText().toString());
                new UpdateUserName().execute(userPref.getString(UserSharepreferenceController.KEY_USER_ID, "")
                        , editText.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.account_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fb_login_button:
                facebookLogin();
                break;
            case R.id.google_sign_in_button:
                googleSignIn();
                break;
            case R.id.sign_out_button:
                SharedPreferences userPref = UserSharepreferenceController.getUserSp(getContext());
                if (userPref.contains(UserSharepreferenceController.KEY_TYPE)) {
                    String type = userPref.getString(UserSharepreferenceController.KEY_TYPE, null);
                    switch (type) {
                        case User.TYPE_GOOGLE:
                            googleSignOut();
                            break;
                        case User.TYPE_FACEBOOK:
                            facebookLogout();
                            break;
                    }
                }
                break;
            case R.id.profile_name:
                showNameEditDialog();
                break;
        }
    }

    private void loadProfileImage(String link) {
        Glide.with(this).load(link).into(picImageView);
    }

    private void addNewFBUser(final User user) {
        Call<ResponseBody> addNewFBUser = userService.addFBUser(getString(R.string.facebook_app_id),
                user.getUserID(), user.getUserName(), user.getUserIcon(), user.getEmail(), null);
        addNewFBUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        user.setUserCode(response.body().string());
                        saveUserProfile(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateUI();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                updateUI();
            }
        });
    }

    private void addNewGoogleUser(final User user) {
        //TODO:Server API is not ready
        saveUserProfile(user);
        updateUI();
    }

    private void addNewGuestUser() {
        showLoading();
        Call<ResponseBody> getGuestCode = userService.getGuestCode();
        getGuestCode.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        User user = new User();
                        user.setUserCode(response.body().string());
                        user.setUserName(getString(R.string.account_default_name));
                        user.setType(User.TYPE_GUEST);
                        saveUserProfile(user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                updateUI();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                updateUI();
            }
        });
    }

    class UpdateUserName extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            if (!strings[0].isEmpty()) {
                DataLoader.getInstance(getContext()).updateUserName(strings[0], strings[1]);
            }
            return null;
        }
    }
}
