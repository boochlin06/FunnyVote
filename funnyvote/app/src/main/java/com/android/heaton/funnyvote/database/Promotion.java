package com.android.heaton.funnyvote.database;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by heaton on 2016/11/4.
 */
@Entity
public class Promotion {
    @Id
    private Long id;
    private String imageURL;
    private String actionURL;
    private String title;
    @Generated(hash = 148763577)
    public Promotion(Long id, String imageURL, String actionURL, String title) {
        this.id = id;
        this.imageURL = imageURL;
        this.actionURL = actionURL;
        this.title = title;
    }
    @Generated(hash = 1959537984)
    public Promotion() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getImageURL() {
        return this.imageURL;
    }
    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public String getActionURL() {
        return this.actionURL;
    }
    public void setActionURL(String actionURL) {
        this.actionURL = actionURL;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
