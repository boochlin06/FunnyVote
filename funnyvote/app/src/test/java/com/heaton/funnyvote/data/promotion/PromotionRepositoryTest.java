package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;
import com.heaton.funnyvote.database.VoteData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.observers.TestSubscriber;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionRepositoryTest {

    @Mock
    private PromotionDataSource localPromotionDataSource;
    @Mock
    private PromotionDataSource remotePromotionDataSource;

    private PromotionRepository promotionRepository;

    private static User user = new User();
    private static List<Promotion> promotionList = new ArrayList<>();

    @Before
    public void setUpPromotionRepository() {
        MockitoAnnotations.initMocks(this);
        promotionRepository = PromotionRepository.getInstance(remotePromotionDataSource, localPromotionDataSource);
    }

    @After
    public void destroyRepositoryInstance() {
        PromotionRepository.destroyInstance();
    }

    @Test
    public void getPromotionList_getFromRemoteAndSaveToLocal() {
        when(remotePromotionDataSource.getPromotionList(eq(user))).thenReturn(rx.Observable.just(promotionList));
        when(localPromotionDataSource.getPromotionList(eq(user))).thenReturn(rx.Observable.error(new Exception()));
        when(promotionRepository.getPromotionList(user)).thenReturn(rx.Observable.just(promotionList));

        TestSubscriber<List<Promotion>> testSubscriber1 = new TestSubscriber<>();
        TestSubscriber<List<Promotion>> testSubscriber2 = new TestSubscriber<>();


        promotionRepository.getPromotionList(user).subscribe(testSubscriber1);
        verify(remotePromotionDataSource).getPromotionList(eq(user));
        //getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(anyList());
        //verify(localPromotionDataSource, times(2)).savePromotionList(promotionList);

    }

    @Test
    public void getPromotionListWithRemoteFailure_getFromLocal() {
        when(localPromotionDataSource.getPromotionList(eq(user))).thenReturn(rx.Observable.just(promotionList));
        when(remotePromotionDataSource.getPromotionList(user)).thenReturn(rx.Observable.error(new Exception()));
        when(promotionRepository.getPromotionList(user)).thenReturn(rx.Observable.just(promotionList));
        promotionRepository.getPromotionList(user);
        verify(remotePromotionDataSource).getPromotionList(eq(user));
        //getPromotionsCallbackArgumentCaptor.getValue().onPromotionsNotAvailable();
        verify(localPromotionDataSource, never()).savePromotionList(eq(promotionList));
        verify(localPromotionDataSource, times(2)).getPromotionList(eq(user));
        //getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(promotionList);
        //verify(getPromotionsCallback).onPromotionsLoaded(promotionList);
    }
}
