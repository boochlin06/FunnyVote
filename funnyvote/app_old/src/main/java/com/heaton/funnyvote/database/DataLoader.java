package com.heaton.funnyvote.database;

import android.content.Context;
import android.text.TextUtils;

import com.heaton.funnyvote.FunnyVoteApplication;
import com.heaton.funnyvote.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heaton on 16/4/7.
 */
public class DataLoader {
    private static DataLoader sInstance;
    private VoteDataDao voteDataDao;
    private OptionDao optionDao;
    private PromotionDao promotionDao;
    private UserDao userDao;
    private Context context;

    public DataLoader(Context context) {
        voteDataDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getVoteDataDao();
        optionDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getOptionDao();
        promotionDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getPromotionDao();
        userDao = ((FunnyVoteApplication) (context.getApplicationContext()))
                .getDaoSession().getUserDao();
        this.context = context;
    }

    public static synchronized DataLoader getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DataLoader(context);
        }
        return sInstance;
    }

    public VoteDataDao getVoteDataDao() {
        return voteDataDao;
    }

    public OptionDao getOptionDao() {
        return optionDao;
    }

    public PromotionDao getPromotionDao() {
        return promotionDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public List<Option> loadAllOption() {
        return optionDao.loadAll();
    }


    public List<Option> mockOptionData(VoteData data, int maxOptionCount, int optionTop
            , int optionUserChoice) {
        List<Option> options = new ArrayList<>();
        int pollCount = 0;
        int topCount = maxOptionCount + 1;
        for (int i = 0; i < data.getOptionCount(); i++) {
            Option option = new Option();
            int randomOptionCount = (int) (Math.random() * maxOptionCount);
            if (maxOptionCount == 0) {
                randomOptionCount = 0;
                topCount = 0;
            }
            option.setVoteCode(data.getVoteCode());
            if (i == 0) {
                data.setOption1Count(randomOptionCount);
                option.setTitle(data.getOption1Title());
                option.setCode(data.getOption1Code());
                option.setCount(data.getOption1Count());
            } else if (i == 1) {
                data.setOption2Count(randomOptionCount);
                option.setTitle(data.getOption2Title());
                option.setCode(data.getOption2Code());
                option.setCount(data.getOption2Count());
            } else {
                option.setTitle("Option mock :" + data.getVoteCode() + "_" + i);
                option.setCount(randomOptionCount);
                option.setCode(data.getVoteCode() + "_" + i);
            }
            if (optionTop == i) {
                if (optionTop == 0) {
                    data.setOption1Count(topCount);
                } else if (optionTop == 1) {
                    data.setOption2Count(topCount);
                }
                option.setCount(topCount);
                data.setOptionTopTitle(option.getTitle());
                data.setOptionTopCode(option.getCode());
                data.setOptionTopCount(topCount);

            }
            if (optionUserChoice == i) {
                data.setOptionUserChoiceCode(option.getCode());
                data.setOptionUserChoiceTitle(option.getTitle());
                data.setOptionUserChoiceCount(option.getCount());
            }
            pollCount = option.getCount() + pollCount;
            options.add(option);
        }
        data.setPollCount(pollCount);
        return options;
    }

    public void mockPromotions(int limit) {
        String imageURL[] = context.getResources().getStringArray(R.array.imageURL);
        List<Promotion> promotions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Promotion promotion = new Promotion();
            promotion.setImageURL(imageURL[i % imageURL.length]);
            promotion.setActionURL("https://play.google.com/store/apps/details?id=com.heaton.funnyvote");
            promotion.setTitle("title:" + i);
            promotions.add(promotion);
        }
        promotionDao.insertInTx(promotions);
    }

    public List<VoteData> queryCreateByVotes(int limit) {
        List<VoteData> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.setTitle("Do your mother know what do you do?:" + i);
            data.setPollCount(i);
            list.add(data);
        }
        return list;
    }

    public List<VoteData> queryHotVotes(int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.Category.eq("hot")
                , VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).offset(offset)
                .orderAsc(VoteDataDao.Properties.DisplayOrder)
                .limit(limit).list();
    }

    public long queryHotVotesCount() {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.Category.eq("hot")).buildCount().count();
    }

    public long queryFavoriteVotesCount() {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsFavorite.eq(1)).buildCount().count();
    }

    public long queryVoteDataByAuthorCount(String authorCode) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.AuthorCode.eq(authorCode)).buildCount().count();
    }

    public long queryVoteDataByParticipateCount() {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsPolled.eq(true)).buildCount().count();
    }

    public List<VoteData> queryFavoriteVotes(int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsFavorite.eq(true)).offset(offset).limit(limit).list();
    }

    public List<VoteData> querySearchVotes(String keyword, int offset, int limit) {
        if (TextUtils.isEmpty(keyword)) {
            return new ArrayList<VoteData>();
        }
        return voteDataDao.queryBuilder().whereOr(VoteDataDao.Properties.Title.like(keyword)
                , VoteDataDao.Properties.AuthorName.like(keyword)).orderDesc(VoteDataDao.Properties.StartTime)
                .offset(offset).limit(limit).list();
    }

    public List<VoteData> queryNewVotes(int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis()))
                .orderDesc(VoteDataDao.Properties.StartTime)
                .orderDesc().offset(offset).limit(limit).list();
    }

    public long queryNewVotesCount() {
        return voteDataDao.queryBuilder().buildCount().count();
    }

    public List<Option> queryOptionsByVoteCode(String voteCode) {
        return optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).list();
    }

    public List<Option> queryOptionsByVoteCode(String voteCode, int limit) {
        return optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).limit(limit).list();
    }

    public VoteData queryVoteDataById(String code) {
        if (TextUtils.isEmpty(code)) {
            VoteData voteData = new VoteData();
            voteData.setVoteCode(code);
            return voteData;
        }
        List<VoteData> list = voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(code)).list();
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public List<VoteData> queryVoteDataByAuthor(String authorCode, int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.AuthorCode.eq(authorCode))
                .limit(limit).offset(offset).orderDesc(VoteDataDao.Properties.StartTime).list();
    }

    public List<VoteData> queryVoteDataByParticipate(int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsPolled.eq(true))
                .limit(limit).offset(offset).orderDesc(VoteDataDao.Properties.StartTime).list();
    }

    public long countUserCreateOrParticipate(String authorCode) {
        return voteDataDao.queryBuilder().whereOr(VoteDataDao.Properties.IsPolled.eq(true)
                , VoteDataDao.Properties.AuthorCode.eq(authorCode))
                .where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).count();
    }

    public void updateVoteByVoteCode(String voteCode, VoteData data) {

        List<VoteData> list = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).list();

        if (list.size() > 0) {
            VoteData voteData = list.get(0);
            data.setId(voteData.getId());
            voteDataDao.update(data);
        }
    }

    public User getUser() {
        List<User> userList = userDao.loadAll();
        return userList.size() == 0 ? null : userList.get(0);
    }

    public void linkTempUserToLoginUser(String oldUserCode, User newUser) {
        List<VoteData> dataList = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.AuthorCode.eq(oldUserCode)).list();
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).setAuthorCode(newUser.getUserCode());
            dataList.get(i).setAuthorIcon(newUser.getUserIcon());
            dataList.get(i).setAuthorName(newUser.getUserName());
        }
        voteDataDao.updateInTx(dataList);
    }

    public void updateUserName(String code, String userName) {
        List<VoteData> dataList = voteDataDao.queryBuilder()
                .where(VoteDataDao.Properties.AuthorCode.eq(code)).list();
        for (int i = 0; i < dataList.size(); i++) {
            dataList.get(i).setAuthorName(userName);
        }
        voteDataDao.updateInTx(dataList);
    }

    public void deleteVoteDataAndOption(String voteCode) {
        voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(voteCode)).buildDelete()
                .executeDeleteWithoutDetachingEntities();
        optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }


    public List<Promotion> queryAllPromotion() {
        return promotionDao.loadAll();
    }

}
