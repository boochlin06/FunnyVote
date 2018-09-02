package com.heaton.funnyvote.data.Promotion;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.google.common.collect.Lists;
import com.heaton.funnyvote.data.promotion.LocalPromotionSource;
import com.heaton.funnyvote.data.promotion.PromotionDataSource;
import com.heaton.funnyvote.database.DaoMaster;
import com.heaton.funnyvote.database.DaoSession;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.PromotionDao;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.utils.AppExecutors;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static com.heaton.funnyvote.FunnyVoteApplication.ENCRYPTED;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LocalPromotionSourceTest {
    private LocalPromotionSource localPromotionSource;
    private static User user;
    private PromotionDao promotionDao;
    private DaoSession daoSession;

    @Before
    public void setUp() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(InstrumentationRegistry.getTargetContext()
                , Companion.getENCRYPTED() ? "votes-db-encrypted" : "votes-db");
        Database db = Companion.getENCRYPTED() ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession(IdentityScopeType.Session);
        promotionDao = daoSession.getPromotionDao();
        LocalPromotionSource.Companion.clearInstance();
        localPromotionSource = LocalPromotionSource.Companion.getInstance(promotionDao, AppExecutors.Companion.getInstance());
        user = new User();
    }

    @After
    public void cleanUp() {
        LocalPromotionSource.Companion.clearInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(localPromotionSource);
    }

    @Test
    public void savePromotions_retrievesPromotions() {
        final Promotion promotion1 = new Promotion();
        promotion1.setId((long) 1000);

        localPromotionSource.savePromotionList(Lists.newArrayList(promotion1));
        localPromotionSource.getPromotionList(user, new PromotionDataSource.GetPromotionsCallback() {
            @Override
            public void onPromotionsLoaded(List<Promotion> promotionList) {
                assertNotNull(promotionList);
                assertTrue(promotionList.size() > 0);
                boolean promotion1IdFound = false;
                for (Promotion promotion : promotionList) {
                    if (promotion1.getId() == promotion.getId()) {
                        promotion1IdFound = true;
                    }
                }
                assertTrue(promotion1IdFound);
            }

            @Override
            public void onPromotionsNotAvailable() {
                fail();
            }

        });
    }
}
