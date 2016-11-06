package com.android.heaton.funnyvote.database;

import android.util.Log;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by heaton on 2016/10/25.
 */
@Entity
public class Option {
    @Id
    private Long id;
    private String voteCode;
    private String title;
    private Integer count;
    private String code;
    private boolean isUserChoiced = false;

    @Generated(hash = 259294054)
    public Option(Long id, String voteCode, String title, Integer count,
                  String code, boolean isUserChoiced) {
        this.id = id;
        this.voteCode = voteCode;
        this.title = title;
        this.count = count;
        this.code = code;
        this.isUserChoiced = isUserChoiced;
    }

    public void dumpDetail() {
        Log.d("test", "Id:" + id + " voteCode:" + voteCode + " title:" + title + " count:"
                + count + " code:" + code + " userchoice:" + isUserChoiced);
    }

    @Generated(hash = 104107376)
    public Option() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVoteCode() {
        return this.voteCode;
    }

    public void setVoteCode(String voteCode) {
        this.voteCode = voteCode;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getIsUserChoiced() {
        return this.isUserChoiced;
    }

    public void setIsUserChoiced(boolean isUserChoiced) {
        this.isUserChoiced = isUserChoiced;
    }
}
