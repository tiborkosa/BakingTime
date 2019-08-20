package com.example.bakingtime.database;

import android.os.AsyncTask;
import android.util.Log;

import com.example.bakingtime.models.Cake;
import com.example.bakingtime.models.Ingredient;
import com.example.bakingtime.models.Step;

import java.util.List;

/**
 * Helper class to save data to the DB asynchronously
 */
public class AsyncInsert {

    /**
     * Saving bulk ingredients data
     * @param ingredients to be saved
     */
    public void saveIngredients(final List<Ingredient> ingredients){

        class SaveBulkIngredients extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase.getInstance().ingredientDao().insertAll(ingredients);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Async","Saved ingredients");
            }
        }

        SaveBulkIngredients t = new SaveBulkIngredients();
        t.execute();
    }

    /**
     * Saving bulk step data
     * @param steps to be saved
     */
    public void saveSteps(final List<Step> steps){

        class SaveBulkSteps extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase.getInstance().stepDao().insertAll(steps);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Async","Saved steps");
            }
        }

        SaveBulkSteps t = new SaveBulkSteps();
        t.execute();
    }

    /**
     * Save bulk cake recipes
     * @param cakes to be saved
     */
    public void saveCakes(final List<Cake> cakes){

        class SaveBulkCakes extends AsyncTask<Void, Void, Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                AppDatabase.getInstance().cakeDao().insertAll(cakes);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Log.d("Async","Saved cakes.");
            }
        }

        SaveBulkCakes t = new SaveBulkCakes();
        t.execute();
    }
}
