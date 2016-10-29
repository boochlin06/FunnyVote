package com.android.heaton.funnyvote.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by chiu_mac on 2016/10/28.
 */

@Entity
public class User {
    public static final String TYPE_FACEBOOK = "facebook";
    public static final String TYPE_GOOGLE = "google";
    public static final String TYPE_TWITTER = "twitter";

    @Id
    private Long id;

    private String userName;

    private String email;

    private String userID;
    
    private String type;

    @Generated(hash = 1115745234)
    public User(Long id, String userName, String email, String userID,
            String type) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.userID = userID;
        this.type = type;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return this.userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
