package com.heaton.funnyvote.database;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * Created by chiu_mac on 2016/10/28.
 */

@Entity
public class User implements Parcelable {
    public static final int TYPE_FACEBOOK = 100;
    public static final int TYPE_GOOGLE = 101;
    public static final int TYPE_TWITTER = 102;
    public static final int TYPE_GUEST = 103;

    public static final String GENDER_MALE = "male";
    public static final String GENDER_FEMALE = "female";

    public static final String TYPE_TOKEN_GUEST = "guest";
    public static final String TYPE_TOKEN_OTP = "otp";

    @Id
    private Long id;

    @SerializedName("nickname")
    public String userName;

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

    public String getTokenType() {
        return getType() == User.TYPE_GUEST ? User.TYPE_TOKEN_GUEST : User.TYPE_TOKEN_OTP;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.userName);
        dest.writeString(this.email);
        dest.writeString(this.userID);
        dest.writeString(this.userCode);
        dest.writeString(this.userIcon);
        dest.writeInt(this.type);
        dest.writeString(this.personalTokenType);
        dest.writeString(this.gender);
        dest.writeInt(this.minAge);
        dest.writeInt(this.maxAge);
    }

    protected User(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.userName = in.readString();
        this.email = in.readString();
        this.userID = in.readString();
        this.userCode = in.readString();
        this.userIcon = in.readString();
        this.type = in.readInt();
        this.personalTokenType = in.readString();
        this.gender = in.readString();
        this.minAge = in.readInt();
        this.maxAge = in.readInt();
    }

    @Generated(hash = 1947222936)
    public User(Long id, String userName, String email, String userID, String userCode,
            String userIcon, int type, String gender, int minAge, int maxAge) {
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

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
