package com.android.heaton.funnyvote.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.android.heaton.funnyvote.data.user.UserManager;
import com.android.heaton.funnyvote.database.User;
import com.bumptech.glide.Glide;
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

import java.util.Arrays;


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
                String email = profile.has("email") ? profile.getString("email") : "";

                String link = "";
                if (profile.has("picture")) {
                    JSONObject picture = profile.getJSONObject("picture");
                    link = picture.getJSONObject("data").getString("url");
                }
                User user = new User();
                user.setUserName(name);
                user.setUserID(facebookID);
                user.setUserIcon(link);
                user.setEmail(email);
                user.setType(User.TYPE_FACEBOOK);
                userManager.registerUser(user, registerUserCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    //Google Sign in API
    private GoogleApiClient googleApiClient;

    //UserManager
    UserManager userManager;
    UserManager.RegisterUserCallback registerUserCallback = new UserManager.RegisterUserCallback() {
        @Override
        public void onSuccess() {
            updateUI();
        }

        @Override
        public void onFailure() {
            updateUI();
        }
    };
    UserManager.GetUserCallback getUserCallback = new UserManager.GetUserCallback() {
        @Override
        public void onResponse(User user) {
            if (user.getType() != User.TYPE_GUEST) {
                showUser(user);
            } else {
                showLoginView();
            }
        }

        @Override
        public void onFailure() {
            showLoginView();
        }
    };
    User user;

    //Views
    ImageView picImageView;
    TextView nameTextView;
    Button googleSignInBtn;
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
        userManager = UserManager.getInstance(getContext().getApplicationContext());
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
                    userManager.unregisterUser();
                    updateUI();
                }
            }
        };

        //Views
        facebookLoginBtn = (Button) view.findViewById(R.id.fb_login_button);
        facebookLoginBtn.setOnClickListener(this);
        nameTextView = (TextView) view.findViewById(R.id.profile_name);
        nameTextView.setOnClickListener(this);
        picImageView = (ImageView) view.findViewById(R.id.profile_picture);
        googleSignInBtn = (Button)view.findViewById(R.id.google_sign_in_button);
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
            User user = new User();
            user.setUserName(name);
            user.setUserID(googleID);
            user.setEmail(email);
            user.setUserIcon(picLink.toString());
            user.setType(User.TYPE_GOOGLE);
            userManager.registerUser(user, registerUserCallback);
        }
    }


    private void updateUI() {
        userManager.getUser(getUserCallback);
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
                            userManager.unregisterUser();
                            updateUI();
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

    private void showUser(User user) {
        this.user = user;
        nameTextView.setText(user.getUserName());
        loadProfileImage(user.getUserIcon());
        loadingProgressBar.setVisibility(View.GONE);
        facebookLoginBtn.setVisibility(View.GONE);
        googleSignInBtn.setVisibility(View.GONE);
        nameTextView.setVisibility(View.VISIBLE);
        picImageView.setVisibility(View.VISIBLE);
        signoutBtn.setVisibility(View.VISIBLE);
    }

    private void showLoginView() {
        nameTextView.setText(R.string.account_default_name);
        picImageView.setImageResource(R.drawable.ic_action_account_circle);
        loadingProgressBar.setVisibility(View.GONE);
        signoutBtn.setVisibility(View.GONE);
        facebookLoginBtn.setVisibility(View.VISIBLE);
        googleSignInBtn.setVisibility(View.VISIBLE);
        nameTextView.setVisibility(View.VISIBLE);
        picImageView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        facebookLoginBtn.setVisibility(View.GONE);
        googleSignInBtn.setVisibility(View.GONE);
        nameTextView.setVisibility(View.GONE);
        picImageView.setVisibility(View.GONE);
        signoutBtn.setVisibility(View.GONE);
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
                user.setUserName(editText.getText().toString());
                userManager.registerUser(user, registerUserCallback);
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
                switch (user.getType()) {
                    case User.TYPE_FACEBOOK:
                        facebookLogout();
                        break;
                    case User.TYPE_GOOGLE:
                        googleSignOut();
                        break;
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
}
