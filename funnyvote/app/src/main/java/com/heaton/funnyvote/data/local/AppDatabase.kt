package com.heaton.funnyvote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heaton.funnyvote.data.local.dao.UserDao
import com.heaton.funnyvote.data.local.dao.VoteDao
import com.heaton.funnyvote.data.local.entity.Option
import com.heaton.funnyvote.data.local.entity.User
import com.heaton.funnyvote.data.local.entity.VoteData

@Database(
    entities = [VoteData::class, User::class, Option::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voteDao(): VoteDao
    abstract fun userDao(): UserDao
}
