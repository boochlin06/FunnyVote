package com.heaton.funnyvote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.heaton.funnyvote.data.local.dao.UserDao
import com.heaton.funnyvote.data.local.dao.VoteDao
import com.heaton.funnyvote.data.local.entity.OptionEntity
import com.heaton.funnyvote.data.local.entity.UserEntity
import com.heaton.funnyvote.data.local.entity.VoteEntity

@Database(
    entities = [VoteEntity::class, OptionEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voteDao(): VoteDao
    abstract fun userDao(): UserDao
}
