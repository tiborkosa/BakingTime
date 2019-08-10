package com.example.bakingtime.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.bakingtime.models.Step;

import java.util.List;

@Dao
public interface StepDao {

    @Query("SELECT * FROM steps order by parentId, id")
    List<Step> loadAll();

    @Query("SELECT * FROM steps WHERE parentId = :parentId order by id")
    List<Step> loadAllById(int parentId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Step> steps);

}
