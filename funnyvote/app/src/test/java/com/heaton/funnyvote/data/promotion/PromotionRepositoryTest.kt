package com.heaton.funnyvote.data.promotion

import com.heaton.funnyvote.capture
import com.heaton.funnyvote.database.Promotion
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.eq
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import java.util.*

class PromotionRepositoryTest {

    @Mock
    private lateinit var localPromotionDataSource: PromotionDataSource
    @Mock
    private lateinit var remotePromotionDataSource: PromotionDataSource

    private lateinit var promotionRepository: PromotionRepository
    @Mock
    private lateinit var getPromotionsCallback: PromotionDataSource.GetPromotionsCallback

    @Captor
    private lateinit var getPromotionsCallbackArgumentCaptor: ArgumentCaptor<PromotionDataSource.GetPromotionsCallback>

    @Before
    fun setUpPromotionRepository() {
        MockitoAnnotations.initMocks(this)
        promotionRepository = PromotionRepository.getInstance(remotePromotionDataSource, localPromotionDataSource)
    }

    @After
    fun destroyRepositoryInstance() {
        PromotionRepository.destroyInstance()
    }

    @Test
    fun getPromotionList_getFromRemoteAndSaveToLocal() {
        promotionRepository.getPromotionList(user, getPromotionsCallback)
        verify<PromotionDataSource>(remotePromotionDataSource).getPromotionList(eq(user), capture(getPromotionsCallbackArgumentCaptor))
        getPromotionsCallbackArgumentCaptor.value.onPromotionsLoaded(ArgumentMatchers.anyList())
        verify<PromotionDataSource>(localPromotionDataSource).savePromotionList(promotionList)

    }

    @Test
    fun getPromotionListWithRemoteFailure_getFromLocal() {
        promotionRepository.getPromotionList(user, getPromotionsCallback)
        verify<PromotionDataSource>(remotePromotionDataSource).getPromotionList(eq(user), capture(getPromotionsCallbackArgumentCaptor))
        getPromotionsCallbackArgumentCaptor.value.onPromotionsNotAvailable()
        verify<PromotionDataSource>(localPromotionDataSource, never())
                .savePromotionList(eq(promotionList))
        verify<PromotionDataSource>(localPromotionDataSource).getPromotionList(eq(user), capture(getPromotionsCallbackArgumentCaptor))
        getPromotionsCallbackArgumentCaptor.value.onPromotionsLoaded(promotionList)
        verify<PromotionDataSource.GetPromotionsCallback>(getPromotionsCallback).onPromotionsLoaded(promotionList)
    }

    companion object {

        private val user = User()
        private val promotionList = ArrayList<Promotion>()
    }
}
