package com.example.bakingtime;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
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

public class MainActivity extends AppCompatActivity implements MainAdapter.ListItemClickLister{

    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String URL ="https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json";

    private RecyclerView mRecycleView;
    private MainAdapter mMainAdapter;
    private List<Cake> cakes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecycleView = findViewById(R.id.rv_main);
        cakes = new ArrayList<>();
        mMainAdapter = new MainAdapter(cakes, MainActivity.this);
        mRecycleView.setAdapter(mMainAdapter);
        mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));

        Thread thread = new Thread(() -> {
            AppDatabase database = AppDatabase.getInstance();
            cakes = database.cakeDao().getAll();
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if(activeNetwork != null && cakes.size() > 0){
                Log.d(TAG, "Getting data from the DB.");
                runOnUiThread(() -> {
                    mMainAdapter = new MainAdapter(cakes, MainActivity.this);
                    mRecycleView.setAdapter(mMainAdapter);
                    mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
                });
            } else {
                Log.d(TAG, "Getting data from the web.");
                loadDataFromWebAndSaveItInDB();
            }
        });
        thread.start();
    }

    private void loadDataFromWebAndSaveItInDB() {
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, URL, null,
                response -> {

                    processAndSaveData(response);

                    runOnUiThread(() -> {
                        mMainAdapter = new MainAdapter(cakes, MainActivity.this);
                        mRecycleView.setAdapter(mMainAdapter);
                        mRecycleView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
                    });
                }, error -> {
                    //textView.setText("That didn't work!");
                    Log.d(TAG, error.toString());
                });

        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

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

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(MainActivity.this, RecipeActivity.class);
        intent.putExtra("cakeId", String.valueOf(cakes.get(clickedItemIndex).getId()) );
        startActivity(intent);
    }
}
