package com.heaton.funnyvote.data.VoteData

import android.support.annotation.NonNull
import android.util.Log
import com.google.common.collect.Lists
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.io.File
import kotlin.collections.LinkedHashMap

class FakeRemoteVoteDataRepository// Prevent direct instantiation.
private constructor() : VoteDataSource {
    override fun pollVote(voteCode: String
                          , password: String, pollOptions: List<String>
                          , user: User, callback: VoteDataSource.PollVoteCallback?) {
        val voteData = VOTES_SERVICE_DATA[voteCode]
        callback!!.onSuccess(voteData!!)
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {
        voteSetting.voteCode = "CODE_0"
        callback.onVoteDataLoaded(voteSetting)
    }

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        Log.d(TAG, "getVoteData")
        val voteData = VOTES_SERVICE_DATA.get(voteCode)
        callback!!.onVoteDataLoaded(voteData!!)
    }

    override fun saveVoteData(voteData: VoteData) {
        VOTES_SERVICE_DATA[voteData.voteCode] = voteData
    }

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        callback.onVoteOptionsLoaded(voteData.netOptions)
    }

    override fun saveOptions(optionList: List<Option>) {
        //NONE
    }

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {
        //NONE
    }

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>
                              , user: User, callback: VoteDataSource.AddNewOptionCallback) {
        val voteData = VOTES_SERVICE_DATA[voteCode]
        callback.onSuccess(voteData!!)
    }

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {
        val voteData = VOTES_SERVICE_DATA[voteCode]
        callback.onSuccess(voteData!!.isFavorite)
    }

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    override fun getCreateVoteList(offset: Int, user: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    override fun getParticipateVoteList(offset: Int, user: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    override fun getFavoriteVoteList(offset: Int, user: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    override fun getSearchVoteList(keyword: String, offset: Int, @NonNull user: User, callback: VoteDataSource.GetVoteListCallback) {
        callback.onVoteListLoaded(Lists.newArrayList(VOTES_SERVICE_DATA.values))
    }

    companion object {

        private val TAG = FakeRemoteVoteDataRepository::class.java.simpleName
        private var INSTANCE: FakeRemoteVoteDataRepository? = null


        private val VOTES_SERVICE_DATA = LinkedHashMap<String,VoteData>()

        @JvmStatic
        fun getInstance(): FakeRemoteVoteDataRepository {
            return INSTANCE ?: FakeRemoteVoteDataRepository()
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
