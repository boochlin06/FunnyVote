package com.android.heaton.funnyvote.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.android.heaton.funnyvote.MainActivity;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;

/**
 * Created by heaton on 2016/10/26.
 */

public class WelcomeActivity extends AppCompatActivity {
    public static final String SP_FIRST_TIME = "first_time";
    public static final String SP_FIRST_MOCK_DATA = "first_mock_data";
    private SharedPreferences sp;
    private AsyncTask syncTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (sp.getBoolean(SP_FIRST_MOCK_DATA, true)) {
                DataLoader.getInstance(getApplicationContext()).mockVoteData(200, 5);
                DataLoader.getInstance(getApplicationContext()).mockPromotions(5);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            sp.edit().putBoolean(SP_FIRST_MOCK_DATA, false).apply();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        sp = getSharedPreferences(SP_FIRST_TIME, Context.MODE_PRIVATE);

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
