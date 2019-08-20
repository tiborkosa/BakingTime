package com.example.bakingtime;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.bakingtime.IdlingResource.SimpleIdlingResource;
import com.example.bakingtime.adapters.MainAdapter;
import com.example.bakingtime.database.AppDatabase;
import com.example.bakingtime.database.AsyncInsert;
import com.example.bakingtime.models.Cake;
import com.example.bakingtime.models.Ingredient;
import com.example.bakingtime.models.Step;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MainAdapter.ListItemClickLister{

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String URL ="https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    // for testing the UI
    @Nullable
    private SimpleIdlingResource mIdlingResource;

    @BindView(R.id.rv_main) RecyclerView mRecycleView;
    @BindView(R.id.tv_error_main) TextView mErrorMessage;
    private MainAdapter mMainAdapter;
    private List<Cake> cakes;


    /**
     * setting up the idling resource for testing the UI
     * @return idling resource instance
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }

    /**
     * Create the activity
     * @param savedInstanceState saved data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        cakes = new ArrayList<>();
        mMainAdapter = new MainAdapter(cakes, MainActivity.this);
        mRecycleView.setAdapter(mMainAdapter);
        mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, getColumns()));

        /**
         * Starting new thread that either loads the data from the web or the db
         */
        Thread thread = new Thread(() -> {
            AppDatabase database = AppDatabase.getInstance();
            cakes = database.cakeDao().getAll();
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if (mIdlingResource != null) {
                mIdlingResource.setIdleState(false);
            }

            if(activeNetwork != null && cakes.size() > 0){
                Log.d(TAG, "Getting data from the DB.");
                runOnUiThread(() -> {
                    mMainAdapter = new MainAdapter(cakes, MainActivity.this);
                    mRecycleView.setAdapter(mMainAdapter);
                    mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, getColumns()));

                    if (mIdlingResource != null) {
                        mIdlingResource.setIdleState(true);
                    }
                });
            } else {
                Log.d(TAG, "Getting data from the web.");
                loadDataFromWebAndSaveItInDB();
            }
        });
        thread.start();

        getIdlingResource();
    }

    /**
     * Getting the grid columns depending of the device size
     * @return
     */
    private int getColumns(){
        if(getResources().getBoolean(R.bool.isTablet)) return 2;
        return 1;
    }

    /**
     * Loading data from the web, process it and update UI
     */
    private void loadDataFromWebAndSaveItInDB() {
        RequestQueue queue = Volley.newRequestQueue(this);
        mErrorMessage.setVisibility(View.INVISIBLE);
        // Request a string response from the provided URL.
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {
                    processAndSaveData(response);

                    runOnUiThread(() -> {
                        mMainAdapter = new MainAdapter(cakes, MainActivity.this);
                        mRecycleView.setAdapter(mMainAdapter);
                        mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, getColumns()));
                        if (mIdlingResource != null) {
                            mIdlingResource.setIdleState(true);
                        }
                    });
                }, error -> {
                    mErrorMessage.setVisibility(View.VISIBLE);
                    Log.d(TAG, error.toString());
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    /**
     * Processing the loaded data from the web and save it in the DB
     * @param response from the web
     */
    private void processAndSaveData(JSONArray response) {
        cakes = new ArrayList<>();
        List<Ingredient> ingredients = new ArrayList<>();
        List<Step> steps = new ArrayList<>();

        try {
            for (int i = 0; i < response.length(); i++) {
                JSONObject cake = response.getJSONObject(i);
                cakes.add(new Gson().fromJson(cake.toString(), Cake.class));
                int cakeId = cake.getInt("id");

                JSONArray ingredientList = cake.getJSONArray("ingredients");
                if(ingredientList != null){
                    for(int j = 0; j < ingredientList.length(); j++){
                        Ingredient ingredient = new Gson()
                                .fromJson(ingredientList.get(j).toString(), Ingredient.class);
                        ingredient.setId(cakeId);
                        ingredients.add(ingredient);
                    }
                }
                JSONArray stepsList = cake.getJSONArray("steps");
                if(stepsList != null){
                    for(int j = 0; j < stepsList.length(); j++){
                        Step step = new Gson()
                                .fromJson(stepsList.get(j).toString(), Step.class);
                        step.setParentId(cakeId);
                        steps.add(step);
                    }
                }

            }
        } catch (JSONException e){
            Log.d(TAG, "Issue parsing the data: " + e.getMessage());
            Log.d(TAG, e.toString());
        }
        // insert cakes to the db
        AsyncInsert asyncInsertCakes = new AsyncInsert();
        asyncInsertCakes.saveCakes(cakes);

        AsyncInsert asyncInsertIng = new AsyncInsert();
        asyncInsertIng.saveIngredients(ingredients);

        AsyncInsert asyncInsertSteps = new AsyncInsert();
        asyncInsertSteps.saveSteps(steps);
    }

    /**
     * Implemented method from the @MainAdapter
     * @param clickedItemIndex opens the RecipeActivity of the selected Cake
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra("cakeId", String.valueOf(cakes.get(clickedItemIndex).getId()) );
        intent.putExtra("rec_name", cakes.get(clickedItemIndex).getName());
        startActivity(intent);
    }

    /**
     * Creating the menu for the sake of having one
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.menu_back_to_list).setVisible(false);
        return true;
    }

    // will not implement onOptionMenuClicked because we are already "Home"
}
