package com.heaton.funnyvote.data.VoteData

import android.support.annotation.VisibleForTesting
import android.text.TextUtils
import android.util.Log
import com.heaton.funnyvote.database.*
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.utils.AppExecutors
import org.greenrobot.greendao.query.WhereCondition
import java.io.File
import java.util.*

class LocalVoteDataSource
private constructor(
        private val voteDataDao: VoteDataDao,
        private val optionDao: OptionDao,
        private val mAppExecutors: AppExecutors
) : VoteDataSource {

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        val runnable = Runnable {
            if (TextUtils.isEmpty(voteCode)) {
                callback!!.onVoteDataNotAvailable()
            }
            mAppExecutors.mainThread.execute {
                val list = voteDataDao.queryBuilder()
                        .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).list()
                if (list.size > 0) {
                    callback!!.onVoteDataLoaded(list[0])
                } else {
                    callback!!.onVoteDataNotAvailable()
                }
            }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun saveVoteData(voteData: VoteData) {
        val optionList = voteData.netOptions
        voteData.optionCount = optionList.size
        var maxOption = 0
        for (i in optionList.indices) {
            val option = optionList[i]
            option.voteCode = voteData.voteCode
            if (option.count == null) {
                option.count = 0
            }
            option.id = null
            if (i == 0) {
                voteData.option1Title = option.title
                voteData.option1Code = option.code
                voteData.option1Count = option.count!!
                voteData.option1Polled = option.isUserChoiced
            } else if (i == 1) {
                voteData.option2Title = option.title
                voteData.option2Code = option.code
                voteData.option2Count = option.count!!
                voteData.option2Polled = option.isUserChoiced
            }
            if (option.count > maxOption && option.count >= 1) {
                maxOption = option.count!!
                voteData.optionTopCount = option.count!!
                voteData.optionTopCode = option.code
                voteData.optionTopTitle = option.title
                voteData.optionTopPolled = option.isUserChoiced
            }
            if (option.isUserChoiced) {
                voteData.optionUserChoiceCode = option.code
                voteData.optionUserChoiceTitle = option.title
                voteData.optionUserChoiceCount = option.count!!
            }

            option.dumpDetail()
        }
        mAppExecutors.diskIO.execute(SaveDBRunnable(voteData))
    }

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        val runnable = Runnable {
            val optionList = optionDao.queryBuilder()
                    .where(OptionDao.Properties.VoteCode.eq(voteData.voteCode)).list()
            mAppExecutors.mainThread.execute {
                if (optionList.size >= 2) {
                    callback.onVoteOptionsLoaded(optionList)
                } else {
                    callback.onVoteOptionsNotAvailable()
                }
            }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: VoteDataSource.AddNewOptionCallback) {
        // Nothing to do
    }

    override fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: VoteDataSource.PollVoteCallback?) {
        // Nothing to do
    }

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {
        val runnable = Runnable {
            val list = voteDataDao.queryBuilder()
                    .where(VoteDataDao.Properties.VoteCode.eq(voteCode)).list()
            if (list.size > 0) {
                val data = VoteData()
                data.isFavorite = isFavorite
                data.voteCode = voteCode
                val voteData = list[0]
                data.id = voteData.id
                voteDataDao.update(data)
                mAppExecutors.mainThread.execute { callback.onSuccess(isFavorite) }
            }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {
        // Nothing to do.
    }

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        val runnable = Runnable {
            val list = voteDataDao.queryBuilder().where(VoteDataDao.Properties.Category.eq("hot"), VoteDataDao.Properties.StartTime.le(System.currentTimeMillis())).offset(offset)
                    .orderAsc(VoteDataDao.Properties.DisplayOrder)
                    .limit(VoteDataRepository.PAGE_COUNT).list()

            mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty()) {
            val runnable = Runnable {
                val list = voteDataDao.queryBuilder()
                        .where(VoteDataDao.Properties.AuthorCode.eq(loginUser.userCode))
                        .limit(VoteDataRepository.PAGE_COUNT)
                        .offset(offset).orderDesc(VoteDataDao.Properties.StartTime).list()

                mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
            }
            mAppExecutors.diskIO.execute(runnable)
        } else {
            mAppExecutors.mainThread.execute { callback.onVoteListNotAvailable() }
        }
    }

    override fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty()) {
            val runnable = Runnable {
                val list = voteDataDao.queryBuilder().where(VoteDataDao.Properties.IsPolled.eq(true))
                        .limit(VoteDataRepository.PAGE_COUNT)
                        .offset(offset).orderDesc(VoteDataDao.Properties.StartTime).list()

                Log.d(TAG, "PARTICIPATE LOCAL SIZE:" + list.size)
                mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
            }
            mAppExecutors.diskIO.execute(runnable)
        } else {
            mAppExecutors.mainThread.execute { callback.onVoteListNotAvailable() }
        }
    }

    override fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty()) {
            val runnable = Runnable {
                val list = voteDataDao.queryBuilder()
                        .where(VoteDataDao.Properties.IsFavorite.eq(true))
                        .offset(offset).limit(VoteDataRepository.PAGE_COUNT).list()

                mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
            }
            mAppExecutors.diskIO.execute(runnable)
        } else {
            mAppExecutors.mainThread.execute { callback.onVoteListNotAvailable() }
        }
    }

    override fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        if (keyword.isNullOrEmpty()) {
            callback.onVoteListNotAvailable()
            return
        }
        val runnable = Runnable {
            val list = voteDataDao.queryBuilder()
                    .whereOr(VoteDataDao.Properties.Title.like(keyword), VoteDataDao.Properties.AuthorName.like(keyword))
                    .orderDesc(VoteDataDao.Properties.StartTime)
                    .offset(offset).limit(VoteDataRepository.PAGE_COUNT).list()

            mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        val runnable = Runnable {
            val list = voteDataDao.queryBuilder().where(VoteDataDao.Properties.StartTime.le(System.currentTimeMillis()))
                    .orderDesc(VoteDataDao.Properties.StartTime)
                    .orderDesc().offset(offset).limit(VoteDataRepository.PAGE_COUNT).list()

            mAppExecutors.mainThread.execute { callback.onVoteListLoaded(list) }
        }
        mAppExecutors.diskIO.execute(runnable)
    }

    override fun saveOptions(optionList: List<Option>) {
        mAppExecutors.diskIO.execute { optionDao.insertOrReplaceInTx(optionList as Option) }
    }

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {
        for (i in voteDataList.indices) {
            val voteData = voteDataList[i]
            if (voteData.firstOption != null) {
                voteData.option1Code = voteData.firstOption.code
                voteData.option1Title = voteData.firstOption.title
                voteData.option1Count = voteData.firstOption.count!!
                voteData.option1Polled = voteData.firstOption.isUserChoiced
            }
            if (voteData.secondOption != null) {
                voteData.option2Code = voteData.secondOption.code
                voteData.option2Title = voteData.secondOption.title
                voteData.option2Count = voteData.secondOption.count!!
                voteData.option2Polled = voteData.secondOption.isUserChoiced
            }
            if (voteData.topOption != null) {
                voteData.optionTopCode = voteData.topOption.code
                voteData.optionTopTitle = voteData.topOption.title
                voteData.optionTopCount = voteData.topOption.count!!
                voteData.optionTopPolled = voteData.topOption.isUserChoiced
            }
            if (voteData.userOption != null) {
                voteData.optionUserChoiceCode = voteData.userOption.code
                voteData.optionUserChoiceTitle = voteData.userOption.title
                voteData.optionUserChoiceCount = voteData.userOption.count!!
            }
            if (tab == MainPageTabFragment.TAB_HOT) {
                voteData.setDisplayOrder(offset * VoteDataRepository.PAGE_COUNT + i)
                voteData.category = "hot"
            } else {
                voteData.category = null
            }
        }
        mAppExecutors.diskIO.execute(SaveListDBRunnable(voteDataList, offset, tab))
    }

    private inner class SaveDBRunnable(private val voteDataNetwork: VoteData) : Runnable {

        override fun run() {
            voteDataDao.queryBuilder().where(VoteDataDao.Properties.VoteCode.eq(voteDataNetwork.voteCode)).buildDelete()
                    .executeDeleteWithoutDetachingEntities()
            optionDao.queryBuilder().where(OptionDao.Properties.VoteCode.eq(voteDataNetwork.voteCode)).buildDelete()
                    .executeDeleteWithoutDetachingEntities()
            voteDataDao.insertOrReplace(voteDataNetwork)
            for (i in 0 until voteDataNetwork.netOptions.size) {
                voteDataNetwork.netOptions[i].voteCode = voteDataNetwork.voteCode
            }
            optionDao.insertOrReplaceInTx(voteDataNetwork.netOptions)
        }
    }

    private inner class SaveListDBRunnable(private val voteDataList: List<VoteData>, private val offset: Int, private val tab: String?) : Runnable {
        //private val isLoginUser: Boolean

        init {
            //this.isLoginUser = isLoginUser
        }

        override fun run() {
            val whereConditions = ArrayList<WhereCondition>()
            //List<String> favVoteCodeList = new ArrayList<>();

            for (i in voteDataList.indices) {
                val voteData = voteDataList[i]
                if (voteData.firstOption != null) {
                    voteData.option1Code = voteData.firstOption.code
                    voteData.option1Title = voteData.firstOption.title
                    voteData.option1Count = voteData.firstOption.count!!
                    voteData.option1Polled = voteData.firstOption.isUserChoiced
                }
                if (voteData.secondOption != null) {
                    voteData.option2Code = voteData.secondOption.code
                    voteData.option2Title = voteData.secondOption.title
                    voteData.option2Count = voteData.secondOption.count!!
                    voteData.option2Polled = voteData.secondOption.isUserChoiced
                }
                if (voteData.topOption != null) {
                    voteData.optionTopCode = voteData.topOption.code
                    voteData.optionTopTitle = voteData.topOption.title
                    voteData.optionTopCount = voteData.topOption.count!!
                    voteData.optionTopPolled = voteData.topOption.isUserChoiced
                }
                if (voteData.userOption != null) {
                    voteData.optionUserChoiceCode = voteData.userOption.code
                    voteData.optionUserChoiceTitle = voteData.userOption.title
                    voteData.optionUserChoiceCount = voteData.userOption.count!!
                }
                if (tab != null && tab == MainPageTabFragment.TAB_HOT) {
                    voteData.setDisplayOrder(offset * VoteDataRepository.PAGE_COUNT + i)
                    voteData.category = "hot"
                } else {
                    voteData.category = null
                }
                //Log.d(TAG, tab + "," + i + ",save item polled:" + voteData.isPolled)
                //                if (!isLoginUser) {
                //                    //todo: temp reset fav for login user
                //                    voteData.setIsFavorite(favVoteCodeList.contains(voteData.getVoteCode()));
                //                }
                whereConditions.add(VoteDataDao.Properties.VoteCode.eq(voteData.voteCode))
            }
            var conditionsArray = whereConditions.toTypedArray()
            val queryBuilder = voteDataDao.queryBuilder()
            if (conditionsArray.size > 2) {
                queryBuilder.whereOr(conditionsArray[0], conditionsArray[1], *Arrays.copyOfRange(conditionsArray, 2, conditionsArray.size))
            } else if (conditionsArray.size == 2) {
                queryBuilder.whereOr(conditionsArray[0], conditionsArray[1])
            } else if (conditionsArray.size == 1) {
                queryBuilder.where(conditionsArray[0])
            } else if (conditionsArray.isEmpty()) {
                return
            }
            queryBuilder.buildDelete().executeDeleteWithoutDetachingEntities()
            voteDataDao.insertOrReplaceInTx(voteDataList)
        }
    }

    companion object {
        private val TAG = LocalVoteDataSource::class.java.simpleName
        @Volatile
        private var INSTANCE: LocalVoteDataSource? = null

        fun getInstance(voteDataDao: VoteDataDao, optionDao: OptionDao, appExecutors: AppExecutors): LocalVoteDataSource? {
            if (INSTANCE == null) {
                synchronized(LocalVoteDataSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = LocalVoteDataSource(voteDataDao, optionDao, appExecutors)
                    }
                }
            }
            return INSTANCE
        }

        @VisibleForTesting
        internal fun clearInstance() {
            INSTANCE = null
        }
    }
}
