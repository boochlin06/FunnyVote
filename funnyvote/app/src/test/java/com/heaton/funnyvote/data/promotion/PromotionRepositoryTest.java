package com.heaton.funnyvote.data.promotion;

import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PromotionRepositoryTest {

    @Mock
    private PromotionDataSource localPromotionDataSource;
    @Mock
    private PromotionDataSource remotePromotionDataSource;

    private PromotionRepository promotionRepository;

    private static User user = new User();
    private static List<Promotion> promotionList = new ArrayList<>();
    @Mock
    private PromotionDataSource.GetPromotionsCallback getPromotionsCallback;

    @Captor
    private ArgumentCaptor<PromotionDataSource.GetPromotionsCallback> getPromotionsCallbackArgumentCaptor;

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
        promotionRepository.getPromotionList(user, getPromotionsCallback);
        verify(remotePromotionDataSource).getPromotionList(eq(user)
                , getPromotionsCallbackArgumentCaptor.capture());
        getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(anyList());
        verify(localPromotionDataSource).savePromotionList(promotionList);

    }

    @Test
    public void getPromotionListWithRemoteFailure_getFromLocal() {
        promotionRepository.getPromotionList(user, getPromotionsCallback);
        verify(remotePromotionDataSource).getPromotionList(eq(user)
                , getPromotionsCallbackArgumentCaptor.capture());
        getPromotionsCallbackArgumentCaptor.getValue().onPromotionsNotAvailable();
        verify(localPromotionDataSource, never()).savePromotionList(eq(promotionList));
        verify(localPromotionDataSource).getPromotionList(eq(user), getPromotionsCallbackArgumentCaptor.capture());
        getPromotionsCallbackArgumentCaptor.getValue().onPromotionsLoaded(promotionList);
        verify(getPromotionsCallback).onPromotionsLoaded(promotionList);
    }
}
