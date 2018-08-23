package com.heaton.funnyvote.ui;

import android.app.Activity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 2016/10/26.
 */

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        InitTask task = new InitTask(this);
        task.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static class InitTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Activity> contextWeakReference;
        private SharedPreferences firstTimePref;

        public InitTask(Activity context) {
            contextWeakReference = new WeakReference<Activity>(context);
            firstTimePref = Injection.provideFirstTimePref(contextWeakReference.get());
        }


        @Override
        protected void onPostExecute(Void o) {
            if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_INTRODUCTION_PAGE, true)) {
                contextWeakReference.get().startActivity(
                        new Intent(contextWeakReference.get(), IntroductionActivity.class));
            } else {
                contextWeakReference.get().startActivity(
                        new Intent(contextWeakReference.get(), MainActivity.class));
            }
            UserDataRepository userDataRepository = Injection.provideUserRepository(contextWeakReference.get());
            userDataRepository.getUser(new UserDataSource.GetUserCallback() {
                @Override
                public void onResponse(User user) {
                    contextWeakReference.get().finish();
                }

                @Override
                public void onFailure() {
                    contextWeakReference.get().finish();
                }
            }, true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.currentThread();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (firstTimePref.getBoolean(FirstTimePref.SP_FIRST_MOCK_DATA, true)) {
                PromotionDao promotionDao = ((FunnyVoteApplication) (contextWeakReference.get().getApplicationContext()))
                        .getDaoSession().getPromotionDao();
                String imageURL[] = contextWeakReference.get().getResources().getStringArray(R.array.imageURL);
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
    }
}
