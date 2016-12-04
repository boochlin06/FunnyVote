package com.android.heaton.funnyvote.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.heaton.funnyvote.MainActivity;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.database.DataLoader;
import com.android.heaton.funnyvote.database.User;
import com.android.heaton.funnyvote.eventbus.EventBusController;
import com.android.heaton.funnyvote.retrofit.Server;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.android.heaton.funnyvote.eventbus.EventBusController.NetworkEvent.INIT_GUEST;

/**
 * Created by heaton on 2016/10/26.
 */

public class WelcomeActivity extends AppCompatActivity {
    public static final String SP_FIRST_TIME = "first_time";
    public static final String SP_FIRST_MOCK_DATA = "first_mock_data";
    public static final String SP_FIRST_GUEST = "first_guest";
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
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_welcome);
        sp = getSharedPreferences(SP_FIRST_TIME, 0);

        if (sp.getBoolean(SP_FIRST_GUEST, true) || UserSharepreferenceController.getUser(getApplicationContext())
                .getUserCode().equals("")) {

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Server.BASE_URL).build();
            Server.UserService service = retrofit.create(Server.UserService.class);
            Call<ResponseBody> call = service.getGuestCode();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d("test","on response:"+response.headers().toString());
                    EventBus.getDefault().post(new EventBusController.NetworkEvent(INIT_GUEST
                            , true, call,response));
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    EventBus.getDefault().post(new EventBusController.NetworkEvent(INIT_GUEST
                            , false , call, null));
                }
            });
        } else {
            syncData();
        }

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkEvent(EventBusController.NetworkEvent event) {
        if (event.message.equals(INIT_GUEST)) {
            if (event.success) {
                try {
                    User user = new User();
                    user.setUserCode(event.response.body().string());
                    user.setUserName(getApplicationContext().getString(R.string.account_default_name));
                    user.setUserIcon("");
                    user.setType(User.TYPE_GUEST);
                    user.setEmail("");
                    UserSharepreferenceController.updtaeUser(getApplicationContext(), user);
                    Log.d("test", "guest code:" + user.getUserCode());
                    sp.edit().putBoolean(SP_FIRST_GUEST, false).apply();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), R.string.toast_network_connect_error, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.toast_network_connect_error, Toast.LENGTH_LONG).show();
            }
            syncData();
        }
    }

    private void syncData() {
        syncTask.execute();
    }
}
