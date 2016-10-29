package com.android.heaton.funnyvote.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class AccountFragment extends android.support.v4.app.Fragment {
    private static final String TAG = AccountFragment.class.getSimpleName();

    //Facebook API
    private CallbackManager mCallbackManager;
    private AccessTokenTracker mAccessTokenTracker;

    //Views
    ImageView mPicImageView;
    TextView mNameTextView;

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
                    updateUserProfile();
                }
            }
        };

        //Views
        mNameTextView = (TextView) view.findViewById(R.id.profile_name);
        mPicImageView = (ImageView)view.findViewById(R.id.profile_picture);

        updateUserProfile();

        return view;
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
        super.onDestroy();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
    }

    private void updateUserProfile() {
        SharedPreferences userPref = getContext().getSharedPreferences(FunnyVoteApplication.SHARED_PREF_USER, Context.MODE_PRIVATE);
        if (userPref.contains(FunnyVoteApplication.KEY_NAME)) {
            mNameTextView.setText(userPref.getString(FunnyVoteApplication.KEY_NAME, "user"));
            try {
                Bitmap pic = BitmapFactory.decodeStream(getContext().openFileInput(FunnyVoteApplication.PROFILE_PICTURE_FILE));
                mPicImageView.setImageBitmap(pic);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            mNameTextView.setText("");
            mPicImageView.setImageBitmap(null);
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
            updateUserProfile();
        }
    }
}
