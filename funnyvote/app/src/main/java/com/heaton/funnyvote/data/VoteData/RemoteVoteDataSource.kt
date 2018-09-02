package com.heaton.funnyvote.data.VoteData

import android.util.Log
import com.heaton.funnyvote.data.RemoteServiceApi
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import com.heaton.funnyvote.retrofit.Server
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.util.*

class RemoteVoteDataSource : VoteDataSource {
    private val voteService: Server.VoteService = RemoteServiceApi.getInstance().voteService

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        if (user.userCode.isNullOrBlank()) {
            callback!!.onVoteDataNotAvailable()
            return
        }
        val call = voteService.getVote(voteCode, user.userCode, user.tokenType)
        call.enqueue(GetVoteResponseCallback(callback!!))
    }

    override fun saveVoteData(voteData: VoteData) {
        // only for local save.
    }

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        // only for local save.
    }

    override fun saveOptions(optionList: List<Option>) {
        // only for local save
    }

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {

    }

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: VoteDataSource.AddNewOptionCallback) {
        if (user.userCode.isNullOrBlank()) {
            callback.onFailure()
            return
        }
        val call = voteService.updateOption(voteCode, password, newOptions, user.userCode, user.tokenType)
        call.enqueue(AddNewOptionResponseCallback(callback))
    }

    override fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: VoteDataSource.PollVoteCallback?) {
        if (user.userCode.isNullOrBlank()) {
            callback!!.onFailure()
            return
        }
        val call = voteService.pollVote(voteCode, password, pollOptions, user.userCode, user.tokenType)
        call.enqueue(PollVoteResponseCallback(callback!!))
    }

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {
        Log.d("favoriteVoteREMOTE", "favoriteVote favoriteVote")
        if (user.userCode.isNullOrBlank()) {
            callback.onFailure()
            return
        }
        val call = voteService.updateFavorite(voteCode, if (isFavorite) "01" else "00", user.userCode, user.tokenType)
        call.enqueue(FavoriteVoteResponseCallback(isFavorite, callback))
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {
        val parameter = HashMap<String, RequestBody>()

        val title = RequestBody.create(MediaType.parse("text/plain"), voteSetting.title)
        val maxOption = RequestBody.create(MediaType.parse("text/plain"), voteSetting.maxOption.toString())
        val minOption = RequestBody.create(MediaType.parse("text/plain"), voteSetting.minOption.toString())
        val userCanAddOption = RequestBody.create(MediaType.parse("text/plain"), voteSetting.isUserCanAddOption.toString())
        val userPanPreviewResult = RequestBody.create(MediaType.parse("text/plain"), voteSetting.isCanPreviewResult.toString())
        val security = RequestBody.create(MediaType.parse("text/plain"), voteSetting.security)
        val category = RequestBody.create(MediaType.parse("text/plain"), voteSetting.category)
        val startTime = RequestBody.create(MediaType.parse("text/plain"), voteSetting.startTime.toString())
        val endTime = RequestBody.create(MediaType.parse("text/plain"), voteSetting.endTime.toString())

        val token = RequestBody.create(MediaType.parse("text/plain"), voteSetting.authorCode.toString())
        val tokenType = RequestBody.create(MediaType.parse("text/plain"), voteSetting.author.tokenType)
        var rbOption: RequestBody
        for (i in options.indices) {
            rbOption = RequestBody.create(MediaType.parse("text/plain"), options[i])
            parameter["pt[$i]"] = rbOption
        }

        parameter["t"] = title
        parameter["max"] = maxOption
        parameter["min"] = minOption
        parameter["add"] = userCanAddOption
        parameter["res"] = userPanPreviewResult
        parameter["sec"] = security
        parameter["cat"] = category
        parameter["on"] = startTime
        parameter["off"] = endTime
        parameter["token"] = token
        parameter["tokentype"] = tokenType

        if (voteSetting.isNeedPassword) {
            val password = RequestBody.create(MediaType.parse("text/plain"), voteSetting.password)
            parameter["p"] = password
        }
        Log.d(TAG, "Need pw:" + voteSetting.isNeedPassword + " pw:" + voteSetting.password
                + "start time:" + voteSetting.startTime + " ,end:" + voteSetting.endTime)

        var requestFile: RequestBody?
        var body: MultipartBody.Part? = null
        val descriptionString = "vote_image"
        var description: RequestBody? = null

        if (image != null) {
            requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), image)
            body = MultipartBody.Part.createFormData("i", image.name, requestFile!!)
            description = RequestBody.create(
                    MediaType.parse("multipart/form-data"), descriptionString)
        }

        val call: Call<VoteData> = voteService.createVote(parameter, description!!, body!!)
        call.enqueue(CreateVoteResponseCallback(callback))
    }

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        if (user.userCode.isNullOrBlank()) {
            callback.onVoteListNotAvailable()
            return
        }
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        val call = voteService.getVoteList(pageNumber, pageCount, "hot", user.userCode, user.tokenType)
        call.enqueue(GetVoteListResponseCallback(callback))
    }

    override fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        val call: Call<List<VoteData>>
        call = if (targetUser.userCode.isNullOrEmpty()) {
            voteService.getUserCreateVoteList(pageNumber, pageCount, loginUser.userCode, loginUser.tokenType)
        } else {
            voteService.getPersonalCreateVoteList(pageNumber, pageCount, loginUser.userCode, loginUser.tokenType, targetUser.userCode, targetUser.personalTokenType)
        }
        call.enqueue(GetVoteListResponseCallback(callback))
    }

    override fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        Log.d(TAG, "getParticipateVoteList:" + loginUser.userCode
                + "," + loginUser.userName + " , " + loginUser.tokenType + "," + targetUser)
        val call = voteService.getUserParticipateVoteList(pageNumber, pageCount, loginUser.userCode, loginUser.tokenType)

        call.enqueue(GetVoteListResponseCallback(callback))
    }

    override fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        val call: Call<List<VoteData>>
        call = if (targetUser.userCode.isNullOrEmpty()) {
            voteService.getFavoriteVoteList(pageNumber, pageCount, loginUser.userCode, loginUser.tokenType)
        } else {
            voteService.getPersonalFavoriteVoteList(pageNumber, pageCount, loginUser.userCode, loginUser.tokenType, targetUser.userCode, targetUser.personalTokenType)
        }
        call.enqueue(GetVoteListResponseCallback(callback))
    }

    override fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        if (user.userCode.isNullOrBlank()) {
            callback.onVoteListNotAvailable()
            return
        }
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        val call = voteService.getSearchVoteList(keyword, pageNumber, pageCount, user.userCode, user.tokenType)
        call.enqueue(GetVoteListResponseCallback(callback))
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        if (user.userCode.isNullOrBlank()) {
            callback.onVoteListNotAvailable()
            return
        }
        val pageNumber = offset / VoteDataRepository.PAGE_COUNT + 1
        val pageCount = VoteDataRepository.PAGE_COUNT
        val call = voteService.getVoteList(pageNumber, pageCount, "new", user.userCode, user.tokenType)
        call.enqueue(GetVoteListResponseCallback(callback))
    }

    inner class PollVoteResponseCallback(internal var callback: VoteDataSource.PollVoteCallback) : Callback<VoteData> {

        override fun onResponse(call: Call<VoteData>, response: Response<VoteData>) {
            if (response.isSuccessful) {
                callback.onSuccess(response.body())
            } else {
                try {
                    var errorMessage = response.errorBody().string()
                    if (errorMessage == "error_invalid_password") {
                        callback.onPasswordInvalid()
                        return
                    }
                    Log.e(TAG, "pollVoteResponseCallback onResponse false , error message:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onFailure()
            }
        }

        override fun onFailure(call: Call<VoteData>, t: Throwable) {
            Log.e(TAG, "pollVoteResponseCallback onFailure , error message:" + t.message)
            callback.onFailure()
        }
    }

    inner class AddNewOptionResponseCallback(internal var callback: VoteDataSource.AddNewOptionCallback) : Callback<VoteData> {

        override fun onResponse(call: Call<VoteData>, response: Response<VoteData>) {
            if (response.isSuccessful) {
                callback.onSuccess(response.body())
            } else {
                try {
                    var errorMessage = response.errorBody().string()
                    if (errorMessage == "error_invalid_password") {
                        callback.onPasswordInvalid()
                        return
                    }
                    Log.e(TAG, "AddNewOptionResponseCallback onResponse false , error message:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onFailure()
            }
        }

        override fun onFailure(call: Call<VoteData>, t: Throwable) {
            Log.e(TAG, "AddNewOptionResponseCallback onFailure , error message:" + t.message)
            callback.onFailure()
        }
    }

    inner class GetVoteResponseCallback(internal var callback: VoteDataSource.GetVoteDataCallback) : Callback<VoteData> {

        override fun onResponse(call: Call<VoteData>, response: Response<VoteData>) {
            if (response.isSuccessful) {
                callback.onVoteDataLoaded(response.body())
            } else {
                try {
                    var errorMessage = response.errorBody().string()
                    Log.e(TAG, "GetVoteResponseCallback onResponse false, error message:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onVoteDataNotAvailable()
            }
        }

        override fun onFailure(call: Call<VoteData>, t: Throwable) {
            Log.e(TAG, "GetVoteResponseCallback onResponse onFailure, error message:" + t.message)
            callback.onVoteDataNotAvailable()
        }
    }

    inner class FavoriteVoteResponseCallback(private val isFavorite: Boolean, private val callback: VoteDataSource.FavoriteVoteCallback) : Callback<ResponseBody> {

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (response.isSuccessful) {
                callback.onSuccess(isFavorite)
            } else {
                try {
                    var errorMessage = response.errorBody().string()
                    Log.e(TAG, "FavoriteVoteResponseCallback onResponse false, error message:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onFailure()
            }

        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Log.e(TAG, "FavoriteVoteResponseCallback onFailure , error message:" + t.message)
            callback.onFailure()
        }
    }

    inner class CreateVoteResponseCallback(internal var callback: VoteDataSource.GetVoteDataCallback) : Callback<VoteData> {

        override fun onResponse(call: Call<VoteData>, response: Response<VoteData>) {
            if (response.isSuccessful) {
                callback.onVoteDataLoaded(response.body())
            } else {
                try {
                    var errorMessage = response.errorBody().string()
                    Log.e(TAG, "CreateVoteResponseCallback onResponse false, error message:$errorMessage")
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                callback.onVoteDataNotAvailable()
            }
        }

        override fun onFailure(call: Call<VoteData>, t: Throwable) {
            Log.e(TAG, "CreateVoteResponseCallback onResponse onFailure, error message:" + t.message)
            callback.onVoteDataNotAvailable()
        }
    }

    inner class GetVoteListResponseCallback(internal var callback: VoteDataSource.GetVoteListCallback) : Callback<List<VoteData>> {
        override fun onResponse(call: Call<List<VoteData>>, response: Response<List<VoteData>>) =
                if (response.isSuccessful) {
                    callback.onVoteListLoaded(response.body())
                } else {
                    var errorMessage = ""
                    try {
                        errorMessage = response.errorBody().string()
                        Log.e(TAG, "GetVoteListResponseCallback onResponse false, message:$errorMessage")

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (errorMessage == "error_no_poll_event") {
                        callback.onVoteListLoaded(ArrayList())
                    } else {
                        callback.onVoteListNotAvailable()
                    }
                }

        override fun onFailure(call: Call<List<VoteData>>, t: Throwable) {
            Log.e(TAG, "GetVoteListResponseCallback onFailure:" + t.message)
            callback.onVoteListNotAvailable()
        }
    }

    companion object {
        private val TAG = RemoteVoteDataSource::class.java.simpleName
        private var INSTANCE: RemoteVoteDataSource? = null


        @JvmStatic
        fun getInstance(): RemoteVoteDataSource {
            return INSTANCE ?: RemoteVoteDataSource()
                    .apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
