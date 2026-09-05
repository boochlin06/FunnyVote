package com.heaton.funnyvote.data.repository

import com.heaton.funnyvote.data.local.dao.UserDao
import com.heaton.funnyvote.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUser(): Flow<UserEntity?> {
        return userDao.getUser()
    }

    suspend fun saveUser(user: UserEntity) {
        userDao.insertUser(user)
    }
}
