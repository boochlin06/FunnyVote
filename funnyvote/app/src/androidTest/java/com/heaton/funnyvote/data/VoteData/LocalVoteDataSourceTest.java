package com.heaton.funnyvote.data.VoteData;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.heaton.funnyvote.database.DaoMaster;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.OptionDao;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.database.VoteDataDao;
import com.heaton.funnyvote.ui.main.MainPageTabFragment;
import com.heaton.funnyvote.utils.AppExecutors;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.heaton.funnyvote.FunnyVoteApplication.ENCRYPTED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalVoteDataSourceTest {

    private LocalVoteDataSource localVoteDataSource;
    private VoteDataDao voteDataDao;
    private DaoSession daoSession;
    private User user;

    @Before
    public void setup() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(InstrumentationRegistry.getTargetContext()
                , ENCRYPTED ? "votes-db-encrypted" : "votes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession(IdentityScopeType.Session);
        voteDataDao = daoSession.getVoteDataDao();
        OptionDao optionDao = daoSession.getOptionDao();
        LocalVoteDataSource.clearInstance();
        localVoteDataSource = LocalVoteDataSource.getInstance(voteDataDao, optionDao, AppExecutors.getInstance());
        user = new User();
    }
//
//    @After
//    public void cleanUp() {
//        LocalVoteDataSource.clearInstance();
//        daoSession.clear();
//    }
//
//    @Test
//    public void testPreConditions() {
//        assertNotNull(localVoteDataSource);
//    }
//
//    @Test
//    public void saveVoteData_retrievesVoteData() {
//        final VoteData newVoteData = newVoteData(0);
//
//        localVoteDataSource.saveVoteData(newVoteData);
//        localVoteDataSource.getVoteData(newVoteData.getVoteCode(), user, new VoteDataSource.GetVoteDataCallback() {
//            @Override
//            public void onVoteDataLoaded(VoteData voteData) {
//                assertThat(voteData, is(newVoteData));
//                assertThat(voteData.getVoteCode(), is(newVoteData.getVoteCode()));
//            }
//
//            @Override
//            public void onVoteDataNotAvailable() {
//                fail("Callback error");
//            }
//        });
//    }
//
//    @Test
//    public void favoriteVoteData_retrievesVoteDataIsFavorite() {
//        VoteDataSource.FavoriteVoteCallback favoriteVoteCallback = mock(VoteDataSource.FavoriteVoteCallback.class);
//
//        final boolean expectIsFavorite = true;
//        final VoteData newVoteData = newVoteData(100);
//        localVoteDataSource.saveVoteData(newVoteData);
//        localVoteDataSource.favoriteVote(newVoteData.getVoteCode(), expectIsFavorite, user, favoriteVoteCallback);
//        localVoteDataSource.getVoteData(newVoteData.getVoteCode(), user, new VoteDataSource.GetVoteDataCallback() {
//            @Override
//            public void onVoteDataLoaded(VoteData voteData) {
//                assertThat(voteData.getVoteCode(), is(newVoteData.getVoteCode()));
//                assertThat(voteData.getIsFavorite(), is(expectIsFavorite));
//            }
//
//            @Override
//            public void onVoteDataNotAvailable() {
//                fail("Callback error");
//            }
//        });
//    }
//
//    @Test
//    public void getVotes_retrieveSaveVoteData() {
//        final VoteData voteData1 = newVoteData(0);
//        final VoteData voteData2 = newVoteData(10);
//        localVoteDataSource.saveVoteDataList(Lists.newArrayList(voteData1, voteData2), 0
//                , MainPageTabFragment.TAB_NEW);
//        localVoteDataSource.getNewVoteList(0, user, new VoteDataSource.GetVoteListCallback() {
//            @Override
//            public void onVoteListLoaded(List<VoteData> voteDataList) {
//                assertNotNull(voteDataList);
//                assertTrue(voteDataList.size() >= 2);
//                boolean newVoteData1IdFound = false;
//                boolean newVoteData2IdFound = false;
//                for (VoteData voteData : voteDataList) {
//                    if (voteData.getVoteCode().equals(voteData1.getVoteCode())) {
//                        newVoteData1IdFound = true;
//                    }
//                    if (voteData.getVoteCode().equals(voteData2.getVoteCode())) {
//                        newVoteData2IdFound = true;
//                    }
//                }
//                assertTrue(newVoteData1IdFound);
//                assertTrue(newVoteData2IdFound);
//            }
//
//            @Override
//            public void onVoteListNotAvailable() {
//                fail();
//            }
//        });
//    }
//
//    @Test
//    public void getOptions_retrieveSaveOption() {
//
//        final VoteData voteData = newVoteData(1);
//        final Option option1 = newOption(voteData.getVoteCode(), 1);
//        final Option option2 = newOption(voteData.getVoteCode(), 2);
//        List<Option> options = Lists.newArrayList(option1, option2);
//        localVoteDataSource.saveOptions(options);
//        localVoteDataSource.getOptions(voteData, new VoteDataSource.GetVoteOptionsCallback() {
//            @Override
//            public void onVoteOptionsLoaded(List<Option> optionList) {
//                assertNotNull(optionList);
//                assertTrue(optionList.size() >= 2);
//                boolean newOption1IdFound = false;
//                boolean newOption2IdFound = false;
//                for (Option option : optionList) {
//                    if (option.getVoteCode().equals(voteData.getVoteCode())) {
//                        if (option.getCode().equals(option2.getCode())) {
//                            newOption2IdFound = true;
//                        }
//                        if (option.getCode().equals(option1.getCode())) {
//                            newOption1IdFound = true;
//                        }
//                    }
//
//                }
//                assertTrue(newOption1IdFound);
//                assertTrue(newOption2IdFound);
//            }
//
//            @Override
//            public void onVoteOptionsNotAvailable() {
//                fail();
//            }
//
//        });
//    }
//
//    private VoteData newVoteData(int index) {
//        VoteData newVoteData = new VoteData();
//        List<Option> netOption = Lists.newArrayListWithCapacity(2);
//        newVoteData.setVoteCode("CODE_" + index);
//        newVoteData.setTitle("TITLE_" + index);
//        newVoteData.setNetOptions(netOption);
//        newVoteData.setStartTime(System.currentTimeMillis() - 3600000);
//        return newVoteData;
//    }
//
//    private Option newOption(String voteCode, int index) {
//        Option newOption = new Option();
//        newOption.setVoteCode(voteCode);
//        newOption.setTitle("TITLE_" + index);
//        newOption.setCode("Option_" + index);
//        return newOption;
//    }
}
