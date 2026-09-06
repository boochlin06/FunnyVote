package com.heaton.funnyvote.data.VoteData

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.heaton.funnyvote.database.Option
import com.heaton.funnyvote.database.User
import com.heaton.funnyvote.database.VoteData
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.UUID

class RemoteVoteDataSource private constructor() : VoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var hasCheckedSeed = false

    private fun getUid(user: User?): String {
        if (user != null && !user.userCode.isNullOrEmpty()) {
            return user.userCode
        }
        val currentUid = auth.currentUser?.uid
        if (!currentUid.isNullOrEmpty()) {
            return currentUid
        }
        return "guest_" + UUID.randomUUID().toString().substring(0, 8)
    }

    private fun initSeedDataIfNeeded(db: FirebaseFirestore) {
        if (hasCheckedSeed) return
        hasCheckedSeed = true
        db.collection("polls").limit(1).get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                Log.d(TAG, "Initializing Firestore Seed Data...")
                val batch = db.batch()
                val now = System.currentTimeMillis()

                val poll1Ref = db.collection("polls").document("seed_poll_1")
                val poll1 = hashMapOf(
                    "pollId" to "seed_poll_1",
                    "title" to "【討論】大家玩遊戲是「白金成就黨」還是「通關劇情黨」？",
                    "authorId" to "seed_author_1",
                    "authorName" to "白金獵人",
                    "authorIcon" to "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150",
                    "imageUrl" to "https://images.unsplash.com/photo-1538481199705-c710c4e965fc?w=800",
                    "category" to "hot",
                    "security" to "00",
                    "isNeedPassword" to false,
                    "isCanPreviewResult" to true,
                    "isUserCanAddOption" to true,
                    "minOption" to 1,
                    "maxOption" to 1,
                    "optionCount" to 3,
                    "totalVotes" to 128,
                    "createdAt" to now,
                    "startTime" to now,
                    "endTime" to now + 86400000L * 30L,
                    "topOptions" to listOf(
                        hashMapOf("code" to "opt_1_1", "title" to "白金成就全收集才算玩過", "count" to 85),
                        hashMapOf("code" to "opt_1_2", "title" to "主線通關就心滿意足看下一款", "count" to 38)
                    )
                )
                batch.set(poll1Ref, poll1)

                val opt1 = db.collection("polls").document("seed_poll_1").collection("options").document("opt_1_1")
                batch.set(opt1, hashMapOf("code" to "opt_1_1", "title" to "白金成就全收集才算玩過", "count" to 85))
                val opt2 = db.collection("polls").document("seed_poll_1").collection("options").document("opt_1_2")
                batch.set(opt2, hashMapOf("code" to "opt_1_2", "title" to "主線通關就心滿意足看下一款", "count" to 38))
                val opt3 = db.collection("polls").document("seed_poll_1").collection("options").document("opt_1_3")
                batch.set(opt3, hashMapOf("code" to "opt_1_3", "title" to "買了當作玩了（收藏庫喜加一）", "count" to 5))

                val poll2Ref = db.collection("polls").document("seed_poll_2")
                val poll2 = hashMapOf(
                    "pollId" to "seed_poll_2",
                    "title" to "上班族的終極難題：今天中午吃什麼？",
                    "authorId" to "seed_author_2",
                    "authorName" to "吃貨小幫手",
                    "authorIcon" to "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
                    "imageUrl" to "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800",
                    "category" to "hot",
                    "security" to "00",
                    "isNeedPassword" to false,
                    "isCanPreviewResult" to true,
                    "isUserCanAddOption" to true,
                    "minOption" to 1,
                    "maxOption" to 1,
                    "optionCount" to 4,
                    "totalVotes" to 95,
                    "createdAt" to now - 3600000L,
                    "startTime" to now - 3600000L,
                    "endTime" to now + 86400000L * 7L,
                    "topOptions" to listOf(
                        hashMapOf("code" to "opt_2_1", "title" to "健康便當 / 輕食水煮", "count" to 42),
                        hashMapOf("code" to "opt_2_2", "title" to "香酥雞排飯配大冰奶", "count" to 36)
                    )
                )
                batch.set(poll2Ref, poll2)

                val opt21 = db.collection("polls").document("seed_poll_2").collection("options").document("opt_2_1")
                batch.set(opt21, hashMapOf("code" to "opt_2_1", "title" to "健康便當 / 輕食水煮", "count" to 42))
                val opt22 = db.collection("polls").document("seed_poll_2").collection("options").document("opt_2_2")
                batch.set(opt22, hashMapOf("code" to "opt_2_2", "title" to "香酥雞排飯配大冰奶", "count" to 36))
                val opt23 = db.collection("polls").document("seed_poll_2").collection("options").document("opt_2_3")
                batch.set(opt23, hashMapOf("code" to "opt_2_3", "title" to "日式拉麵", "count" to 12))
                val opt24 = db.collection("polls").document("seed_poll_2").collection("options").document("opt_2_4")
                batch.set(opt24, hashMapOf("code" to "opt_2_4", "title" to "超商隨便抓", "count" to 5))

                batch.commit().addOnSuccessListener {
                    Log.d(TAG, "Seed data initialized successfully")
                }
            }
        }
    }

    override fun getVoteData(voteCode: String, user: User, callback: VoteDataSource.GetVoteDataCallback?) {
        initSeedDataIfNeeded(firestore)
        firestore.collection("polls").document(voteCode).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && task.result!!.exists()) {
                val voteData = mapDocToVoteData(task.result!!)
                firestore.collection("polls").document(voteCode).collection("options").get().addOnCompleteListener { optTask ->
                    val options = mutableListOf<Option>()
                    if (optTask.isSuccessful && optTask.result != null) {
                        for (optDoc in optTask.result!!) {
                            val opt = Option().apply {
                                code = optDoc.getString("code") ?: optDoc.id
                                title = optDoc.getString("title") ?: ""
                                count = (optDoc.getLong("count") ?: 0L).toInt()
                                this.voteCode = voteCode
                            }
                            options.add(opt)
                        }
                    }
                    val uid = getUid(user)
                    firestore.collection("polls").document(voteCode).collection("voters").document(uid).get().addOnCompleteListener { voterTask ->
                        if (voterTask.isSuccessful && voterTask.result != null && voterTask.result!!.exists()) {
                            val polledCodes = voterTask.result!!.get("polledOptionCodes") as? List<String>
                            if (polledCodes != null) {
                                for (opt in options) {
                                    if (polledCodes.contains(opt.code)) {
                                        opt.isUserChoiced = true
                                        voteData?.optionUserChoiceCode = opt.code
                                        voteData?.optionUserChoiceTitle = opt.title
                                        voteData?.optionUserChoiceCount = opt.count
                                    }
                                }
                            }
                        }
                        firestore.collection("users").document(uid).collection("favorites").document(voteCode).get().addOnCompleteListener { favTask ->
                            if (favTask.isSuccessful && favTask.result != null && favTask.result!!.exists()) {
                                voteData?.isFavorite = true
                            }
                            voteData?.options = options
                            if (voteData != null) {
                                callback?.onVoteDataLoaded(voteData)
                            } else {
                                callback?.onVoteDataNotAvailable()
                            }
                        }
                    }
                }
            } else {
                callback?.onVoteDataNotAvailable()
            }
        }
    }

    override fun saveVoteData(voteData: VoteData) {}

    override fun getOptions(voteData: VoteData, callback: VoteDataSource.GetVoteOptionsCallback) {
        firestore.collection("polls").document(voteData.voteCode).collection("options").get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null && !task.result!!.isEmpty) {
                val options = mutableListOf<Option>()
                for (doc in task.result!!) {
                    val opt = Option().apply {
                        code = doc.getString("code") ?: doc.id
                        title = doc.getString("title") ?: ""
                        count = (doc.getLong("count") ?: 0L).toInt()
                        this.voteCode = voteData.voteCode
                    }
                    options.add(opt)
                }
                callback.onVoteOptionsLoaded(options)
            } else {
                callback.onVoteOptionsNotAvailable()
            }
        }
    }

    override fun saveOptions(optionList: List<Option>) {}

    override fun saveVoteDataList(voteDataList: List<VoteData>, offset: Int, tab: String) {}

    override fun addNewOption(voteCode: String, password: String, newOptions: List<String>, user: User, callback: VoteDataSource.AddNewOptionCallback) {
        val pollRef = firestore.collection("polls").document(voteCode)
        pollRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                callback.onFailure()
                return@addOnSuccessListener
            }
            val needPw = doc.getBoolean("isNeedPassword") ?: false
            if (needPw) {
                val storedHash = doc.getString("passwordHash") ?: ""
                val inputHash = sha256(password)
                if (storedHash.isNotEmpty() && storedHash != inputHash) {
                    callback.onPasswordInvalid()
                    return@addOnSuccessListener
                }
            }
            val batch = firestore.batch()
            for (optTitle in newOptions) {
                val optCode = "opt_" + UUID.randomUUID().toString().substring(0, 8)
                val optRef = pollRef.collection("options").document(optCode)
                batch.set(optRef, hashMapOf("code" to optCode, "title" to optTitle, "count" to 0))
            }
            batch.update(pollRef, "optionCount", FieldValue.increment(newOptions.size.toLong()))
            batch.commit().addOnSuccessListener {
                getVoteData(voteCode, user, object : VoteDataSource.GetVoteDataCallback {
                    override fun onVoteDataLoaded(voteData: VoteData) {
                        callback.onSuccess(voteData)
                    }

                    override fun onVoteDataNotAvailable() {
                        callback.onFailure()
                    }
                })
            }.addOnFailureListener {
                callback.onFailure()
            }
        }.addOnFailureListener {
            callback.onFailure()
        }
    }

    override fun pollVote(voteCode: String, password: String, pollOptions: List<String>, user: User, callback: VoteDataSource.PollVoteCallback?) {
        val pollRef = firestore.collection("polls").document(voteCode)
        val uid = getUid(user)
        val voterRef = pollRef.collection("voters").document(uid)

        pollRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                callback?.onFailure()
                return@addOnSuccessListener
            }
            val needPw = doc.getBoolean("isNeedPassword") ?: false
            if (needPw) {
                val storedHash = doc.getString("passwordHash") ?: ""
                val inputHash = sha256(password)
                if (storedHash.isNotEmpty() && storedHash != inputHash) {
                    callback?.onPasswordInvalid()
                    return@addOnSuccessListener
                }
            }
            firestore.runTransaction { transaction ->
                val voterDoc = transaction.get(voterRef)
                if (voterDoc.exists()) {
                    return@runTransaction false
                }
                for (optCode in pollOptions) {
                    val optRef = pollRef.collection("options").document(optCode)
                    transaction.update(optRef, "count", FieldValue.increment(1))
                }
                transaction.update(pollRef, "totalVotes", FieldValue.increment(pollOptions.size.toLong()))
                transaction.set(voterRef, hashMapOf(
                    "userId" to uid,
                    "polledOptionCodes" to pollOptions,
                    "timestamp" to System.currentTimeMillis()
                ))
                val userHistoryRef = firestore.collection("users").document(uid).collection("participated").document(voteCode)
                transaction.set(userHistoryRef, hashMapOf("pollId" to voteCode, "timestamp" to System.currentTimeMillis()))
                true
            }.addOnSuccessListener { success ->
                if (success) {
                    getVoteData(voteCode, user, object : VoteDataSource.GetVoteDataCallback {
                        override fun onVoteDataLoaded(voteData: VoteData) {
                            callback?.onSuccess(voteData)
                        }

                        override fun onVoteDataNotAvailable() {
                            callback?.onFailure()
                        }
                    })
                } else {
                    callback?.onFailure()
                }
            }.addOnFailureListener {
                callback?.onFailure()
            }
        }.addOnFailureListener {
            callback?.onFailure()
        }
    }

    override fun favoriteVote(voteCode: String, isFavorite: Boolean, user: User, callback: VoteDataSource.FavoriteVoteCallback) {
        val uid = getUid(user)
        val favRef = firestore.collection("users").document(uid).collection("favorites").document(voteCode)
        if (isFavorite) {
            favRef.set(hashMapOf("pollId" to voteCode, "timestamp" to System.currentTimeMillis()))
                .addOnSuccessListener { callback.onSuccess(true) }
                .addOnFailureListener { callback.onFailure() }
        } else {
            favRef.delete()
                .addOnSuccessListener { callback.onSuccess(false) }
                .addOnFailureListener { callback.onFailure() }
        }
    }

    override fun createVote(voteSetting: VoteData, options: List<String>, image: File?, callback: VoteDataSource.GetVoteDataCallback) {
        val pollId = "poll_" + UUID.randomUUID().toString().substring(0, 8)
        val now = System.currentTimeMillis()
        val uid = getUid(null)

        val pollMap = hashMapOf<String, Any>(
            "pollId" to pollId,
            "title" to (voteSetting.title ?: ""),
            "authorId" to uid,
            "authorName" to (voteSetting.authorName ?: "匿名"),
            "authorIcon" to (voteSetting.authorIcon ?: ""),
            "category" to (voteSetting.category ?: "new"),
            "security" to (voteSetting.security ?: "00"),
            "isNeedPassword" to (voteSetting.isNeedPassword ?: false),
            "isCanPreviewResult" to (voteSetting.isCanPreviewResult ?: true),
            "isUserCanAddOption" to (voteSetting.isUserCanAddOption ?: false),
            "minOption" to (voteSetting.minOption ?: 1),
            "maxOption" to (voteSetting.maxOption ?: 1),
            "optionCount" to options.size,
            "totalVotes" to 0,
            "createdAt" to now,
            "startTime" to if (voteSetting.startTime > 0) voteSetting.startTime else now,
            "endTime" to if (voteSetting.endTime > 0) voteSetting.endTime else now + 86400000L * 30L
        )

        if (voteSetting.isNeedPassword && !voteSetting.password.isNullOrEmpty()) {
            pollMap["passwordHash"] = sha256(voteSetting.password)
        }

        fun saveToFirestore(imageUrl: String?) {
            if (imageUrl != null) {
                pollMap["imageUrl"] = imageUrl
            }
            val pollRef = firestore.collection("polls").document(pollId)
            val batch = firestore.batch()
            batch.set(pollRef, pollMap)

            val topOpts = mutableListOf<Map<String, Any>>()
            for (i in options.indices) {
                val optCode = "opt_" + UUID.randomUUID().toString().substring(0, 8)
                val optRef = pollRef.collection("options").document(optCode)
                batch.set(optRef, hashMapOf("code" to optCode, "title" to options[i], "count" to 0))
                if (i < 2) {
                    topOpts.add(hashMapOf("code" to optCode, "title" to options[i], "count" to 0))
                }
            }
            pollMap["topOptions"] = topOpts
            batch.set(pollRef, pollMap, SetOptions.merge())

            val userCreatedRef = firestore.collection("users").document(uid).collection("created").document(pollId)
            batch.set(userCreatedRef, hashMapOf("pollId" to pollId, "timestamp" to now))

            batch.commit().addOnSuccessListener {
                voteSetting.voteCode = pollId
                voteSetting.voteImage = imageUrl
                callback.onVoteDataLoaded(voteSetting)
            }.addOnFailureListener {
                callback.onVoteDataNotAvailable()
            }
        }

        if (image != null && image.exists()) {
            val storageRef = FirebaseStorage.getInstance().reference.child("poll_covers/${pollId}_${System.currentTimeMillis()}.jpg")
            storageRef.putFile(Uri.fromFile(image)).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(uri.toString())
                }.addOnFailureListener {
                    saveToFirestore(null)
                }
            }.addOnFailureListener {
                saveToFirestore(null)
            }
        } else {
            saveToFirestore(null)
        }
    }

    override fun getHotVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        initSeedDataIfNeeded(firestore)
        firestore.collection("polls")
            .orderBy("totalVotes", Query.Direction.DESCENDING)
            .limit(VoteDataRepository.PAGE_COUNT.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val list = mutableListOf<VoteData>()
                    for (doc in task.result!!) {
                        mapDocToVoteData(doc)?.let { list.add(it) }
                    }
                    callback.onVoteListLoaded(list)
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    override fun getNewVoteList(offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        initSeedDataIfNeeded(firestore)
        firestore.collection("polls")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(VoteDataRepository.PAGE_COUNT.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val list = mutableListOf<VoteData>()
                    for (doc in task.result!!) {
                        mapDocToVoteData(doc)?.let { list.add(it) }
                    }
                    callback.onVoteListLoaded(list)
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    override fun getCreateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val uid = if (!targetUser.userCode.isNullOrEmpty()) targetUser.userCode else getUid(loginUser)
        firestore.collection("polls")
            .whereEqualTo("authorId", uid)
            .limit(VoteDataRepository.PAGE_COUNT.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val list = mutableListOf<VoteData>()
                    for (doc in task.result!!) {
                        mapDocToVoteData(doc)?.let { list.add(it) }
                    }
                    callback.onVoteListLoaded(list)
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    override fun getParticipateVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val uid = if (!targetUser.userCode.isNullOrEmpty()) targetUser.userCode else getUid(loginUser)
        firestore.collection("users").document(uid).collection("participated")
            .limit(VoteDataRepository.PAGE_COUNT.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && !task.result!!.isEmpty) {
                    val pollIds = task.result!!.documents.mapNotNull { it.getString("pollId") ?: it.id }
                    val tasks = pollIds.map { id -> firestore.collection("polls").document(id).get() }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { snapshots ->
                        val list = mutableListOf<VoteData>()
                        for (doc in snapshots) {
                            if (doc.exists()) {
                                mapDocToVoteData(doc)?.let { list.add(it) }
                            }
                        }
                        callback.onVoteListLoaded(list)
                    }.addOnFailureListener {
                        callback.onVoteListNotAvailable()
                    }
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    override fun getFavoriteVoteList(offset: Int, loginUser: User, targetUser: User, callback: VoteDataSource.GetVoteListCallback) {
        val uid = if (!targetUser.userCode.isNullOrEmpty()) targetUser.userCode else getUid(loginUser)
        firestore.collection("users").document(uid).collection("favorites")
            .limit(VoteDataRepository.PAGE_COUNT.toLong())
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && !task.result!!.isEmpty) {
                    val pollIds = task.result!!.documents.mapNotNull { it.getString("pollId") ?: it.id }
                    val tasks = pollIds.map { id -> firestore.collection("polls").document(id).get() }
                    Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { snapshots ->
                        val list = mutableListOf<VoteData>()
                        for (doc in snapshots) {
                            if (doc.exists()) {
                                val vd = mapDocToVoteData(doc)
                                if (vd != null) {
                                    vd.isFavorite = true
                                    list.add(vd)
                                }
                            }
                        }
                        callback.onVoteListLoaded(list)
                    }.addOnFailureListener {
                        callback.onVoteListNotAvailable()
                    }
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    override fun getSearchVoteList(keyword: String, offset: Int, user: User, callback: VoteDataSource.GetVoteListCallback) {
        firestore.collection("polls")
            .limit(50)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val list = mutableListOf<VoteData>()
                    val q = keyword.trim().lowercase()
                    for (doc in task.result!!) {
                        val title = doc.getString("title") ?: ""
                        if (title.lowercase().contains(q)) {
                            mapDocToVoteData(doc)?.let { list.add(it) }
                        }
                    }
                    callback.onVoteListLoaded(list)
                } else {
                    callback.onVoteListNotAvailable()
                }
            }
    }

    companion object {
        private const val TAG = "RemoteVoteDataSource"
        private var INSTANCE: RemoteVoteDataSource? = null

        @JvmStatic
        fun getInstance(): RemoteVoteDataSource {
            if (INSTANCE == null) {
                synchronized(RemoteVoteDataSource::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = RemoteVoteDataSource()
                    }
                }
            }
            return INSTANCE!!
        }

        @JvmStatic
        fun mapDocToVoteData(doc: DocumentSnapshot?): VoteData? {
            if (doc == null || !doc.exists()) return null
            val vd = VoteData()
            val pollId = doc.getString("pollId") ?: doc.id
            vd.voteCode = pollId
            vd.title = doc.getString("title")
            vd.authorCode = doc.getString("authorId")
            vd.authorName = doc.getString("authorName") ?: "匿名"
            vd.authorIcon = doc.getString("authorIcon")
            vd.voteImage = doc.getString("imageUrl")
            vd.category = doc.getString("category")
            vd.security = doc.getString("security") ?: VoteData.SECURITY_PUBLIC
            vd.isNeedPassword = doc.getBoolean("isNeedPassword") ?: false
            vd.isCanPreviewResult = doc.getBoolean("isCanPreviewResult") ?: true
            vd.isUserCanAddOption = doc.getBoolean("isUserCanAddOption") ?: false
            vd.minOption = (doc.getLong("minOption") ?: 1L).toInt()
            vd.maxOption = (doc.getLong("maxOption") ?: 1L).toInt()
            vd.optionCount = (doc.getLong("optionCount") ?: 0L).toInt()
            vd.pollCount = (doc.getLong("totalVotes") ?: 0L).toInt()

            val now = System.currentTimeMillis()
            val startT = doc.getLong("startTime") ?: doc.getLong("createdAt") ?: now
            vd.startTime = startT
            val endT = doc.getLong("endTime") ?: (startT + 86400000L * 30L)
            vd.endTime = endT

            val topOpts = doc.get("topOptions") as? List<Map<String, Any>>
            if (topOpts != null) {
                if (topOpts.isNotEmpty()) {
                    val m1 = topOpts[0]
                    vd.option1Code = m1["code"] as? String
                    vd.option1Title = m1["title"] as? String
                    vd.option1Count = (m1["count"] as? Number)?.toInt() ?: 0
                    vd.optionTopCode = vd.option1Code
                    vd.optionTopTitle = vd.option1Title
                    vd.optionTopCount = vd.option1Count
                }
                if (topOpts.size > 1) {
                    val m2 = topOpts[1]
                    vd.option2Code = m2["code"] as? String
                    vd.option2Title = m2["title"] as? String
                    vd.option2Count = (m2["count"] as? Number)?.toInt() ?: 0
                }
            }
            return vd
        }

        private fun sha256(input: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
                val hexString = StringBuilder()
                for (b in hash) {
                    val hex = Integer.toHexString(0xff and b.toInt())
                    if (hex.length == 1) hexString.append('0')
                    hexString.append(hex)
                }
                hexString.toString()
            } catch (e: Exception) {
                ""
            }
        }
    }
}
