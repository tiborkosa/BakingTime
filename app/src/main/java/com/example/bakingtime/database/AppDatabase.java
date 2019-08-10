package com.example.bakingtime.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.util.Log;

import com.example.bakingtime.BakingTime;
import com.example.bakingtime.models.Cake;
import com.example.bakingtime.models.Ingredient;
import com.example.bakingtime.models.Step;

@Database(entities = {Cake.class, Ingredient.class, Step.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private final static String TAG = AppDatabase.class.getSimpleName();

    private final static Object LOCK = new Object();
    private final static String DATABASE_NAME = "baking_db";
    private static AppDatabase sInstance;

    /**
     * Get single instance of the DB
     * @return database instance
     */
    public static AppDatabase getInstance(){
        if(sInstance == null){
            synchronized (LOCK){
                Log.d(TAG, "Creating database instance.");
                sInstance = Room.databaseBuilder(BakingTime.getAppContext(),
                        AppDatabase.class, DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    /**
     * Abstract Database Dao
     */
    public abstract CakeDao cakeDao();
    public abstract StepDao stepDao();
    public abstract IngredientDao ingredientDao();
}
