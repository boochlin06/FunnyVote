package com.heaton.funnyvote.data.VoteData

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.heaton.funnyvote.data.local.dao.OptionDao
import com.heaton.funnyvote.data.local.dao.VoteDataDao
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.main.MainPageTabFragment
import com.heaton.funnyvote.utils.AppExecutors
import java.io.File

class LocalVoteDataSource private constructor(
    private val voteDataDao: VoteDataDao,
    private val optionDao: OptionDao,
    private val mAppExecutors: AppExecutors
) : VoteDataSource {

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        mAppExecutors.diskIO.execute {
            if (TextUtils.isEmpty(voteCode)) {
                mAppExecutors.mainThread.execute { callback?.onVoteDataNotAvailable() }
                return@execute
            }
            val voteData = voteDataDao.getVoteByCode(voteCode)
            if (voteData != null) {
                val options = optionDao.getOptionsByVoteCode(voteCode)
                voteData.options = options
                mAppExecutors.mainThread.execute {
                    callback?.onVoteDataLoaded(voteData)
                }
            } else {
                mAppExecutors.mainThread.execute {
                    callback?.onVoteDataNotAvailable()
                }
            }
        }
    }

    override fun saveVoteData(voteData: VoteData) {
        val optionList = voteData.netOptions ?: emptyList()
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
                voteData.option1Count = option.count
                voteData.option1Polled = option.isUserChoiced
            } else if (i == 1) {
                voteData.option2Title = option.title
                voteData.option2Code = option.code
                voteData.option2Count = option.count
                voteData.option2Polled = option.isUserChoiced
            }
            if (option.count > maxOption && option.count >= 1) {
                maxOption = option.count
                voteData.optionTopCount = option.count
                voteData.optionTopCode = option.code
                voteData.optionTopTitle = option.title
                voteData.optionTopPolled = option.isUserChoiced
            }
            if (option.isUserChoiced) {
                voteData.optionUserChoiceCode = option.code
                voteData.optionUserChoiceTitle = option.title
                voteData.optionUserChoiceCount = option.count
            }
        }
        mAppExecutors.diskIO.execute {
            voteDataDao.deleteByCode(voteData.voteCode)
            voteDataDao.insertOrReplace(voteData)
            optionDao.deleteByVoteCode(voteData.voteCode)
            optionDao.insertOrReplaceInTx(optionList)
        }
    }

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        mAppExecutors.diskIO.execute {
            val optionList = optionDao.getOptionsByVoteCode(voteData.voteCode)
            mAppExecutors.mainThread.execute {
                if (optionList.size >= 2) {
                    callback.onVoteOptionsLoaded(optionList)
                } else {
                    callback.onVoteOptionsNotAvailable()
                }
            }
        }
    }

    override fun saveOptions(optionList: List<Option>) {
        mAppExecutors.diskIO.execute {
            optionDao.insertOrReplaceInTx(optionList)
        }
    }

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {
        for (i in voteDataList.indices) {
            val voteData = voteDataList[i]
            if (voteData.firstOption != null) {
                voteData.option1Code = voteData.firstOption.code
                voteData.option1Title = voteData.firstOption.title
                voteData.option1Count = voteData.firstOption.count
                voteData.option1Polled = voteData.firstOption.isUserChoiced
            }
            if (voteData.secondOption != null) {
                voteData.option2Code = voteData.secondOption.code
                voteData.option2Title = voteData.secondOption.title
                voteData.option2Count = voteData.secondOption.count
                voteData.option2Polled = voteData.secondOption.isUserChoiced
            }
            if (voteData.topOption != null) {
                voteData.optionTopCode = voteData.topOption.code
                voteData.optionTopTitle = voteData.topOption.title
                voteData.optionTopCount = voteData.topOption.count
                voteData.optionTopPolled = voteData.topOption.isUserChoiced
            }
            if (voteData.userOption != null) {
                voteData.optionUserChoiceCode = voteData.userOption.code
                voteData.optionUserChoiceTitle = voteData.userOption.title
                voteData.optionUserChoiceCount = voteData.userOption.count
            }
            if (tab == MainPageTabFragment.TAB_HOT) {
                voteData.displayOrder = offset * VoteDataRepository.PAGE_COUNT + i
                voteData.category = "hot"
            } else {
                voteData.category = null
            }
        }
        mAppExecutors.diskIO.execute {
            for (data in voteDataList) {
                voteDataDao.deleteByCode(data.voteCode)
            }
            voteDataDao.insertOrReplaceInTx(voteDataList)
        }
    }

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: VoteDataSource.AddNewOptionCallback) {}

    override fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: VoteDataSource.PollVoteCallback?) {}

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {
        mAppExecutors.diskIO.execute {
            val voteData = voteDataDao.getVoteByCode(voteCode)
            if (voteData != null) {
                voteData.isFavorite = isFavorite
                voteDataDao.update(voteData)
                mAppExecutors.mainThread.execute {
                    callback.onSuccess(isFavorite)
                }
            } else {
                mAppExecutors.mainThread.execute {
                    callback.onFailure()
                }
            }
        }
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {}

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        mAppExecutors.diskIO.execute {
            val list = voteDataDao.getHotVotes("hot", VoteDataRepository.PAGE_COUNT, offset)
            mAppExecutors.mainThread.execute {
                callback.onVoteListLoaded(list)
            }
        }
    }

    override fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty() && !loginUser.userCode.isNullOrEmpty()) {
            mAppExecutors.diskIO.execute {
                val list = voteDataDao.getCreateVotes(loginUser.userCode, VoteDataRepository.PAGE_COUNT, offset)
                mAppExecutors.mainThread.execute {
                    callback.onVoteListLoaded(list)
                }
            }
        } else {
            callback.onVoteListNotAvailable()
        }
    }

    override fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty()) {
            mAppExecutors.diskIO.execute {
                val list = voteDataDao.getParticipateVotes(VoteDataRepository.PAGE_COUNT, offset)
                mAppExecutors.mainThread.execute {
                    callback.onVoteListLoaded(list)
                }
            }
        } else {
            callback.onVoteListNotAvailable()
        }
    }

    override fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (targetUser.userCode.isNullOrEmpty()) {
            mAppExecutors.diskIO.execute {
                val list = voteDataDao.getFavoriteVotes(VoteDataRepository.PAGE_COUNT, offset)
                mAppExecutors.mainThread.execute {
                    callback.onVoteListLoaded(list)
                }
            }
        } else {
            callback.onVoteListNotAvailable()
        }
    }

    override fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        if (TextUtils.isEmpty(keyword)) {
            callback.onVoteListNotAvailable()
            return
        }
        mAppExecutors.diskIO.execute {
            val list = voteDataDao.searchVotes(keyword, VoteDataRepository.PAGE_COUNT, offset)
            mAppExecutors.mainThread.execute {
                callback.onVoteListLoaded(list)
            }
        }
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        mAppExecutors.diskIO.execute {
            val list = voteDataDao.getNewVotes(VoteDataRepository.PAGE_COUNT, offset)
            mAppExecutors.mainThread.execute {
                callback.onVoteListLoaded(list)
            }
        }
    }

    companion object {
        private var INSTANCE: LocalVoteDataSource? = null

        @JvmStatic
        fun getInstance(
            voteDataDao: VoteDataDao,
            optionDao: OptionDao,
            appExecutors: AppExecutors
        ): LocalVoteDataSource {
            if (INSTANCE == null) {
                synchronized(LocalVoteDataSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = LocalVoteDataSource(voteDataDao, optionDao, appExecutors)
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
