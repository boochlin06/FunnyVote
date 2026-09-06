package com.heaton.funnyvote.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.heaton.funnyvote.database.Option;
import com.heaton.funnyvote.database.Promotion;
import com.heaton.funnyvote.database.VoteData;
import com.heaton.funnyvote.data.local.dao.OptionDao;
import com.heaton.funnyvote.data.local.dao.PromotionDao;
import com.heaton.funnyvote.data.local.dao.VoteDataDao;

@Database(entities = {VoteData.class, Option.class, Promotion.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract VoteDataDao voteDataDao();
    public abstract OptionDao optionDao();
    public abstract PromotionDao promotionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "funnyvote.db"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
