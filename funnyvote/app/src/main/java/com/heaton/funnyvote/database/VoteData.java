package com.heaton.funnyvote.database;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.heaton.funnyvote.R;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;
import java.util.List;

/**
 * Created by heaton on 2016/10/25.
 */
@Entity(tableName = "vote_data")
public class VoteData {
    public static final String SECURITY_PRIVATE = "01";
    public static final String SECURITY_PUBLIC = "00";

    public static final String CATEGORY_HOT = "hot";
    @PrimaryKey(autoGenerate = true)
    private Long id;
    @Ignore
    private List<Option> options;
    @SerializedName("c")
    private String voteCode;
    @SerializedName("t")
    private String title;

    @SerializedName("nn")
    private String authorName;
    @SerializedName("token")
    private String authorCode;
    @SerializedName("tokentype")
    private String authorCodeType;
    @SerializedName("mi")
    private String authorIcon;
    @SerializedName("i")
    private String voteImage;
    private int localImage;
    @SerializedName("on")
    private long startTime;
    @SerializedName("off")
    private long endTime;
    private String option1Title;
    private String option1Code;
    private int option1Count;
    private boolean option1Polled;

    private String option2Title;
    private String option2Code;
    private int option2Count;
    private boolean option2Polled;
    private String optionTopTitle;
    private String optionTopCode;
    private int optionTopCount;
    private boolean optionTopPolled;
    private String optionUserChoiceTitle;
    private String optionUserChoiceCode;
    private int optionUserChoiceCount;
    @SerializedName("min")
    private int minOption;
    @SerializedName("max")
    private int maxOption;
    @SerializedName("osn")
    private int optionCount;
    @SerializedName("voted")
    private int pollCount;
    @SerializedName("isVoted")
    private boolean isPolled;
    @SerializedName("fav")
    private boolean isFavorite;

    @SerializedName("res")
    private boolean isCanPreviewResult;
    @SerializedName("add")
    private boolean isUserCanAddOption;
    @SerializedName("p")
    private boolean isNeedPassword;
    @Ignore
    public String password;
    @SerializedName("sec")
    private String security = SECURITY_PUBLIC;

    private String category;
    //Only save hot order
    private int displayOrder;
    //TODO: OPTION TYPE
    private String pollType;

    /**
     * gson used.
     */
    @Ignore
    @SerializedName("os")
    private List<Option> netOptions;

    @Ignore
    @SerializedName("first")
    private Option firstOption;

    @Ignore
    @SerializedName("second")
    private Option secondOption;

    @Ignore
    @SerializedName("top")
    private Option topOption;

    @Ignore
    @SerializedName("user")
    private Option userOption;

    public void setNetOptions(List<Option> netOptions) {
        this.netOptions = netOptions;
    }

    public List<Option> getNetOptions() {
        return this.netOptions;
    }

    public Option getFirstOption() {
        return this.firstOption;
    }

    public void setFirstOption(Option firstOption) {
        this.firstOption = firstOption;
    }

    public Option getSecondOption() {
        return this.secondOption;
    }

    public void setSecondOption(Option secondOption) {
        this.secondOption = secondOption;
    }

    public Option getTopOption() {
        return this.topOption;
    }

    public void setTopOption(Option topOption) {
        this.topOption = topOption;
    }

    public Option getUserOption() {
        return this.userOption;
    }

    public void setUserOption(Option userOption) {
        this.userOption = userOption;
    }

    @Ignore
    private File image;

    public File getImageFile() { return this.image;}
    public void setImageFile(File file) {
        this.image = file;
    }

    /**
     * UI temp used
     */
    @Ignore
    public User author;

    public static String getSecurityString(Context context, String security) {
        if (security.equals(SECURITY_PUBLIC)) {
            return context.getString(R.string.create_vote_tab_settings_public);
        } else {
            return context.getString(R.string.create_vote_tab_settings_private);
        }
    }

    @Ignore
    public VoteData(Long id, String voteCode, String title, String authorName, String authorCode, String authorCodeType, String authorIcon, String voteImage, int localImage,
            long startTime, long endTime, String option1Title, String option1Code, int option1Count, boolean option1Polled, String option2Title, String option2Code, int option2Count,
            boolean option2Polled, String optionTopTitle, String optionTopCode, int optionTopCount, boolean optionTopPolled, String optionUserChoiceTitle, String optionUserChoiceCode,
            int optionUserChoiceCount, int minOption, int maxOption, int optionCount, int pollCount, boolean isPolled, boolean isFavorite, boolean isCanPreviewResult,
            boolean isUserCanAddOption, boolean isNeedPassword, String security, String category, int displayOrder, String pollType) {
        this.id = id;
        this.voteCode = voteCode;
        this.title = title;
        this.authorName = authorName;
        this.authorCode = authorCode;
        this.authorCodeType = authorCodeType;
        this.authorIcon = authorIcon;
        this.voteImage = voteImage;
        this.localImage = localImage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.option1Title = option1Title;
        this.option1Code = option1Code;
        this.option1Count = option1Count;
        this.option1Polled = option1Polled;
        this.option2Title = option2Title;
        this.option2Code = option2Code;
        this.option2Count = option2Count;
        this.option2Polled = option2Polled;
        this.optionTopTitle = optionTopTitle;
        this.optionTopCode = optionTopCode;
        this.optionTopCount = optionTopCount;
        this.optionTopPolled = optionTopPolled;
        this.optionUserChoiceTitle = optionUserChoiceTitle;
        this.optionUserChoiceCode = optionUserChoiceCode;
        this.optionUserChoiceCount = optionUserChoiceCount;
        this.minOption = minOption;
        this.maxOption = maxOption;
        this.optionCount = optionCount;
        this.pollCount = pollCount;
        this.isPolled = isPolled;
        this.isFavorite = isFavorite;
        this.isCanPreviewResult = isCanPreviewResult;
        this.isUserCanAddOption = isUserCanAddOption;
        this.isNeedPassword = isNeedPassword;
        this.security = security;
        this.category = category;
        this.displayOrder = displayOrder;
        this.pollType = pollType;
    }

    public VoteData() {
    }

    public boolean isMultiChoice() {
        return !(maxOption == 1 && minOption == 1);
    }


    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getAuthorName() {
        return this.authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorCode() {
        return this.authorCode;
    }

    public void setAuthorCode(String authorCode) {
        this.authorCode = authorCode;
    }

    public String getAuthorIcon() {
        return this.authorIcon;
    }

    public void setAuthorIcon(String authorIcon) {
        this.authorIcon = authorIcon;
    }

    public String getVoteImage() {
        return this.voteImage;
    }

    public void setVoteImage(String voteImage) {
        this.voteImage = voteImage;
    }

    public int getLocalImage() {
        return this.localImage;
    }

    public void setLocalImage(int localImage) {
        this.localImage = localImage;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getOption1Title() {
        return this.option1Title;
    }

    public void setOption1Title(String option1Title) {
        this.option1Title = option1Title;
    }

    public String getOption1Code() {
        return this.option1Code;
    }

    public void setOption1Code(String option1Code) {
        this.option1Code = option1Code;
    }

    public int getOption1Count() {
        return this.option1Count;
    }

    public void setOption1Count(int option1Count) {
        this.option1Count = option1Count;
    }

    public String getOption2Title() {
        return this.option2Title;
    }

    public void setOption2Title(String option2Title) {
        this.option2Title = option2Title;
    }

    public String getOption2Code() {
        return this.option2Code;
    }

    public void setOption2Code(String option2Code) {
        this.option2Code = option2Code;
    }

    public int getOption2Count() {
        return this.option2Count;
    }

    public void setOption2Count(int option2Count) {
        this.option2Count = option2Count;
    }

    public String getOptionTopTitle() {
        return this.optionTopTitle;
    }

    public void setOptionTopTitle(String optionTopTitle) {
        this.optionTopTitle = optionTopTitle;
    }

    public String getOptionTopCode() {
        return this.optionTopCode;
    }

    public void setOptionTopCode(String optionTopCode) {
        this.optionTopCode = optionTopCode;
    }

    public int getOptionTopCount() {
        return this.optionTopCount;
    }

    public void setOptionTopCount(int optionTopCount) {
        this.optionTopCount = optionTopCount;
    }

    public String getOptionUserChoiceTitle() {
        return this.optionUserChoiceTitle;
    }

    public void setOptionUserChoiceTitle(String optionUserChoiceTitle) {
        this.optionUserChoiceTitle = optionUserChoiceTitle;
    }

    public String getOptionUserChoiceCode() {
        return this.optionUserChoiceCode;
    }

    public void setOptionUserChoiceCode(String optionUserChoiceCode) {
        this.optionUserChoiceCode = optionUserChoiceCode;
    }

    public int getOptionUserChoiceCount() {
        return this.optionUserChoiceCount;
    }

    public void setOptionUserChoiceCount(int optionUserChoiceCount) {
        this.optionUserChoiceCount = optionUserChoiceCount;
    }

    public int getMinOption() {
        return this.minOption;
    }

    public void setMinOption(int minOption) {
        this.minOption = minOption;
    }

    public int getMaxOption() {
        return this.maxOption;
    }

    public void setMaxOption(int maxOption) {
        this.maxOption = maxOption;
    }

    public int getOptionCount() {
        return this.optionCount;
    }

    public void setOptionCount(int optionCount) {
        this.optionCount = optionCount;
    }

    public int getPollCount() {
        return this.pollCount;
    }

    public void setPollCount(int pollCount) {
        this.pollCount = pollCount;
    }

    public boolean getIsPolled() {
        return this.isPolled;
    }

    public void setIsPolled(boolean isPolled) {
        this.isPolled = isPolled;
    }

    public boolean getIsFavorite() {
        return this.isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean getIsCanPreviewResult() {
        return this.isCanPreviewResult;
    }

    public void setIsCanPreviewResult(boolean isCanPreviewResult) {
        this.isCanPreviewResult = isCanPreviewResult;
    }

    public boolean getIsUserCanAddOption() {
        return this.isUserCanAddOption;
    }

    public void setIsUserCanAddOption(boolean isUserCanAddOption) {
        this.isUserCanAddOption = isUserCanAddOption;
    }

    public boolean getIsNeedPassword() {
        return this.isNeedPassword;
    }

    public void setIsNeedPassword(boolean isNeedPassword) {
        this.isNeedPassword = isNeedPassword;
    }

    public String getSecurity() {
        return this.security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public String getPollType() {
        return this.pollType;
    }

    public void setPollType(String pollType) {
        this.pollType = pollType;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public int getDisplayOrder() {
        return this.displayOrder;
    }

    @Ignore
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getAuthorCodeType() {
        return this.authorCodeType;
    }

    public void setAuthorCodeType(String authorCodeType) {
        this.authorCodeType = authorCodeType;
    }

    public boolean getOption1Polled() {
        return this.option1Polled;
    }

    public void setOption1Polled(boolean option1Polled) {
        this.option1Polled = option1Polled;
    }

    public boolean getOption2Polled() {
        return this.option2Polled;
    }

    public void setOption2Polled(boolean option2Polled) {
        this.option2Polled = option2Polled;
    }

    public boolean getOptionTopPolled() {
        return this.optionTopPolled;
    }

    public void setOptionTopPolled(boolean optionTopPolled) {
        this.optionTopPolled = optionTopPolled;
    }
}