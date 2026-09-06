package com.heaton.funnyvote.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.heaton.funnyvote.database.Promotion;

import java.util.List;

@Dao
public interface PromotionDao {
    @Query("SELECT * FROM promotions")
    List<Promotion> loadAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Promotion> list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplaceInTx(List<Promotion> list);

    @Query("DELETE FROM promotions")
    void deleteAll();
}
