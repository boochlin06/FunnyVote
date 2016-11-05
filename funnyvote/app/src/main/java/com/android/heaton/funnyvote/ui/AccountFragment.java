package com.android.heaton.funnyvote.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.heaton.funnyvote.FunnyVoteApplication;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.User;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by chiu_mac on 2016/10/28.
 */

public class AccountFragment extends android.support.v4.app.Fragment
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    private static final String TAG = AccountFragment.class.getSimpleName();
    private static final int RC_GOOGLE_SIGN_IN = 101;

    //Facebook API
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mAccessTokenTracker;

    //Google Sign in API
    private GoogleApiClient mGoogleApiClient;

    //Views
    ImageView mPicImageView;
    TextView mNameTextView;
    View mGoogleSignInBtn;
    Button mSignoutBtn;
    LoginButton mFBLoginBtn;
    ProgressBar mLoadingProgressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_account, null);

        mCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) view.findViewById(R.id.fb_login_button);
        loginButton.setFragment(this);
        loginButton.setReadPermissions(Arrays.asList("email"));
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess");
                showLoadingProgressBar();
                getFacebookProfile();
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

        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                Log.d(TAG, "onCurrentAccessTokenChanged");
                if (currentAccessToken == null) {
                    removeUserProfile();
                }
            }
        };

        //Views
        mFBLoginBtn = loginButton;
        mNameTextView = (TextView)view.findViewById(R.id.profile_name);
        mPicImageView = (ImageView)view.findViewById(R.id.profile_picture);
        mGoogleSignInBtn = view.findViewById(R.id.google_sign_in_button);
        mGoogleSignInBtn.setOnClickListener(this);
        mSignoutBtn = (Button)view.findViewById(R.id.sign_out_button);
        mSignoutBtn.setOnClickListener(this);
        mLoadingProgressBar = (ProgressBar)view.findViewById(R.id.loading_progress_bar);

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
        mAccessTokenTracker.stopTracking();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            showLoadingProgressBar();
            try {
                // Signed in successfully
                GoogleSignInAccount googleAccount = result.getSignInAccount();
                String name = googleAccount.getDisplayName();
                String googleID = googleAccount.getId();
                String email = googleAccount.getEmail();
                Uri picLink = googleAccount.getPhotoUrl();
                User user = new User(null, name, email, googleID, User.TYPE_GOOGLE);
                saveUserProfile(user);
                Log.d(TAG, "name:" + name);
                Log.d(TAG, "google ID:" + googleID);
                Log.d(TAG, "email:" + email);
                Log.d(TAG, "pic link:" + picLink.toString());
                URL url = new URL(picLink.toString());
                new GetProfilePictureTask().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getFacebookProfile() {
        Bundle params = new Bundle();
        params.putString("fields","email,name,picture.type(large)");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            //save user profile
                            JSONObject profile = response.getJSONObject();
                            String name = profile.getString("name");
                            String facebookID = profile.getString("id");
                            String email = null;
                            if (profile.has("email")) {
                                email = profile.getString("email");
                            }
                            User user = new User(null, name, email, facebookID,User.TYPE_FACEBOOK);
                            saveUserProfile(user);
                            //download user's profile picture
                            if (profile.has("picture")) {
                                JSONObject picture = profile.getJSONObject("picture");
                                String link = picture.getJSONObject("data").getString("url");
                                URL url = new URL(link);
                                new GetProfilePictureTask().execute(url);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    private void saveUserProfile(User user) {
        Log.d(TAG, "saveUserProfile");
        SharedPreferences userPref = getContext().getSharedPreferences(FunnyVoteApplication.SHARED_PREF_USER, Context.MODE_PRIVATE);
        SharedPreferences.Editor spEditor = userPref.edit();
        spEditor.putString(FunnyVoteApplication.KEY_NAME, user.getUserName());
        spEditor.putString(FunnyVoteApplication.KEY_USER_ID, user.getUserID());
        spEditor.putString(FunnyVoteApplication.KEY_TYPE, user.getType());
        if (user.getEmail() != null) {
            spEditor.putString(FunnyVoteApplication.KEY_EMAIL, user.getEmail());
        }
        spEditor.commit();
        //TODO: save user data to DB
    }

    private void removeUserProfile() {
        SharedPreferences userPref = getContext().getSharedPreferences(FunnyVoteApplication.SHARED_PREF_USER, Context.MODE_PRIVATE);
        userPref.edit().clear().commit();
        getContext().deleteFile(FunnyVoteApplication.PROFILE_PICTURE_FILE);
        updateUI();
    }

    private void updateUI() {
        SharedPreferences userPref = getContext().getSharedPreferences(FunnyVoteApplication.SHARED_PREF_USER, Context.MODE_PRIVATE);
        if (userPref.contains(FunnyVoteApplication.KEY_NAME)) {
            String name = userPref.getString(FunnyVoteApplication.KEY_NAME, "user");
            mNameTextView.setText(name);
            new LoadProfilePictureTask().execute(getContext().getFileStreamPath(FunnyVoteApplication.PROFILE_PICTURE_FILE).getAbsolutePath());
        } else {
            showLoginButton();
        }
    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
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

    private void showUserProfile() {
        mPicImageView.setVisibility(View.VISIBLE);
        mNameTextView.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.GONE);
        showLogoutButton();
    }

    private void showLoginButton() {
        mPicImageView.setImageBitmap(null);
        mNameTextView.setText("");
        mPicImageView.setVisibility(View.GONE);
        mNameTextView.setVisibility(View.GONE);
        mFBLoginBtn.setVisibility(View.VISIBLE);
        mSignoutBtn.setVisibility(View.GONE);
        mGoogleSignInBtn.setVisibility(View.VISIBLE);
        mLoadingProgressBar.setVisibility(View.GONE);
    }

    private void showLogoutButton() {
        SharedPreferences userPref = getContext().getSharedPreferences(FunnyVoteApplication.SHARED_PREF_USER, Context.MODE_PRIVATE);
        if (userPref.contains(FunnyVoteApplication.KEY_NAME)) {
            String type = userPref.getString(FunnyVoteApplication.KEY_TYPE, null);
            switch (type) {
                case User.TYPE_FACEBOOK:
                    mFBLoginBtn.setVisibility(View.VISIBLE);
                    mGoogleSignInBtn.setVisibility(View.GONE);
                    mSignoutBtn.setVisibility(View.GONE);
                    break;
                case User.TYPE_GOOGLE:
                    mFBLoginBtn.setVisibility(View.GONE);
                    mGoogleSignInBtn.setVisibility(View.GONE);
                    mSignoutBtn.setVisibility(View.VISIBLE);
                    break;
                case User.TYPE_TWITTER:
                    break;
                default:
                    removeUserProfile();
            }
            mLoadingProgressBar.setVisibility(View.GONE);
        } else {
            showLoginButton();
        }
    }

    private void showLoadingProgressBar() {
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mFBLoginBtn.setVisibility(View.GONE);
        mSignoutBtn.setVisibility(View.GONE);
        mGoogleSignInBtn.setVisibility(View.GONE);
        mPicImageView.setVisibility(View.GONE);
        mNameTextView.setVisibility(View.GONE);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_sign_in_button:
                googleSignIn();
                break;
            case R.id.sign_out_button:
                googleSignOut();
                break;
        }
    }

    class LoadProfilePictureTask extends  AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPreExecute() {
            showLoadingProgressBar();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap pic = BitmapFactory.decodeFile(strings[0]);
            return pic;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mPicImageView.setImageBitmap(bitmap);
            showUserProfile();
        }
    }

    class GetProfilePictureTask extends AsyncTask<URL, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(URL... params) {
            try {
                Bitmap pic = BitmapFactory.decodeStream(params[0].openStream());
                pic.compress(Bitmap.CompressFormat.PNG, 90,
                        getContext().openFileOutput(FunnyVoteApplication.PROFILE_PICTURE_FILE,
                                Context.MODE_PRIVATE));
                pic.recycle();
                return pic;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            updateUI();
        }
    }
}
