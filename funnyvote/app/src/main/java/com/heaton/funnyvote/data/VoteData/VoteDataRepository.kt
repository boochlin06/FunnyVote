package com.heaton.funnyvote.data.VoteData

import android.util.Log

import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.ui.main.MainPageTabFragment

import java.io.File

class VoteDataRepository(
        private val voteDataLocalSource: VoteDataSource,
        private val voteDataRemoteSource: VoteDataSource
) : VoteDataSource {

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        voteDataRemoteSource.getVoteData(voteCode, user, object : VoteDataSource.GetVoteDataCallback {
            override fun onVoteDataLoaded(voteData: VoteData) {
                voteDataLocalSource.saveVoteData(voteData)
                callback!!.onVoteDataLoaded(voteData)
            }

            override fun onVoteDataNotAvailable() {
                voteDataLocalSource.getVoteData(voteCode, user, object : VoteDataSource.GetVoteDataCallback {
                    override fun onVoteDataLoaded(voteData: VoteData) {
                        callback!!.onVoteDataLoaded(voteData)
                    }

                    override fun onVoteDataNotAvailable() {
                        callback!!.onVoteDataNotAvailable()
                    }
                })

            }
        })
    }

    override fun saveVoteData(voteData: VoteData) {
        voteDataLocalSource.saveVoteData(voteData)
    }

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        voteDataLocalSource.getOptions(voteData, callback)
    }

    override fun saveOptions(optionList: List<Option>) {
        voteDataLocalSource.saveOptions(optionList)
    }

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {
        voteDataLocalSource.saveVoteDataList(voteDataList, offset, tab)
    }

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: VoteDataSource.AddNewOptionCallback) {
        voteDataRemoteSource.addNewOption(voteCode, password, newOptions, user, object : VoteDataSource.AddNewOptionCallback {
            override fun onSuccess(voteData: VoteData) {
                voteDataLocalSource.saveVoteData(voteData)
                callback.onSuccess(voteData)
            }

            override fun onFailure() {
                callback.onFailure()
            }

            override fun onPasswordInvalid() {
                callback.onPasswordInvalid()
            }
        })

    }

    override fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: VoteDataSource.PollVoteCallback?) {
        voteDataRemoteSource.pollVote(voteCode, password, pollOptions, user, object : VoteDataSource.PollVoteCallback {
            override fun onSuccess(voteData: VoteData) {
                voteDataLocalSource.saveVoteData(voteData)
                callback!!.onSuccess(voteData)
            }

            override fun onFailure() {
                callback!!.onFailure()
            }

            override fun onPasswordInvalid() {
                callback!!.onPasswordInvalid()
            }
        })
    }

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {

        Log.d("favoriteVoteRE", "favoriteVote favoriteVote")
        voteDataRemoteSource.favoriteVote(voteCode, isFavorite, user, object : VoteDataSource.FavoriteVoteCallback {
            override fun onSuccess(isFavorite: Boolean) {
                voteDataLocalSource.favoriteVote(voteCode, isFavorite, user, object : VoteDataSource.FavoriteVoteCallback {
                    override fun onSuccess(isFavorite: Boolean) {
                        callback.onSuccess(isFavorite)
                    }

                    override fun onFailure() {
                        callback.onFailure()
                    }
                })
            }

            override fun onFailure() {
                callback.onFailure()
            }
        })
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {
        voteDataRemoteSource.createVote(voteSetting, options, image, object : VoteDataSource.GetVoteDataCallback {
            override fun onVoteDataLoaded(voteData: VoteData) {
                voteDataLocalSource.saveVoteData(voteData)
                callback.onVoteDataLoaded(voteData)
            }

            override fun onVoteDataNotAvailable() {
                callback.onVoteDataNotAvailable()
            }
        })
    }

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        voteDataRemoteSource.getHotVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_HOT)
                callback.onVoteListLoaded(voteDataList)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getHotVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })

    }

    override fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        voteDataRemoteSource.getCreateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_CREATE)
                callback.onVoteListLoaded(voteDataList)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getCreateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })
    }

    override fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        if (!targetUser.userCode.isNullOrEmpty()) {
            //Log.d("test", "target:${targetUser.userCode} loginuser:${loginUser.userCode}")
            callback.onVoteListNotAvailable()
            return
        }
        voteDataRemoteSource.getParticipateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_PARTICIPATE)
                callback.onVoteListLoaded(voteDataList)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getParticipateVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })
    }

    override fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        voteDataRemoteSource.getFavoriteVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_FAVORITE)
                callback.onVoteListLoaded(voteDataList)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getFavoriteVoteList(offset, loginUser, targetUser, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })
    }

    override fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        voteDataRemoteSource.getSearchVoteList(keyword, offset, user, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                callback.onVoteListLoaded(voteDataList)
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.KEY_TAB)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getSearchVoteList(keyword, offset, user, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        voteDataRemoteSource.getNewVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
            override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                voteDataLocalSource.saveVoteDataList(voteDataList, offset, MainPageTabFragment.TAB_NEW)
                callback.onVoteListLoaded(voteDataList)
            }

            override fun onVoteListNotAvailable() {
                voteDataLocalSource.getNewVoteList(offset, user, object : VoteDataSource.GetVoteListCallback {
                    override fun onVoteListLoaded(voteDataList: List<VoteData>) {
                        callback.onVoteListLoaded(voteDataList)
                    }

                    override fun onVoteListNotAvailable() {
                        callback.onVoteListNotAvailable()
                    }
                })
            }
        })
    }

    companion object {
        private var INSTANCE: VoteDataRepository? = null
        const val PAGE_COUNT = 20

        @JvmStatic
        fun getInstance(voteDataLocalSource: VoteDataSource
                        , voteDataRemoteSource: VoteDataSource): VoteDataRepository {
            return INSTANCE ?: VoteDataRepository(voteDataLocalSource, voteDataRemoteSource)
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }

}
