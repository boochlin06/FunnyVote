package com.heaton.funnyvote.data.VoteData;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.heaton.funnyvote.database.DaoMaster;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.database.VoteDataDao;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.heaton.funnyvote.FunnyVoteApplication.ENCRYPTED;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class VoteDataDaoTest {
    private static final VoteData voteData = new VoteData();
    private Database database;

    private DaoSession daoSession;

    @Before
    public void initDb() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(InstrumentationRegistry.getTargetContext()
                , ENCRYPTED ? "votes-db-encrypted" : "votes-db-test");
        database = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(database).newSession(IdentityScopeType.Session);
    }

    @After
    public void closeDb() {
        daoSession.clear();
        database.close();
    }
//
//    @Test
//    public void insertVoteDataAndGetById() {
//        daoSession.getVoteDataDao().deleteAll();
//        VoteData newVoteData = newVoteData(31);
//        daoSession.getVoteDataDao().insert(newVoteData);
//        List<VoteData> loaded = daoSession.getVoteDataDao().queryBuilder()
//                .where(VoteDataDao.Properties.VoteCode.eq(newVoteData.getVoteCode())).list();
//        assertThat(loaded.size(), is(1));
//        assertVoteData(loaded.get(0), newVoteData.getId(), newVoteData.getTitle(), newVoteData.getVoteCode());
//    }
//
//    @Test
//    public void updateVoteDataAndGetById() {
//        voteData.setVoteCode("CODE_111");
//        voteData.setTitle("TITLE_111");
//        voteData.setId((long) 111);
//        daoSession.getVoteDataDao().insert(voteData);
//        voteData.setTitle("TITLE_UPDATE_111");
//        daoSession.getVoteDataDao().update(voteData);
//        VoteData loaded = daoSession.getVoteDataDao().load((long) 111);
//        assertVoteData(loaded, (long) 111, "TITLE_UPDATE_111", "CODE_111");
//
//    }
//
//    @Test
//    public void deleteVoteDataByIdAndGettingVoteData() {
//        daoSession.getVoteDataDao().deleteAll();
//        daoSession.getVoteDataDao().insert(voteData);
//        daoSession.getVoteDataDao().deleteByKey(voteData.getId());
//        List<VoteData> list = daoSession.getVoteDataDao().queryBuilder().list();
//        assertThat(list.size(), is(0));
//    }
//
//    private void assertVoteData(VoteData voteData, Long id, String title,
//                                String voteCode) {
//        Assert.assertThat(voteData, notNullValue());
//        Assert.assertThat(voteData.getId(), is(id));
//        Assert.assertThat(voteData.getTitle(), is(title));
//        Assert.assertThat(voteData.getVoteCode(), is(voteCode));
//    }
//
//    private VoteData newVoteData(int index) {
//        VoteData newVoteData = new VoteData();
//        List<Option> netOption = Lists.newArrayListWithCapacity(2);
//        newVoteData.setVoteCode("VoteDataDaoTest_CODE_" + index);
//        newVoteData.setTitle("VoteDataDaoTest_TITLE_" + index);
//        newVoteData.setNetOptions(netOption);
//        newVoteData.setStartTime(System.currentTimeMillis() - 3600000);
//        return newVoteData;
//    }
}
