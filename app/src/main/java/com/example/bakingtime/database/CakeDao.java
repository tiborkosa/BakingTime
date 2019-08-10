package com.example.bakingtime.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.bakingtime.models.Cake;

import java.util.List;

@Dao
public interface CakeDao {
    @Query("SELECT * FROM cakes")
    List<Cake> getAll();

    @Query("SELECT * FROM cakes WHERE id = :id")
    Cake findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Cake> cakes);


}
