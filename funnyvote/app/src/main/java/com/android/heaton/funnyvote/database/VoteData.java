package com.android.heaton.funnyvote.database;

import android.content.Context;

import com.android.heaton.funnyvote.R;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

/**
 * Created by heaton on 2016/10/25.
 */
@Entity
public class VoteData {
    public static final String SECURITY_PRIVATE = "01";
    public static final String SECURITY_PUBLIC = "00";

    public static final String CATEGORY_HOT = "hot";
    @Id
    private Long id;
    @ToMany(referencedJoinProperty = "voteCode")
    @OrderBy("id ASC")
    private List<Option> options;
    @SerializedName("c")
    private String voteCode;
    @SerializedName("t")
    private String title;

    @SerializedName("nn")
    private String authorName;
    private String authorCode;
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

    private String option2Title;
    private String option2Code;
    private int option2Count;
    private String optionTopTitle;
    private String optionTopCode;
    private int optionTopCount;
    private String optionUserChoiceTitle;
    private String optionUserChoiceCode;
    private int optionUserChoiceCount;
    @SerializedName("min")
    private int minOption;
    @SerializedName("max")
    private int maxOption;
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
    @Transient
    public String password;
    private String security = SECURITY_PUBLIC;

    private String category;
    //Only save hot order
    private int displayOrder;
    // TODO: OPTION TYPE
    private String pollType;

    /**
     * gson used.
     */
    @Transient
    @SerializedName("os")
    private List<Option> netOptions;

    @Transient
    @SerializedName("first")
    private Option firstOption;

    @Transient
    @SerializedName("second")
    private Option secondOption;

    @Transient
    @SerializedName("top")
    private Option topOption;

    @Transient
    @SerializedName("user")
    private Option userOption;

    @Transient
    @SerializedName("gc")
    private String guestCode;

    @Transient
    @SerializedName("mc")
    private String memberCode;

    public List<Option> getNetOptions() {
        return this.netOptions;
    }

    public Option getFirstOption() {
        return this.firstOption;
    }

    public Option getSecondOption() {
        return this.secondOption;
    }

    public Option getTopOption() {
        return this.topOption;
    }

    public Option getUserOption() {
        return this.userOption;
    }

    public String getGuestCode() {
        return guestCode;
    }

    public String getMemberCode() {
        return memberCode;
    }

    /**
     * UI temp used
     */
    @Transient
    public User author;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 621134322)
    private transient VoteDataDao myDao;

    public static String getSecurityString(Context context, String security) {
        if (security.equals(SECURITY_PUBLIC)) {
            return context.getString(R.string.create_vote_tab_settings_public);
        } else {
            return context.getString(R.string.create_vote_tab_settings_private);
        }
    }

    @Generated(hash = 1347483641)
    public VoteData(Long id, String voteCode, String title, String authorName, String authorCode, String authorIcon, String voteImage, int localImage, long startTime,
                    long endTime, String option1Title, String option1Code, int option1Count, String option2Title, String option2Code, int option2Count, String optionTopTitle,
                    String optionTopCode, int optionTopCount, String optionUserChoiceTitle, String optionUserChoiceCode, int optionUserChoiceCount, int minOption, int maxOption,
                    int optionCount, int pollCount, boolean isPolled, boolean isFavorite, boolean isCanPreviewResult, boolean isUserCanAddOption, boolean isNeedPassword,
                    String security, String category, int displayOrder, String pollType) {
        this.id = id;
        this.voteCode = voteCode;
        this.title = title;
        this.authorName = authorName;
        this.authorCode = authorCode;
        this.authorIcon = authorIcon;
        this.voteImage = voteImage;
        this.localImage = localImage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.option1Title = option1Title;
        this.option1Code = option1Code;
        this.option1Count = option1Count;
        this.option2Title = option2Title;
        this.option2Code = option2Code;
        this.option2Count = option2Count;
        this.optionTopTitle = optionTopTitle;
        this.optionTopCode = optionTopCode;
        this.optionTopCount = optionTopCount;
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

    @Generated(hash = 1720963510)
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Keep
    public List<Option> getOptions() {
        if (options == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            OptionDao targetDao = daoSession.getOptionDao();
            List<Option> optionsNew = targetDao._queryVoteData_Options(voteCode);
            synchronized (this) {
                if (options == null) {
                    options = optionsNew;
                }
            }
        }
        return options;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 37457025)
    public synchronized void resetOptions() {
        options = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1087745195)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getVoteDataDao() : null;
    }

    public int getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}