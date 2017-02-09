package com.android.heaton.funnyvote.database;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by chiu_mac on 2016/10/28.
 */

@Entity
public class User {
    public static final int TYPE_FACEBOOK = 100;
    public static final int TYPE_GOOGLE = 101;
    public static final int TYPE_TWITTER = 102;
    public static final int TYPE_GUEST = 103;

    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    public static final String TYPE_TOKEN_MEMBER = "member";
    public static final String TYPE_TOKEN_GUEST = "guset";
    public static final String TYPE_TOKEN_OTP = "otp";

    @Id
    private Long id;

    @SerializedName("nickname")
    private String userName;

    private String email;

    private String userID;
    @SerializedName("guest")
    private String userCode;
    @SerializedName("img")
    private String userIcon;

    private int type;
    // For query the other person.
    @Transient
    public String personalTokenType;

    private String gender;

    private int minAge;
    private int maxAge;

    public static String getUserTypeString(int type) {
        switch (type) {
            case TYPE_FACEBOOK:
                return "FaceBook";
            case TYPE_GOOGLE:
                return "Google";
            case TYPE_TWITTER:
                return "Twitter";
            default:
                return "Guest";
        }
    }

    @Generated(hash = 586692638)
    public User() {
    }

    @Generated(hash = 1947222936)
    public User(Long id, String userName, String email, String userID,
                String userCode, String userIcon, int type, String gender, int minAge,
                int maxAge) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.userID = userID;
        this.userCode = userCode;
        this.userIcon = userIcon;
        this.type = type;
        this.gender = gender;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    public String getTokenType() {
        return getType() == User.TYPE_GUEST ? User.TYPE_TOKEN_GUEST : User.TYPE_TOKEN_MEMBER;
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUserCode() {
        return this.userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getUserIcon() {
        return this.userIcon;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }
}
