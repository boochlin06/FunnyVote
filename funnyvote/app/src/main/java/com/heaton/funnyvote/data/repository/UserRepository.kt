package com.heaton.funnyvote.data.repository

import com.heaton.funnyvote.data.local.dao.UserDao
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.remote.firebase.FirebaseAuthDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val authDataSource: FirebaseAuthDataSource
) {
    fun getUser(): Flow<UserEntity?> {
        return authDataSource.getCurrentUser().catch {
            emitAll(userDao.getUser())
        }
    }

    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
        authDataSource.updateUserProfile(user.userName, user.userIcon)
    }

    fun isAnonymous(): Boolean = authDataSource.isAnonymous()
 
    suspend fun linkOrSignInWithGoogle(idToken: String): Result<UserEntity> {
        val result = authDataSource.linkOrSignInWithGoogle(idToken)
        result.onSuccess { userEntity ->
            userDao.insertUser(userEntity)
        }
        return result
    }

    fun signOut() {
        authDataSource.signOut()
    }

    suspend fun ensureAuthenticated(): String {
        return authDataSource.ensureAuthenticated()
    }
}
