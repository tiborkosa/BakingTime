package com.example.bakingtime;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;

import com.example.bakingtime.adapters.RecipeAdapter;
import com.example.bakingtime.database.AppDatabase;
import com.example.bakingtime.models.Ingredient;
import com.example.bakingtime.models.Step;

import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends FragmentActivity implements
        RecipeAdapter.ListItemClickListener,
        RecipeDetailsFragment.OnIngredientClickedListener,
        RecipeStepFragment.OnNextStepClickedListener {

    private static final String TAG = RecipeActivity.class.getSimpleName();

    private AppDatabase appDatabase = AppDatabase.getInstance();
    private FragmentManager fragmentManager;
    private int id;
    private List<Step> steps;
    private Boolean isTwoPane = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        /*if(findViewById(R.id.recipe_main_linear) != null){
            isTwoPane = true;
        }*/

        Log.d(TAG, "onCreate called!");

        Intent intent = getIntent();
        id = Integer.valueOf(intent.getStringExtra("cakeId") );

        fragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null){
            Thread thread = new Thread( () -> {
                steps = appDatabase.stepDao().loadAllById(id);
                startStepFragment(steps);
                Log.d(TAG, "Loading steps from db.");

            });
            thread.start();

        } else {
            if(savedInstanceState.containsKey("steps")){
                Log.d(TAG, "Loading steps from state");
                steps = savedInstanceState.getParcelableArrayList("steps");
                startStepFragment(steps);
            }else{
                Log.d(TAG, "Something is not ok with the steps");
            }
        }
    }

    private void startStepFragment(List<Step> steps){
        RecipeDetailsFragment recipeDetailsFragment = new RecipeDetailsFragment();
        recipeDetailsFragment.setId(id);
        recipeDetailsFragment.setmClickListener(RecipeActivity.this);
        recipeDetailsFragment.setSteps(steps);
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.recipe_main,recipeDetailsFragment)
                .commit();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("steps", (ArrayList<? extends Parcelable>) steps);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        RecipeStepFragment stepFragment = new RecipeStepFragment();
        startStepFragment(stepFragment, clickedItemIndex);
        Log.d(TAG, "steps clicked");
    }

    @Override
    public void onIngredientClickedListener(final int cakeId) {
        Thread thread = new Thread( () ->{
                RecipeIngredientsFragment ingredientsFragment = new RecipeIngredientsFragment();
                final List<Ingredient> ingredients = appDatabase.ingredientDao().loadAllById(id);
                ingredientsFragment.setIngredients(ingredients);
                ingredientsFragment.setId(cakeId);
                fragmentManager
                        .beginTransaction()
                        .addToBackStack("ingredient")
                        .setReorderingAllowed(true)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.recipe_main,ingredientsFragment)
                        .commit();
            });

        thread.start();
    }

    @Override
    public void onNextStepClickedListener(int stepId) {

        Log.d(TAG, "this was clicked: " +stepId);

        RecipeStepFragment stepFragment = new RecipeStepFragment();
        if(stepId == steps.size()-1){
            stepId = 0;
        } else {
            stepId++;
        }
        startStepFragment(stepFragment, stepId);
    }

    private void startStepFragment(RecipeStepFragment stepFragment, int stepId){
        stepFragment.setStep(steps.get(stepId));
        fragmentManager
                .beginTransaction()
                .addToBackStack("step")
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.recipe_main,stepFragment)
                .commit();
    }
}
