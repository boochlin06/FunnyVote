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

    fun isAnonymous(): Boolean = auth.currentUser?.isAnonymous ?: true

    suspend fun linkOrSignInWithGoogle(idToken: String): Result<UserEntity> = runCatching {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val currentUser = auth.currentUser
        val authResult = if (currentUser != null && currentUser.isAnonymous) {
            try {
                currentUser.linkWithCredential(credential).await()
            } catch (e: Exception) {
                // 若該 Google 帳號已獨立存在，退回直接登入
                auth.signInWithCredential(credential).await()
            }
        } else {
            auth.signInWithCredential(credential).await()
        }
        val firebaseUser = authResult.user ?: throw IllegalStateException("無法取得 Google 使用者資訊")
        val uid = firebaseUser.uid

        val docRef = firestore.collection("users").document(uid)
        val snap = docRef.get().await()
        val currentNickname = snap.getString("userName")
        val nickname = if (currentNickname.isNullOrBlank() || currentNickname == "FunnyVote 訪客") {
            firebaseUser.displayName ?: "FunnyVote 使用者"
        } else {
            currentNickname
        }
        val icon = snap.getString("userIcon") ?: firebaseUser.photoUrl?.toString()

        val updates = hashMapOf<String, Any?>(
            "uid" to uid,
            "userName" to nickname,
            "userIcon" to icon,
            "email" to firebaseUser.email,
            "isAnonymous" to false,
            "updatedAt" to System.currentTimeMillis()
        )
        docRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()

        UserEntity(
            userId = uid,
            userName = nickname,
            userIcon = icon,
            email = firebaseUser.email
        )
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateUserProfile(userName: String, userIcon: String? = null): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val map = hashMapOf<String, Any>("userName" to userName)
        if (userIcon != null) {
            map["userIcon"] = userIcon
        }
        firestore.collection("users").document(uid)
            .update(map).await()
    }
}
