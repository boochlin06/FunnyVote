package com.heaton.funnyvote.data.VoteData

import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData

import java.io.File

interface VoteDataSource {
    interface GetVoteDataCallback {
        fun onVoteDataLoaded(voteData: VoteData)

        fun onVoteDataNotAvailable()
    }

    interface GetVoteOptionsCallback {
        fun onVoteOptionsLoaded(optionList: List<Option>)

        fun onVoteOptionsNotAvailable()
    }

    interface PollVoteCallback {
        fun onSuccess(voteData: VoteData)

        fun onFailure()

        fun onPasswordInvalid()
    }

    interface FavoriteVoteCallback {
        fun onSuccess(isFavorite: Boolean)

        fun onFailure()
    }

    interface AddNewOptionCallback {
        fun onSuccess(voteData: VoteData)

        fun onFailure()

        fun onPasswordInvalid()
    }

    interface GetVoteListCallback {
        fun onVoteListLoaded(voteDataList: List<VoteData>)

        fun onVoteListNotAvailable()
    }


    fun getVoteData(voteCode: String, user: User, callback: GetVoteDataCallback?)

    fun saveVoteData(voteData: VoteData)

    fun getOptions(voteData: VoteData, callback: GetVoteOptionsCallback)

    fun saveOptions(optionList: List<Option>)

    fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String)

    fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: AddNewOptionCallback)

    fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: PollVoteCallback?)

    fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: FavoriteVoteCallback)

    fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: GetVoteDataCallback)

    fun getHotVoteList(offset: Int, user: User, callback: GetVoteListCallback)

    fun getNewVoteList(offset: Int, user: User, callback: GetVoteListCallback)

    fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: GetVoteListCallback)

    fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: GetVoteListCallback)

    fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: GetVoteListCallback)

    fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: GetVoteListCallback)
}
