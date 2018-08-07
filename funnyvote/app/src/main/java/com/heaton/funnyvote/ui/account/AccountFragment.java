package com.heaton.funnyvote.ui.account;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

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
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.analytics.AnalyzticsTag;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.twitter.CustomTwitterApiClient;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Response;


/**
 * Created by chiu_mac on 2016/10/28.
 */

public class AccountFragment extends android.support.v4.app.Fragment
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, AccountContract.View {
    private static final String TAG = AccountFragment.class.getSimpleName();
    private static final int RC_GOOGLE_SIGN_IN = 101;
    private static final int LOGIN_FB = 111;
    private static final int LOGIN_GOOGLE = 112;
    private static final int LOGIN_TWITTER = 113;

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
                String gender = profile.has("gender") ? profile.getString("gender") : "";
                int min = -1;
                int max = -1;

                if (profile.has("age_range")) {
                    JSONObject ageRange = profile.getJSONObject("age_range");
                    min = ageRange.has("min") ? ageRange.getInt("min") : -1;
                    max = ageRange.has("max") ? ageRange.getInt("max") : -1;
                }

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
                user.setGender(gender);
                user.setMinAge(min);
                user.setMaxAge(max);
                user.setType(User.TYPE_FACEBOOK);
                presenter.registerUser(user, getString(R.string.facebook_app_id));
                //userManager.registerUser(user, mergeGuest, registerUserCallback);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    private Tracker tracker;
    //Google Sign in API
    private GoogleApiClient googleApiClient;

    //UserManager
    private AccountContract.Presenter presenter;

    //Views
    private ImageView picImageView;
    private ImageView editNameImageView;
    private TextView nameTextView;
    private Button googleSignInBtn;
    private Button signoutBtn;
    private Button facebookLoginBtn;
    private Button twitterLoginBtn;
    private ProgressBar loadingProgressBar;
    private TwitterAuthClient authClient;
    private Callback<TwitterSession> twitterLoginCallback = new Callback<TwitterSession>() {
        @Override
        public void success(Result<TwitterSession> result) {
            Log.d(TAG, "Twitter login success");
            TwitterSession twitterSession = result.data;
            CustomTwitterApiClient twitterApiClient = new CustomTwitterApiClient(twitterSession);
            CustomTwitterApiClient.UserService userService = twitterApiClient.getUserService();
            Call<com.twitter.sdk.android.core.models.User> twitterUserProfileCall = userService.show(twitterSession.getUserId());
            twitterUserProfileCall.enqueue(new retrofit2.Callback<com.twitter.sdk.android.core.models.User>() {
                @Override
                public void onResponse(Call<com.twitter.sdk.android.core.models.User> call,
                                       Response<com.twitter.sdk.android.core.models.User> response) {
                    Log.d(TAG, "Get twitter user profile:" + response.code());
                    if (response.isSuccessful()) {
                        com.twitter.sdk.android.core.models.User user = response.body();
                        User newUser = new User();
                        newUser.setUserName(user.name);
                        newUser.setUserID(user.idStr);
                        newUser.setEmail(user.email);
                        newUser.setUserIcon(user.profileImageUrl.replace("normal", "bigger"));
                        newUser.setType(User.TYPE_TWITTER);
                        presenter.registerUser(newUser, getString(R.string.twitter_api_id));
                        //userManager.registerUser(newUser, mergeGuest, registerUserCallback);
                    } else {
                        Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<com.twitter.sdk.android.core.models.User> call, Throwable t) {
                    t.printStackTrace();
                    Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void failure(TwitterException exception) {
            exception.printStackTrace();
        }
    };

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        FunnyVoteApplication application = (FunnyVoteApplication) getActivity().getApplication();
        tracker = application.getDefaultTracker();
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
                Log.e(TAG, "onError:" + error.getMessage());
                Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
            }
        });

        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "onCurrentAccessTokenChanged");
                if (currentAccessToken == null) {
                    //userManager.unregisterUser();
                    presenter.unregisterUser();
                    presenter.updateUser();
                    //updateUI();
                }
            }
        };

        //Views
        facebookLoginBtn = (Button) view.findViewById(R.id.fb_login_button);
        facebookLoginBtn.setOnClickListener(this);
        nameTextView = (TextView) view.findViewById(R.id.profile_name);
        nameTextView.setOnClickListener(this);
        picImageView = (ImageView) view.findViewById(R.id.profile_picture);
        googleSignInBtn = (Button) view.findViewById(R.id.google_sign_in_button);
        googleSignInBtn.setOnClickListener(this);
        signoutBtn = (Button) view.findViewById(R.id.sign_out_button);
        signoutBtn.setOnClickListener(this);
        loadingProgressBar = (ProgressBar) view.findViewById(R.id.loading_progress_bar);
        editNameImageView = (ImageView) view.findViewById(R.id.edit_name);
        editNameImageView.setOnClickListener(this);
        twitterLoginBtn = (Button) view.findViewById(R.id.twitter_log_in_button);
        twitterLoginBtn.setOnClickListener(this);


        presenter = new AccountPresenter(Injection.provideUserRepository(getContext()), this);
        presenter.start();

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
        super.onDestroy();
        accessTokenTracker.stopTracking();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult:" + requestCode + "/" + resultCode);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignIn(result);
        } else if (requestCode == getTwitterAuthClient().getRequestCode()) {
            getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleFacebookLogIn() {
        Bundle params = new Bundle();
        params.putString("fields", "email,name,picture.type(large),gender,age_range");
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
            if (picLink != null) {
                user.setUserIcon(picLink.toString());
            }
            user.setType(User.TYPE_GOOGLE);
            presenter.registerUser(user, getString(R.string.google_app_id));
            //userManager.registerUser(user, mergeGuest, registerUserCallback);
        } else {
            Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
        }
    }

    public void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("google").build());
    }

    public void googleSignOut() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(TAG, "status:" + status.isSuccess());
                        if (status.isSuccess()) {
                            presenter.unregisterUser();
                            presenter.updateUser();
                            //userManager.unregisterUser();
                            //updateUI();
                        }
                    }
                });
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("google").build());
    }

    public void facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this,
                Arrays.asList("public_profile", "email"));
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("facebook").build());
    }

    public void facebookLogout() {
        LoginManager.getInstance().logOut();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("facebook").build());
    }

    public void showUser(User user) {
        nameTextView.setText(user.getUserName());
        loadProfileImage(user.getUserIcon());
        loadingProgressBar.setVisibility(View.GONE);
        facebookLoginBtn.setVisibility(View.GONE);
        googleSignInBtn.setVisibility(View.GONE);
        twitterLoginBtn.setVisibility(View.GONE);
        nameTextView.setVisibility(View.VISIBLE);
        picImageView.setVisibility(View.VISIBLE);
        signoutBtn.setVisibility(View.VISIBLE);
    }

    public void showLoginView(String guestName) {
        nameTextView.setText(guestName);
        picImageView.setImageResource(R.drawable.ic_action_account_circle);
        loadingProgressBar.setVisibility(View.GONE);
        signoutBtn.setVisibility(View.GONE);
        facebookLoginBtn.setVisibility(View.VISIBLE);
        googleSignInBtn.setVisibility(View.VISIBLE);
        twitterLoginBtn.setVisibility(View.VISIBLE);
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

    public void showNameEditDialog() {
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

                tracker.send(new HitBuilders.EventBuilder()
                        .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                        .setAction(AnalyzticsTag.ACTION_ACCOUNT_RENAME)
                        .setLabel(editText.getText().toString()).build());
                presenter.changeCurrentUserName(editText.getText().toString());
                //userManager.changeCurrentUserName(editText.getText().toString(), changeUserNameCallback);
            }
        });
        builder.setNegativeButton(R.string.account_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();

    }

    public void showMergeOptionDialog(final int loginType) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.merge_option_msg);
        builder.setTitle(R.string.merge_option_title);
        builder.setPositiveButton(R.string.account_dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.login(loginType, true);
            }
        });
        builder.setNegativeButton(R.string.account_dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presenter.login(loginType, false);
            }
        });
        builder.show();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(getContext(), R.string.account_toast_login_fail, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fb_login_button:
                showMergeOptionDialog(LOGIN_FB);
                break;
            case R.id.google_sign_in_button:
                showMergeOptionDialog(LOGIN_GOOGLE);
                break;
            case R.id.twitter_log_in_button:
                showMergeOptionDialog(LOGIN_TWITTER);
            case R.id.sign_out_button:
                presenter.logout();
                break;
            case R.id.edit_name:
            case R.id.profile_name:
                showNameEditDialog();
                break;
        }
    }

    private void loadProfileImage(String link) {
        Glide.with(this).load(link).into(picImageView);
    }

    public void twitterLogin() {
        getTwitterAuthClient().authorize(getActivity(), twitterLoginCallback);
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGIN)
                .setLabel("twitter").build());
    }

    public void twitterlogout() {
        Twitter.getSessionManager().clearActiveSession();
        presenter.unregisterUser();
        presenter.updateUser();
        //userManager.unregisterUser();
        //updateUI();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(AnalyzticsTag.CATEGORY_ACCOUNT)
                .setAction(AnalyzticsTag.ACTION_ACCOUNT_LOGOUT)
                .setLabel("twitter").build());
    }

    private TwitterAuthClient getTwitterAuthClient() {
        if (authClient == null) {
            synchronized (TwitterLoginButton.class) {
                if (authClient == null) {
                    authClient = new TwitterAuthClient();
                }
            }
        }
        return authClient;
    }

    @Override
    public void setPresenter(AccountContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
