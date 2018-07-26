package com.heaton.funnyvote.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.heaton.funnyvote.FirstTimePref;
import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.MainActivity;
import com.heaton.funnyvote.R;
import com.heaton.funnyvote.data.Injection;
import com.heaton.funnyvote.data.user.UserDataRepository;
import com.heaton.funnyvote.data.user.UserDataSource;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.PromotionDao;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.ui.introduction.IntroductionActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/10/26.
 */

public class WelcomeActivity extends AppCompatActivity {
    private SharedPreferences firstTimePref;
    private AsyncTask syncTask ;

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

    @SuppressLint("StaticFieldLeak")
    private void syncData() {
        syncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, true)) {
                    PromotionDao promotionDao = ((FunnyVoteApplication) (getApplication().getApplicationContext()))
                            .getDaoSession().getPromotionDao();
                    String imageURL[] = getApplication().getResources().getStringArray(R.array.imageURL);
                    List<Promotion> promotions = new ArrayList<>();
                    for (int i = 0; i < 1; i++) {
                        Promotion promotion = new Promotion();
                        promotion.setImageURL(imageURL[i % imageURL.length]);
                        promotion.setActionURL("https://play.google.com/store/apps/details?id=com.heaton.funnyvote");
                        promotion.setTitle("title:" + i);
                        promotions.add(promotion);
                    }
                    promotionDao.deleteAll();
                    promotionDao.insertInTx(promotions);
                    firstTimePref.edit().putBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, false).apply();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Object o) {
                if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
                    startActivity(new Intent(WelcomeActivity.this, IntroductionActivity.class));
                } else {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                }
                UserDataRepository userDataRepository = Injection.provideUserRepository(getApplicationContext());
                userDataRepository.getUser(new UserDataSource.GetUserCallback() {
                    @Override
                    public void onResponse(User user) {
                        finish();
                    }

                    @Override
                    public void onFailure() {
                        finish();
                    }
                }, true);
            }
        };
        syncTask.execute();
    }
}
