package com.android.heaton.funnyvote.database;

import android.content.Context;

import com.android.heaton.funnyvote.FunnyVoteApplication;
import com.android.heaton.funnyvote.R;
import com.android.heaton.funnyvote.ui.UserSharepreferenceController;

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

    public void mockVoteData(int limit, int maxOptionCount) {
        List<VoteData> list = new ArrayList<>();
        List<Option> options = new ArrayList<>();
        String[] IMAGE_URL = context.getResources().getStringArray(R.array.imageURL);
        for (int i = 0; i < limit; i++) {
            VoteData data = new VoteData();
            data.setIsCanPreviewResult(true);
            data.setVoteCode(System.currentTimeMillis() + "_" + Integer.toString(i));
            data.setAuthorName("Heaton" + i);
            data.setAuthorCode("AuthorMock_" + i);
            data.setAuthorIcon("");
            data.setVoteLink("https://vinta.ws/booch/");

            data.setIsFavorite(false);
            data.setIsNeedPassword(false);
            data.setIsPolled(false);
            data.setCategory("hot");

            data.setMaxOption(1);
            data.setMinOption(1);
            data.setIsUserCanAddOption(true);

            data.setSecurity(VoteData.SECURITY_PUBLIC);
            data.setTitle("Do your mother know what do you do?:" + i);
            data.setStartTime(System.currentTimeMillis() - 864200 * 1000);
            int randomImage = (int) (Math.random() * 4);
            int image;
            switch (randomImage) {
                case 1:
                    image = R.drawable.vote_finger;
                    break;
                case 2:
                    image = R.drawable.vote_box;
                    break;
                case 3:
                    image = R.drawable.handsup;
                    break;
                default:
                    image = R.drawable.ballot_box;

            }
            data.setVoteImage(IMAGE_URL[randomImage]);
            data.setLocalImage(image);

            int optionType = i % 12;
            if (optionType == 0) {
                // TWO POLL TYPE
                data.setTitle("TWO POLL TYPE: option 1 , top are the same.");
                data.setIsPolled(false);
                data.setOptionCount(2);

                data.setOption1Code(i + "" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option 1 and top is the same
                data.setOptionTopCode(i + "" + 0);
                data.setOptionTopTitle("option 1 mock:" + i);

                data.setIsUserCanAddOption(false);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);
                options.addAll(mockOptionData(data, maxOptionCount, 0, -1));
            } else if (optionType == 1) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 2 , top are the same.");
                data.setIsPolled(false);

                data.setOptionCount(3);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option 2 and top is the same
                data.setOptionTopCode(i + "_" + 2);
                data.setOptionTopTitle("option 2 title:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 1, -1));
            } else if (optionType == 2) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 1, 2 is not the same with top. top is 4");
                data.setIsPolled(false);

                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title :" + i);
                // option 1,2 is NOT the same with top.
                data.setOptionTopTitle("option top title mock:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 3, -1));
            } else if (optionType == 3) {
                // TOP 1 TYPE.

                data.setTitle("TOP 1 TYPE: time end and user choiced option that not 1" +
                        " and 2 , is 5, top is 4 long title long title long title long title" +
                        " long title long title long title long title long title long title");
                data.setIsPolled(true);

                data.setOptionCount(15);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice are the same with top.
                data.setOptionTopCode("option_user_code_mock");
                data.setOptionTopTitle("option user mock:" + i);

                // time end and user choiced option that not 1 and 2.
                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
                data.setOptionUserChoiceCode("option_user_code_mock");
                data.setOptionUserChoiceTitle("option user mock" + i);

                options.addAll(mockOptionData(data, maxOptionCount, 3, 4));

            } else if (optionType == 4) {

                // TOP 1 TYPE.

                data.setTitle("TOP 1 TYPE: time end and user not choiced.top is 3");
                data.setIsPolled(false);

                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);

                // time end and user choiced.
                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 2, -1));
            } else if (optionType == 5) {
                // TOP 2 TYPE.
                data.setTitle("TOP 2 TYPE: time end and user choiced. user choice is 4 , top is 3");
                data.setIsPolled(true);

                data.setOptionCount(5);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
                options.addAll(mockOptionData(data, maxOptionCount, 2, 3));
            } else if (optionType == 6) {

                // TWO POLL TYPE
                data.setTitle("TWO POLL TYPE: no one poll , time is not end");
                data.setIsPolled(false);

                // NO ONE POLL
                data.setPollCount(0);

                data.setOptionCount(2);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option top is null

                options.addAll(mockOptionData(data, 0, -1, -1));

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);
            } else if (optionType == 7) {
                // TOP 2 TYPE.
                data.setTitle("TOP 2 TYPE: time not end and user choiced. user choice is 1 , top is 2");
                data.setIsPolled(true);

                data.setPollCount(5);
                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);
                data.setOptionTopCount(3);

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 1, 0));
            } else if (optionType == 8) {
                // TOP 1 TYPE.
                data.setTitle("TOP 1 TYPE: time not end and user choiced. user choice and top is 3");
                data.setIsPolled(true);

                data.setPollCount(5);
                data.setOptionCount(4);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option user choice NOT the same with top.
                data.setOptionTopCode("option_top_code_mock");
                data.setOptionTopTitle("option top mock:" + i);
                data.setOptionTopCount(3);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 2, 2));
            } else if (optionType == 9) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 2 , top is 3. , multi-choice");
                data.setIsPolled(false);

                data.setPollCount(5);
                data.setOptionCount(5);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption1Count(1);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                data.setOption2Count(3);
                // option 2 and top is the same
                data.setOptionTopCode(i + "_" + 2);
                data.setOptionTopTitle("option 2 mock:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                data.setMinOption(2);
                data.setMaxOption(2);

                options.addAll(mockOptionData(data, maxOptionCount, 2, -1));
            } else if (optionType == 10) {
                // ONE POLL TYPE
                data.setTitle("ONE POLL TYPE: no one poll , tiem is end");
                data.setIsPolled(false);

                // NO ONE POLL
                data.setPollCount(0);

                data.setOptionCount(2);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 mock:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);

                options.addAll(mockOptionData(data, 0, -1, -1));

                data.setEndTime(System.currentTimeMillis() - 7 * 86400 * 1000);
            } else if (optionType == 11) {
                // TWO MORE TYPE
                data.setTitle("TWO MORE TYPE: option 2 , top are the same. nedd password");
                data.setIsPolled(false);
                data.setIsNeedPassword(true);

                data.setOptionCount(3);
                data.setOption1Code(i + "_" + 0);
                data.setOption1Title("option 1 title:" + i);
                data.setOption2Code(i + "_" + 2);
                data.setOption2Title("option 2 title:" + i);
                // option 2 and top is the same
                data.setOptionTopCode(i + "_" + 2);
                data.setOptionTopTitle("option 2 title:" + i);

                data.setEndTime(System.currentTimeMillis() + 7 * 86400 * 1000);

                options.addAll(mockOptionData(data, maxOptionCount, 1, -1));
            }
            list.add(data);
        }
        voteDataDao.insertInTx(list);
        optionDao.insertInTx(options);
    }

    public void mockPromotions(int limit) {
        String imageURL[] = context.getResources().getStringArray(R.array.imageURL);
        List<Promotion> promotions = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Promotion promotion = new Promotion();
            promotion.setImageURL(imageURL[i % imageURL.length]);
            promotion.setActionURL("https://vinta.ws/booch/?p=226");
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
        // TODO : SECURITY AND HOT CLASS
        return voteDataDao.queryBuilder().offset(offset).limit(limit).list();
    }

    public long queryHotVotesCount() {
        return voteDataDao.queryBuilder().buildCount().count();
    }

    public long queryFavoriteVotesCount() {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsFavorite.eq(1)).buildCount().count();
    }
    public long queryVoteDataByAuthorCount(String authorCode) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.AuthorCode.eq(authorCode)).buildCount().count();
    }
    public List<VoteData> queryFavoriteVotes(int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsFavorite.eq(1)).offset(offset).limit(limit).list();
    }

    public List<VoteData> queryNewVotes(int offset, int limit) {
        // TODO: SECURITY HOT AND TIME
        return voteDataDao.queryBuilder().orderDesc(VoteDataDao.Properties.StartTime).orderDesc().offset(offset).limit(limit).list();
    }

    public List<Option> queryOptionsByVoteCode(String voteCode) {
        return optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).list();
    }

    public List<Option> queryOptionsByVoteCode(String voteCode, int limit) {
        return optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteCode)).limit(limit).list();
    }

    public VoteData queryVoteDataById(String code) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(code)).list().get(0);
    }

    public List<VoteData> queryVoteDataByAuthor(String authorCode, int offset, int limit) {
        return voteDataDao.queryBuilder().where(VoteDataDao.Properties.AuthorCode.eq(authorCode))
                .limit(limit).offset(offset).orderDesc(VoteDataDao.Properties.StartTime).list();
    }

    public User getUser() {
        List<User> userList = userDao.loadAll();
        return userList.size() == 0 ? null : userList.get(0);
    }

    public void initTempUser() {
        User user = new User();
        user.setUserCode(Long.toString(System.currentTimeMillis()));
        user.setUserName(context.getString(R.string.account_default_name));
        user.setUserIcon("");
        user.setType(User.TYPE_TEMP);
        user.setEmail("");
        UserSharepreferenceController.updtaeUser(context, user);
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


    public List<Promotion> queryAllPromotion() {
        return promotionDao.loadAll();
    }

}
