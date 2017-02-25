package com.android.heaton.funnyvote.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.heaton.funnyvote.FirstTimePref;
import com.android.heaton.funnyvote.MainActivity;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.ui.introduction.IntroductionActivity;

import static com.android.heaton.funnyvote.FirstTimePref.SP_FIRST_INTRODUCTION_PAGE;
import static com.android.heaton.funnyvote.FirstTimePref.SP_FIRST_MOCK_DATA;

/**
 * Created by heaton on 2016/10/26.
 */

public class WelcomeActivity extends AppCompatActivity {
    private SharedPreferences firstTimePref;
    private AsyncTask syncTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (firstTimePref.getBoolean(SP_FIRST_MOCK_DATA, true)) {
                DataLoader.getInstance(getApplicationContext()).mockPromotions(5);
            }
            firstTimePref.edit().putBoolean(SP_FIRST_MOCK_DATA, false);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            if (firstTimePref.getBoolean(SP_FIRST_INTRODUCTION_PAGE, true)) {
                firstTimePref.edit().putBoolean(SP_FIRST_INTRODUCTION_PAGE, false).apply();
                startActivity(new Intent(WelcomeActivity.this, IntroductionActivity.class));
            } else {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            }
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        firstTimePref = FirstTimePref.getInstance(getApplicationContext())
                .getPreferences();
        syncData();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void syncData() {
        syncTask.execute();
    }
}
