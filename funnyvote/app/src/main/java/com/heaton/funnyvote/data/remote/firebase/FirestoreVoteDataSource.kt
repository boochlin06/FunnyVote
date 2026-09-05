package com.heaton.funnyvote.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity
import com.heaton.funnyvote.data.local.entity.VoteWithDetails
import com.heaton.funnyvote.data.remote.VoteRemoteDataSource
import com.heaton.funnyvote.util.NgramUtil
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreVoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : VoteRemoteDataSource {

    private val pollsCollection = firestore.collection("polls")

    override fun getAllVotes(): Flow<List<VoteWithDetails>> = callbackFlow {
        val registration = pollsCollection
            .whereEqualTo("security", "00")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    mapDocToVoteWithDetails(doc)
                }.orEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    override fun getVotesByCategory(category: String): Flow<List<VoteWithDetails>> = callbackFlow {
        if (category == "favorite") {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val favRef = firestore.collection("users").document(userId).collection("favorites")
            val registration = favRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val pollIds = snapshot?.documents?.mapNotNull { it.id }.orEmpty()
                if (pollIds.isEmpty()) {
                    trySend(emptyList())
                } else {
                    pollsCollection.whereIn("pollId", pollIds.take(10)).get()
                        .addOnSuccessListener { pollDocs ->
                            val list = pollDocs.documents.mapNotNull { doc ->
                                mapDocToVoteWithDetails(doc)?.let {
                                    it.copy(vote = it.vote.copy(isFavorite = true))
                                }
                            }
                            trySend(list)
                        }
                        .addOnFailureListener {
                            trySend(emptyList())
                        }
                }
            }
            awaitClose { registration.remove() }
            return@callbackFlow
        }

        val query = if (category == "hot") {
            pollsCollection
                .whereEqualTo("security", "00")
                .orderBy("totalVotes", Query.Direction.DESCENDING)
                .limit(50)
        } else {
            pollsCollection
                .whereEqualTo("security", "00")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                mapDocToVoteWithDetails(doc)
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override fun getVoteDetail(voteCode: String): Flow<VoteWithDetails?> = callbackFlow {
        val pollDocRef = pollsCollection.document(voteCode)
        val optionsRef = pollDocRef.collection("options").orderBy("displayOrder")
        val currentUid = auth.currentUser?.uid
        val voterDocRef = currentUid?.let { pollDocRef.collection("voters").document(it) }
        val favDocRef = currentUid?.let { firestore.collection("users").document(it).collection("favorites").document(voteCode) }

        var currentOptions = emptyList<OptionEntity>()
        var currentPollDoc: com.google.firebase.firestore.DocumentSnapshot? = null

        fun tryEmit() {
            val doc = currentPollDoc ?: return
            if (!doc.exists()) {
                close(NoSuchElementException("Poll $voteCode not found"))
                return
            }
            val base = mapDocToVoteWithDetails(doc) ?: return
            if (voterDocRef != null) {
                voterDocRef.get().addOnSuccessListener { voterDoc ->
                    val isVoted = voterDoc.exists()
                    @Suppress("UNCHECKED_CAST")
                    val chosenCodes = voterDoc.get("selectedOptionCodes") as? List<String> ?: emptyList()
                    val resolvedOptions = (if (currentOptions.isNotEmpty()) currentOptions else base.options).map { opt ->
                        opt.copy(isUserChoiced = chosenCodes.contains(opt.optionCode))
                    }

                    if (favDocRef != null) {
                        favDocRef.get().addOnSuccessListener { favDoc ->
                            val isFav = favDoc.exists()
                            trySend(base.copy(
                                vote = base.vote.copy(isVoted = isVoted, isFavorite = isFav),
                                options = resolvedOptions
                            ))
                        }.addOnFailureListener {
                            trySend(base.copy(
                                vote = base.vote.copy(isVoted = isVoted),
                                options = resolvedOptions
                            ))
                        }
                    } else {
                        trySend(base.copy(
                            vote = base.vote.copy(isVoted = isVoted),
                            options = resolvedOptions
                        ))
                    }
                }.addOnFailureListener {
                    trySend(base.copy(options = if (currentOptions.isNotEmpty()) currentOptions else base.options))
                }
            } else {
                trySend(base.copy(options = if (currentOptions.isNotEmpty()) currentOptions else base.options))
            }
        }

        val regOptions = optionsRef.addSnapshotListener { optSnapshot, optError ->
            if (optError != null) {
                close(optError)
                return@addSnapshotListener
            }
            currentOptions = optSnapshot?.documents?.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: ""
                val count = doc.getLong("voteCount")?.toInt() ?: 0
                OptionEntity(
                    id = 0,
                    voteCode = voteCode,
                    optionCode = id,
                    title = title,
                    count = count,
                    isUserChoiced = false
                )
            }.orEmpty()
            tryEmit()
        }

        val regPoll = pollDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) {
                close(NoSuchElementException("Poll $voteCode not found"))
                return@addSnapshotListener
            }
            currentPollDoc = snapshot
            tryEmit()
        }

        awaitClose {
            regOptions.remove()
            regPoll.remove()
        }
    }

    override fun searchVotes(query: String): Flow<List<VoteWithDetails>> = callbackFlow {
        val ngrams = NgramUtil.generateBiGrams(query)
        val firestoreQuery = if (ngrams.isNotEmpty()) {
            pollsCollection
                .whereEqualTo("security", "00")
                .whereArrayContains("searchKeywords", ngrams.first())
                .limit(30)
        } else {
            pollsCollection
                .whereEqualTo("security", "00")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(30)
        }

        val registration = firestoreQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                mapDocToVoteWithDetails(doc)
            }.orEmpty()
            trySend(list)
        }
        awaitClose { registration.remove() }
    }

    override suspend fun submitVote(
        voteCode: String,
        selectedOptionCodes: List<String>,
        userId: String
    ): Result<Unit> = runCatching {
        val pollRef = pollsCollection.document(voteCode)
        val voterRef = pollRef.collection("voters").document(userId)

        firestore.runTransaction { transaction ->
            val voterSnap = transaction.get(voterRef)
            if (voterSnap.exists()) {
                throw IllegalStateException("已經投過票，無法重複投票！")
            }

            // 1. 純寫入 voter 紀錄 (防刷票)
            val voterData = hashMapOf(
                "userId" to userId,
                "selectedOptionCodes" to selectedOptionCodes,
                "votedAt" to System.currentTimeMillis()
            )
            transaction.set(voterRef, voterData)

            // 2. 遞增各選項 voteCount
            for (optCode in selectedOptionCodes) {
                val optRef = pollRef.collection("options").document(optCode)
                transaction.update(optRef, "voteCount", FieldValue.increment(1))
            }

            // 3. 遞增主文檔 totalVotes
            transaction.update(pollRef, "totalVotes", FieldValue.increment(selectedOptionCodes.size.toLong()))

            // 4. 同步寫入使用者參與歷史 users/{userId}/voted_polls/{voteCode}
            val userVotedRef = firestore.collection("users").document(userId)
                .collection("voted_polls").document(voteCode)
            val userVotedData = hashMapOf(
                "pollId" to voteCode,
                "selectedOptionCodes" to selectedOptionCodes,
                "votedAt" to System.currentTimeMillis()
            )
            transaction.set(userVotedRef, userVotedData)
        }.await()
    }

    override suspend fun addNewOption(
        voteCode: String,
        optionTitle: String,
        userId: String
    ): Result<Unit> = runCatching {
        val pollRef = pollsCollection.document(voteCode)
        val optId = "opt_${System.currentTimeMillis()}_${(100..999).random()}"
        val optData = hashMapOf(
            "optionId" to optId,
            "title" to optionTitle,
            "voteCount" to 0,
            "displayOrder" to 99,
            "creatorId" to userId,
            "createdAt" to System.currentTimeMillis()
        )

        val batch = firestore.batch()
        batch.set(pollRef.collection("options").document(optId), optData)
        batch.update(pollRef, "optionCount", FieldValue.increment(1))
        batch.commit().await()
    }

    override suspend fun createVote(
        title: String,
        options: List<String>,
        isPrivate: Boolean,
        password: String?,
        isMultiChoice: Boolean,
        authorId: String,
        authorName: String,
        description: String?,
        imageUrl: String?,
        endTime: Long?
    ): Result<String> = runCatching {
        val pollId = "poll_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val searchKeywords = NgramUtil.generateBiGrams(title)
        val now = System.currentTimeMillis()

        // 反正規化 topOptions 快取
        val topOptions = options.take(2).mapIndexed { index, optTitle ->
            mapOf(
                "optionId" to "opt_${index + 1}",
                "title" to optTitle,
                "voteCount" to 0
            )
        }

        val pollDoc = hashMapOf(
            "pollId" to pollId,
            "title" to title,
            "description" to description,
            "authorId" to authorId,
            "authorName" to authorName,
            "authorIcon" to null,
            "imageUrl" to imageUrl,
            "category" to "hot",
            "security" to if (isPrivate) "01" else "00",
            "isNeedPassword" to !password.isNullOrBlank(),
            "isCanPreviewResult" to true,
            "isUserCanAddOption" to false,
            "minOption" to 1,
            "maxOption" to if (isMultiChoice) 2 else 1,
            "optionCount" to options.size,
            "totalVotes" to 0,
            "searchKeywords" to searchKeywords,
            "topOptions" to topOptions,
            "startTime" to now,
            "endTime" to (endTime ?: (now + 86400000L * 30L)),
            "createdAt" to now
        )

        val batch = firestore.batch()
        val pollRef = pollsCollection.document(pollId)
        batch.set(pollRef, pollDoc)

        options.forEachIndexed { index, optTitle ->
            val optId = "opt_${index + 1}"
            val optRef = pollRef.collection("options").document(optId)
            batch.set(
                optRef,
                hashMapOf(
                    "optionId" to optId,
                    "title" to optTitle,
                    "voteCount" to 0,
                    "displayOrder" to index + 1,
                    "creatorId" to authorId,
                    "createdAt" to now
                )
            )
        }

        if (!password.isNullOrBlank()) {
            val secretHash = hashPollPassword(pollId, password)
            val secureRef = firestore.collection("secure_polls").document(secretHash)
            batch.set(
                secureRef,
                hashMapOf(
                    "pollId" to pollId,
                    "createdAt" to now
                )
            )
        }

        batch.commit().await()
        pollId
    }

    override suspend fun toggleFavorite(
        voteCode: String,
        isFavorite: Boolean,
        userId: String
    ): Result<Unit> = runCatching {
        val favRef = firestore.collection("users").document(userId)
            .collection("favorites").document(voteCode)
        if (isFavorite) {
            favRef.set(mapOf("pollId" to voteCode, "createdAt" to System.currentTimeMillis())).await()
        } else {
            favRef.delete().await()
        }
    }

    override fun getUserParticipatedVotes(userId: String): Flow<List<VoteWithDetails>> = callbackFlow {
        val votedRef = firestore.collection("users").document(userId).collection("voted_polls")
        val registration = votedRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val pollIds = snapshot?.documents?.mapNotNull { it.id }.orEmpty()
            if (pollIds.isEmpty()) {
                trySend(emptyList())
            } else {
                pollsCollection.whereIn("pollId", pollIds.take(20)).get()
                    .addOnSuccessListener { pollDocs ->
                        val list = pollDocs.documents.mapNotNull { doc ->
                            mapDocToVoteWithDetails(doc)?.let {
                                it.copy(vote = it.vote.copy(isVoted = true))
                            }
                        }
                        trySend(list)
                    }
                    .addOnFailureListener {
                        trySend(emptyList())
                    }
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun loadMoreVotes(
        category: String,
        lastVoteCode: String,
        limit: Long
    ): Result<List<VoteWithDetails>> = runCatching {
        val lastDoc = pollsCollection.document(lastVoteCode).get().await()
        if (!lastDoc.exists()) return@runCatching emptyList()

        val query = if (category == "hot") {
            pollsCollection
                .whereEqualTo("security", "00")
                .orderBy("totalVotes", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(limit)
        } else {
            pollsCollection
                .whereEqualTo("security", "00")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .startAfter(lastDoc)
                .limit(limit)
        }
        val snapshot = query.get().await()
        snapshot.documents.mapNotNull { mapDocToVoteWithDetails(it) }
    }

    override fun getVotesByAuthor(authorId: String): Flow<List<VoteWithDetails>> = callbackFlow {
        val registration = pollsCollection
            .whereEqualTo("authorId", authorId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val votes = snapshot?.documents?.mapNotNull { mapDocToVoteWithDetails(it) }.orEmpty()
                trySend(votes)
            }
        awaitClose { registration.remove() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun mapDocToVoteWithDetails(doc: com.google.firebase.firestore.DocumentSnapshot): VoteWithDetails? {
        val pollId = doc.getString("pollId") ?: doc.id
        val title = doc.getString("title") ?: return null
        val authorId = doc.getString("authorId") ?: ""
        val authorName = doc.getString("authorName") ?: "匿名"
        val authorIcon = doc.getString("authorIcon")
        val category = doc.getString("category") ?: "hot"
        val minOption = doc.getLong("minOption")?.toInt() ?: 1
        val maxOption = doc.getLong("maxOption")?.toInt() ?: 1
        val isNeedPassword = doc.getBoolean("isNeedPassword") ?: false
        val isUserCanAddOption = doc.getBoolean("isUserCanAddOption") ?: false
        val isCanPreviewResult = doc.getBoolean("isCanPreviewResult") ?: true
        val totalVotes = doc.getLong("totalVotes")?.toInt() ?: 0
        val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        val description = doc.getString("description")
        val imageUrl = doc.getString("imageUrl")
        val endTime = doc.getLong("endTime") ?: (createdAt + 86400000L * 30L)

        val voteEntity = VoteEntity(
            voteCode = pollId,
            title = title,
            authorId = authorId,
            authorName = authorName,
            authorIcon = authorIcon,
            category = category,
            minOption = minOption,
            maxOption = maxOption,
            isNeedPassword = isNeedPassword,
            isUserCanAddOption = isUserCanAddOption,
            isCanPreviewResult = isCanPreviewResult,
            isFavorite = false,
            isVoted = false,
            totalVotedCount = totalVotes,
            description = description,
            imageUrl = imageUrl,
            endTime = endTime,
            createdAt = createdAt
        )

        val topOptionsRaw = doc.get("topOptions") as? List<Map<String, Any>>
        val options = topOptionsRaw?.mapIndexed { index, map ->
            OptionEntity(
                id = index.toLong(),
                voteCode = pollId,
                optionCode = map["optionId"] as? String ?: "opt_$index",
                title = map["title"] as? String ?: "",
                count = (map["voteCount"] as? Number)?.toInt() ?: 0,
                isUserChoiced = false
            )
        }.orEmpty()

        return VoteWithDetails(
            vote = voteEntity,
            options = options
        )
    }

    override suspend fun verifyPollPassword(voteCode: String, password: String): Boolean {
        return try {
            val secretHash = hashPollPassword(voteCode, password)
            val doc = firestore.collection("secure_polls").document(secretHash).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    private fun hashPollPassword(pollId: String, password: String): String {
        val input = "$pollId:$password"
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
