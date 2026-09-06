package com.heaton.funnyvote.database;

import com.google.gson.annotations.SerializedName;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by heaton on 2016/11/4.
 */
@Entity(tableName = "promotions")
public class Promotion {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    @SerializedName("imgurl")
    private String imageURL;
    @SerializedName("link")
    private String actionURL;
    @SerializedName("title")
    private String title;

    @Ignore
    public Promotion(Long id, String imageURL, String actionURL, String title) {
        this.id = id;
        this.imageURL = imageURL;
        this.actionURL = actionURL;
        this.title = title;
    }

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
