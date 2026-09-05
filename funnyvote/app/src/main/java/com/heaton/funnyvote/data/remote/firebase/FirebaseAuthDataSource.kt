package com.heaton.funnyvote.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.heaton.funnyvote.data.local.entity.UserEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun ensureAuthenticated(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        val result = auth.signInAnonymously().await()
        val uid = result.user?.uid ?: "anonymous_user"
        initUserProfile(uid)
        return uid
    }

    private suspend fun initUserProfile(uid: String) {
        val docRef = firestore.collection("users").document(uid)
        val snap = docRef.get().await()
        if (!snap.exists()) {
            val user = hashMapOf(
                "uid" to uid,
                "userName" to "FunnyVote 訪客",
                "isAnonymous" to true,
                "createdAt" to System.currentTimeMillis()
            )
            docRef.set(user).await()
        }
    }

    fun getCurrentUser(): Flow<UserEntity?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                val uid = firebaseUser.uid
                firestore.collection("users").document(uid)
                    .addSnapshotListener { snapshot, _ ->
                        val nickname = snapshot?.getString("userName") ?: firebaseUser.displayName ?: "FunnyVote 使用者"
                        val icon = snapshot?.getString("userIcon") ?: firebaseUser.photoUrl?.toString()
                        val email = firebaseUser.email
                        trySend(
                            UserEntity(
                                userId = uid,
                                userName = nickname,
                                userIcon = icon,
                                email = email
                            )
                        )
                    }
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun updateNickname(nickname: String): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        firestore.collection("users").document(uid)
            .update("userName", nickname).await()
    }
}
