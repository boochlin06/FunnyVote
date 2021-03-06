package com.heaton.funnyvote.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "VOTE_DATA".
*/
public class VoteDataDao extends AbstractDao<VoteData, Long> {

    public static final String TABLENAME = "VOTE_DATA";

    /**
     * Properties of entity VoteData.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property VoteCode = new Property(1, String.class, "voteCode", false, "VOTE_CODE");
        public final static Property Title = new Property(2, String.class, "title", false, "TITLE");
        public final static Property AuthorName = new Property(3, String.class, "authorName", false, "AUTHOR_NAME");
        public final static Property AuthorCode = new Property(4, String.class, "authorCode", false, "AUTHOR_CODE");
        public final static Property AuthorCodeType = new Property(5, String.class, "authorCodeType", false, "AUTHOR_CODE_TYPE");
        public final static Property AuthorIcon = new Property(6, String.class, "authorIcon", false, "AUTHOR_ICON");
        public final static Property VoteImage = new Property(7, String.class, "voteImage", false, "VOTE_IMAGE");
        public final static Property LocalImage = new Property(8, int.class, "localImage", false, "LOCAL_IMAGE");
        public final static Property StartTime = new Property(9, long.class, "startTime", false, "START_TIME");
        public final static Property EndTime = new Property(10, long.class, "endTime", false, "END_TIME");
        public final static Property Option1Title = new Property(11, String.class, "option1Title", false, "OPTION1_TITLE");
        public final static Property Option1Code = new Property(12, String.class, "option1Code", false, "OPTION1_CODE");
        public final static Property Option1Count = new Property(13, int.class, "option1Count", false, "OPTION1_COUNT");
        public final static Property Option1Polled = new Property(14, boolean.class, "option1Polled", false, "OPTION1_POLLED");
        public final static Property Option2Title = new Property(15, String.class, "option2Title", false, "OPTION2_TITLE");
        public final static Property Option2Code = new Property(16, String.class, "option2Code", false, "OPTION2_CODE");
        public final static Property Option2Count = new Property(17, int.class, "option2Count", false, "OPTION2_COUNT");
        public final static Property Option2Polled = new Property(18, boolean.class, "option2Polled", false, "OPTION2_POLLED");
        public final static Property OptionTopTitle = new Property(19, String.class, "optionTopTitle", false, "OPTION_TOP_TITLE");
        public final static Property OptionTopCode = new Property(20, String.class, "optionTopCode", false, "OPTION_TOP_CODE");
        public final static Property OptionTopCount = new Property(21, int.class, "optionTopCount", false, "OPTION_TOP_COUNT");
        public final static Property OptionTopPolled = new Property(22, boolean.class, "optionTopPolled", false, "OPTION_TOP_POLLED");
        public final static Property OptionUserChoiceTitle = new Property(23, String.class, "optionUserChoiceTitle", false, "OPTION_USER_CHOICE_TITLE");
        public final static Property OptionUserChoiceCode = new Property(24, String.class, "optionUserChoiceCode", false, "OPTION_USER_CHOICE_CODE");
        public final static Property OptionUserChoiceCount = new Property(25, int.class, "optionUserChoiceCount", false, "OPTION_USER_CHOICE_COUNT");
        public final static Property MinOption = new Property(26, int.class, "minOption", false, "MIN_OPTION");
        public final static Property MaxOption = new Property(27, int.class, "maxOption", false, "MAX_OPTION");
        public final static Property OptionCount = new Property(28, int.class, "optionCount", false, "OPTION_COUNT");
        public final static Property PollCount = new Property(29, int.class, "pollCount", false, "POLL_COUNT");
        public final static Property IsPolled = new Property(30, boolean.class, "isPolled", false, "IS_POLLED");
        public final static Property IsFavorite = new Property(31, boolean.class, "isFavorite", false, "IS_FAVORITE");
        public final static Property IsCanPreviewResult = new Property(32, boolean.class, "isCanPreviewResult", false, "IS_CAN_PREVIEW_RESULT");
        public final static Property IsUserCanAddOption = new Property(33, boolean.class, "isUserCanAddOption", false, "IS_USER_CAN_ADD_OPTION");
        public final static Property IsNeedPassword = new Property(34, boolean.class, "isNeedPassword", false, "IS_NEED_PASSWORD");
        public final static Property Security = new Property(35, String.class, "security", false, "SECURITY");
        public final static Property Category = new Property(36, String.class, "category", false, "CATEGORY");
        public final static Property DisplayOrder = new Property(37, int.class, "displayOrder", false, "DISPLAY_ORDER");
        public final static Property PollType = new Property(38, String.class, "pollType", false, "POLL_TYPE");
    }

    private DaoSession daoSession;


    public VoteDataDao(DaoConfig config) {
        super(config);
    }
    
    public VoteDataDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"VOTE_DATA\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"VOTE_CODE\" TEXT," + // 1: voteCode
                "\"TITLE\" TEXT," + // 2: title
                "\"AUTHOR_NAME\" TEXT," + // 3: authorName
                "\"AUTHOR_CODE\" TEXT," + // 4: authorCode
                "\"AUTHOR_CODE_TYPE\" TEXT," + // 5: authorCodeType
                "\"AUTHOR_ICON\" TEXT," + // 6: authorIcon
                "\"VOTE_IMAGE\" TEXT," + // 7: voteImage
                "\"LOCAL_IMAGE\" INTEGER NOT NULL ," + // 8: localImage
                "\"START_TIME\" INTEGER NOT NULL ," + // 9: startTime
                "\"END_TIME\" INTEGER NOT NULL ," + // 10: endTime
                "\"OPTION1_TITLE\" TEXT," + // 11: option1Title
                "\"OPTION1_CODE\" TEXT," + // 12: option1Code
                "\"OPTION1_COUNT\" INTEGER NOT NULL ," + // 13: option1Count
                "\"OPTION1_POLLED\" INTEGER NOT NULL ," + // 14: option1Polled
                "\"OPTION2_TITLE\" TEXT," + // 15: option2Title
                "\"OPTION2_CODE\" TEXT," + // 16: option2Code
                "\"OPTION2_COUNT\" INTEGER NOT NULL ," + // 17: option2Count
                "\"OPTION2_POLLED\" INTEGER NOT NULL ," + // 18: option2Polled
                "\"OPTION_TOP_TITLE\" TEXT," + // 19: optionTopTitle
                "\"OPTION_TOP_CODE\" TEXT," + // 20: optionTopCode
                "\"OPTION_TOP_COUNT\" INTEGER NOT NULL ," + // 21: optionTopCount
                "\"OPTION_TOP_POLLED\" INTEGER NOT NULL ," + // 22: optionTopPolled
                "\"OPTION_USER_CHOICE_TITLE\" TEXT," + // 23: optionUserChoiceTitle
                "\"OPTION_USER_CHOICE_CODE\" TEXT," + // 24: optionUserChoiceCode
                "\"OPTION_USER_CHOICE_COUNT\" INTEGER NOT NULL ," + // 25: optionUserChoiceCount
                "\"MIN_OPTION\" INTEGER NOT NULL ," + // 26: minOption
                "\"MAX_OPTION\" INTEGER NOT NULL ," + // 27: maxOption
                "\"OPTION_COUNT\" INTEGER NOT NULL ," + // 28: optionCount
                "\"POLL_COUNT\" INTEGER NOT NULL ," + // 29: pollCount
                "\"IS_POLLED\" INTEGER NOT NULL ," + // 30: isPolled
                "\"IS_FAVORITE\" INTEGER NOT NULL ," + // 31: isFavorite
                "\"IS_CAN_PREVIEW_RESULT\" INTEGER NOT NULL ," + // 32: isCanPreviewResult
                "\"IS_USER_CAN_ADD_OPTION\" INTEGER NOT NULL ," + // 33: isUserCanAddOption
                "\"IS_NEED_PASSWORD\" INTEGER NOT NULL ," + // 34: isNeedPassword
                "\"SECURITY\" TEXT," + // 35: security
                "\"CATEGORY\" TEXT," + // 36: category
                "\"DISPLAY_ORDER\" INTEGER NOT NULL ," + // 37: displayOrder
                "\"POLL_TYPE\" TEXT);"); // 38: pollType
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"VOTE_DATA\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, VoteData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String voteCode = entity.getVoteCode();
        if (voteCode != null) {
            stmt.bindString(2, voteCode);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String authorName = entity.getAuthorName();
        if (authorName != null) {
            stmt.bindString(4, authorName);
        }
 
        String authorCode = entity.getAuthorCode();
        if (authorCode != null) {
            stmt.bindString(5, authorCode);
        }
 
        String authorCodeType = entity.getAuthorCodeType();
        if (authorCodeType != null) {
            stmt.bindString(6, authorCodeType);
        }
 
        String authorIcon = entity.getAuthorIcon();
        if (authorIcon != null) {
            stmt.bindString(7, authorIcon);
        }
 
        String voteImage = entity.getVoteImage();
        if (voteImage != null) {
            stmt.bindString(8, voteImage);
        }
        stmt.bindLong(9, entity.getLocalImage());
        stmt.bindLong(10, entity.getStartTime());
        stmt.bindLong(11, entity.getEndTime());
 
        String option1Title = entity.getOption1Title();
        if (option1Title != null) {
            stmt.bindString(12, option1Title);
        }
 
        String option1Code = entity.getOption1Code();
        if (option1Code != null) {
            stmt.bindString(13, option1Code);
        }
        stmt.bindLong(14, entity.getOption1Count());
        stmt.bindLong(15, entity.getOption1Polled() ? 1L: 0L);
 
        String option2Title = entity.getOption2Title();
        if (option2Title != null) {
            stmt.bindString(16, option2Title);
        }
 
        String option2Code = entity.getOption2Code();
        if (option2Code != null) {
            stmt.bindString(17, option2Code);
        }
        stmt.bindLong(18, entity.getOption2Count());
        stmt.bindLong(19, entity.getOption2Polled() ? 1L: 0L);
 
        String optionTopTitle = entity.getOptionTopTitle();
        if (optionTopTitle != null) {
            stmt.bindString(20, optionTopTitle);
        }
 
        String optionTopCode = entity.getOptionTopCode();
        if (optionTopCode != null) {
            stmt.bindString(21, optionTopCode);
        }
        stmt.bindLong(22, entity.getOptionTopCount());
        stmt.bindLong(23, entity.getOptionTopPolled() ? 1L: 0L);
 
        String optionUserChoiceTitle = entity.getOptionUserChoiceTitle();
        if (optionUserChoiceTitle != null) {
            stmt.bindString(24, optionUserChoiceTitle);
        }
 
        String optionUserChoiceCode = entity.getOptionUserChoiceCode();
        if (optionUserChoiceCode != null) {
            stmt.bindString(25, optionUserChoiceCode);
        }
        stmt.bindLong(26, entity.getOptionUserChoiceCount());
        stmt.bindLong(27, entity.getMinOption());
        stmt.bindLong(28, entity.getMaxOption());
        stmt.bindLong(29, entity.getOptionCount());
        stmt.bindLong(30, entity.getPollCount());
        stmt.bindLong(31, entity.getIsPolled() ? 1L: 0L);
        stmt.bindLong(32, entity.getIsFavorite() ? 1L: 0L);
        stmt.bindLong(33, entity.getIsCanPreviewResult() ? 1L: 0L);
        stmt.bindLong(34, entity.getIsUserCanAddOption() ? 1L: 0L);
        stmt.bindLong(35, entity.getIsNeedPassword() ? 1L: 0L);
 
        String security = entity.getSecurity();
        if (security != null) {
            stmt.bindString(36, security);
        }
 
        String category = entity.getCategory();
        if (category != null) {
            stmt.bindString(37, category);
        }
        stmt.bindLong(38, entity.getDisplayOrder());
 
        String pollType = entity.getPollType();
        if (pollType != null) {
            stmt.bindString(39, pollType);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, VoteData entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String voteCode = entity.getVoteCode();
        if (voteCode != null) {
            stmt.bindString(2, voteCode);
        }
 
        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(3, title);
        }
 
        String authorName = entity.getAuthorName();
        if (authorName != null) {
            stmt.bindString(4, authorName);
        }
 
        String authorCode = entity.getAuthorCode();
        if (authorCode != null) {
            stmt.bindString(5, authorCode);
        }
 
        String authorCodeType = entity.getAuthorCodeType();
        if (authorCodeType != null) {
            stmt.bindString(6, authorCodeType);
        }
 
        String authorIcon = entity.getAuthorIcon();
        if (authorIcon != null) {
            stmt.bindString(7, authorIcon);
        }
 
        String voteImage = entity.getVoteImage();
        if (voteImage != null) {
            stmt.bindString(8, voteImage);
        }
        stmt.bindLong(9, entity.getLocalImage());
        stmt.bindLong(10, entity.getStartTime());
        stmt.bindLong(11, entity.getEndTime());
 
        String option1Title = entity.getOption1Title();
        if (option1Title != null) {
            stmt.bindString(12, option1Title);
        }
 
        String option1Code = entity.getOption1Code();
        if (option1Code != null) {
            stmt.bindString(13, option1Code);
        }
        stmt.bindLong(14, entity.getOption1Count());
        stmt.bindLong(15, entity.getOption1Polled() ? 1L: 0L);
 
        String option2Title = entity.getOption2Title();
        if (option2Title != null) {
            stmt.bindString(16, option2Title);
        }
 
        String option2Code = entity.getOption2Code();
        if (option2Code != null) {
            stmt.bindString(17, option2Code);
        }
        stmt.bindLong(18, entity.getOption2Count());
        stmt.bindLong(19, entity.getOption2Polled() ? 1L: 0L);
 
        String optionTopTitle = entity.getOptionTopTitle();
        if (optionTopTitle != null) {
            stmt.bindString(20, optionTopTitle);
        }
 
        String optionTopCode = entity.getOptionTopCode();
        if (optionTopCode != null) {
            stmt.bindString(21, optionTopCode);
        }
        stmt.bindLong(22, entity.getOptionTopCount());
        stmt.bindLong(23, entity.getOptionTopPolled() ? 1L: 0L);
 
        String optionUserChoiceTitle = entity.getOptionUserChoiceTitle();
        if (optionUserChoiceTitle != null) {
            stmt.bindString(24, optionUserChoiceTitle);
        }
 
        String optionUserChoiceCode = entity.getOptionUserChoiceCode();
        if (optionUserChoiceCode != null) {
            stmt.bindString(25, optionUserChoiceCode);
        }
        stmt.bindLong(26, entity.getOptionUserChoiceCount());
        stmt.bindLong(27, entity.getMinOption());
        stmt.bindLong(28, entity.getMaxOption());
        stmt.bindLong(29, entity.getOptionCount());
        stmt.bindLong(30, entity.getPollCount());
        stmt.bindLong(31, entity.getIsPolled() ? 1L: 0L);
        stmt.bindLong(32, entity.getIsFavorite() ? 1L: 0L);
        stmt.bindLong(33, entity.getIsCanPreviewResult() ? 1L: 0L);
        stmt.bindLong(34, entity.getIsUserCanAddOption() ? 1L: 0L);
        stmt.bindLong(35, entity.getIsNeedPassword() ? 1L: 0L);
 
        String security = entity.getSecurity();
        if (security != null) {
            stmt.bindString(36, security);
        }
 
        String category = entity.getCategory();
        if (category != null) {
            stmt.bindString(37, category);
        }
        stmt.bindLong(38, entity.getDisplayOrder());
 
        String pollType = entity.getPollType();
        if (pollType != null) {
            stmt.bindString(39, pollType);
        }
    }

    @Override
    protected final void attachEntity(VoteData entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public VoteData readEntity(Cursor cursor, int offset) {
        VoteData entity = new VoteData( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // voteCode
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // title
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // authorName
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // authorCode
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // authorCodeType
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // authorIcon
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // voteImage
            cursor.getInt(offset + 8), // localImage
            cursor.getLong(offset + 9), // startTime
            cursor.getLong(offset + 10), // endTime
            cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // option1Title
            cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12), // option1Code
            cursor.getInt(offset + 13), // option1Count
            cursor.getShort(offset + 14) != 0, // option1Polled
            cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // option2Title
            cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16), // option2Code
            cursor.getInt(offset + 17), // option2Count
            cursor.getShort(offset + 18) != 0, // option2Polled
            cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19), // optionTopTitle
            cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // optionTopCode
            cursor.getInt(offset + 21), // optionTopCount
            cursor.getShort(offset + 22) != 0, // optionTopPolled
            cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23), // optionUserChoiceTitle
            cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24), // optionUserChoiceCode
            cursor.getInt(offset + 25), // optionUserChoiceCount
            cursor.getInt(offset + 26), // minOption
            cursor.getInt(offset + 27), // maxOption
            cursor.getInt(offset + 28), // optionCount
            cursor.getInt(offset + 29), // pollCount
            cursor.getShort(offset + 30) != 0, // isPolled
            cursor.getShort(offset + 31) != 0, // isFavorite
            cursor.getShort(offset + 32) != 0, // isCanPreviewResult
            cursor.getShort(offset + 33) != 0, // isUserCanAddOption
            cursor.getShort(offset + 34) != 0, // isNeedPassword
            cursor.isNull(offset + 35) ? null : cursor.getString(offset + 35), // security
            cursor.isNull(offset + 36) ? null : cursor.getString(offset + 36), // category
            cursor.getInt(offset + 37), // displayOrder
            cursor.isNull(offset + 38) ? null : cursor.getString(offset + 38) // pollType
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, VoteData entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setVoteCode(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setTitle(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setAuthorName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setAuthorCode(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAuthorCodeType(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAuthorIcon(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setVoteImage(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setLocalImage(cursor.getInt(offset + 8));
        entity.setStartTime(cursor.getLong(offset + 9));
        entity.setEndTime(cursor.getLong(offset + 10));
        entity.setOption1Title(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setOption1Code(cursor.isNull(offset + 12) ? null : cursor.getString(offset + 12));
        entity.setOption1Count(cursor.getInt(offset + 13));
        entity.setOption1Polled(cursor.getShort(offset + 14) != 0);
        entity.setOption2Title(cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15));
        entity.setOption2Code(cursor.isNull(offset + 16) ? null : cursor.getString(offset + 16));
        entity.setOption2Count(cursor.getInt(offset + 17));
        entity.setOption2Polled(cursor.getShort(offset + 18) != 0);
        entity.setOptionTopTitle(cursor.isNull(offset + 19) ? null : cursor.getString(offset + 19));
        entity.setOptionTopCode(cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20));
        entity.setOptionTopCount(cursor.getInt(offset + 21));
        entity.setOptionTopPolled(cursor.getShort(offset + 22) != 0);
        entity.setOptionUserChoiceTitle(cursor.isNull(offset + 23) ? null : cursor.getString(offset + 23));
        entity.setOptionUserChoiceCode(cursor.isNull(offset + 24) ? null : cursor.getString(offset + 24));
        entity.setOptionUserChoiceCount(cursor.getInt(offset + 25));
        entity.setMinOption(cursor.getInt(offset + 26));
        entity.setMaxOption(cursor.getInt(offset + 27));
        entity.setOptionCount(cursor.getInt(offset + 28));
        entity.setPollCount(cursor.getInt(offset + 29));
        entity.setIsPolled(cursor.getShort(offset + 30) != 0);
        entity.setIsFavorite(cursor.getShort(offset + 31) != 0);
        entity.setIsCanPreviewResult(cursor.getShort(offset + 32) != 0);
        entity.setIsUserCanAddOption(cursor.getShort(offset + 33) != 0);
        entity.setIsNeedPassword(cursor.getShort(offset + 34) != 0);
        entity.setSecurity(cursor.isNull(offset + 35) ? null : cursor.getString(offset + 35));
        entity.setCategory(cursor.isNull(offset + 36) ? null : cursor.getString(offset + 36));
        entity.setDisplayOrder(cursor.getInt(offset + 37));
        entity.setPollType(cursor.isNull(offset + 38) ? null : cursor.getString(offset + 38));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(VoteData entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(VoteData entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(VoteData entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
