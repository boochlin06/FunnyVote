package com.android.heaton.funnyvote.data.user;

import com.android.heaton.funnyvote.database.User;

/**
 * Created by chiu_mac on 2016/12/6.
 */

public interface UserDataSource {
    User getUser();
    void setUser(User user);
    void removeUser();
}
