package com.example.bakingtime;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;

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

    private final static String DETAIL_FRAGMENT_TAG = "detail_tag_key";
    private final static String INGREDIENT_FRAGMENT_TAG = "ing_tag_key";
    private final static String STEP_FRAGMENT_TAG = "step_tag_key";

    private AppDatabase appDatabase = AppDatabase.getInstance();
    private FragmentManager fragmentManager;
    private int id;
    private List<Step> steps;
    private Boolean isTwoPane = false;
    private static String currentFragment = DETAIL_FRAGMENT_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        /*if(findViewById(R.id.recipe_main_linear) != null){
            isTwoPane = true;
        }*/

        Log.d(TAG, "[RecipeActivity] onCreate called!");

        Intent intent = getIntent();
        id = Integer.valueOf(intent.getStringExtra("cakeId") );

        fragmentManager = getSupportFragmentManager();

        if(savedInstanceState == null){
            Thread thread = new Thread( () -> {
                steps = appDatabase.stepDao().loadAllById(id);
                startDetailsFragment(steps);
                Log.d(TAG, "Loading steps from db.");

            });
            thread.start();

        } else {
            id = savedInstanceState.getInt("id");
            steps = savedInstanceState.getParcelableArrayList("steps");
            if(currentFragment.equals(DETAIL_FRAGMENT_TAG)){
                if(savedInstanceState.containsKey("steps")){
                    Log.d(TAG, "Loading steps from state");
                    steps = savedInstanceState.getParcelableArrayList("steps");
                    startDetailsFragment(steps);
                }
            } else if(currentFragment.equals(STEP_FRAGMENT_TAG)){
                RecipeStepFragment rf = (RecipeStepFragment) fragmentManager.findFragmentByTag(STEP_FRAGMENT_TAG);
                fragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.recipe_main,rf, STEP_FRAGMENT_TAG)
                        .commit();
            } else{
                RecipeIngredientsFragment ingf = (RecipeIngredientsFragment) fragmentManager.findFragmentByTag(INGREDIENT_FRAGMENT_TAG);
                fragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.recipe_main,ingf, INGREDIENT_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.d(TAG,"Landscape mode");
        }

        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.d(TAG,"Portrait mode mode");
        }
    }

    private void startDetailsFragment(List<Step> steps){
        RecipeDetailsFragment recipeDetailsFragment
                = RecipeDetailsFragment
                    .newInstance(steps,id);
        currentFragment = DETAIL_FRAGMENT_TAG;
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(R.id.recipe_main,recipeDetailsFragment,DETAIL_FRAGMENT_TAG)
                .commit();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", id);
        outState.putParcelableArrayList("steps", (ArrayList<? extends Parcelable>) steps);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        startStepFragment(clickedItemIndex);
        Log.d(TAG, "steps clicked");
    }

    @Override
    public void onIngredientClickedListener(final int cakeId) {
        Thread thread = new Thread( () -> {
            List<Ingredient> ingredients = appDatabase.ingredientDao().loadAllById(id);
            startIngredientFragment(ingredients);
        });
        thread.start();
    }

    private void startIngredientFragment(List<Ingredient> ingredients){
            RecipeIngredientsFragment ingredientsFragment =
                    RecipeIngredientsFragment
                            .newInstance(ingredients);

            currentFragment = INGREDIENT_FRAGMENT_TAG;
            fragmentManager
                    .beginTransaction()
                    .addToBackStack(INGREDIENT_FRAGMENT_TAG)
                    .setReorderingAllowed(true)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.recipe_main,ingredientsFragment, INGREDIENT_FRAGMENT_TAG)
                    .commit();
    }

    @Override
    public void onNextStepClickedListener(int stepId) {

        Log.d(TAG, "this was clicked: " +stepId);

        if(stepId == steps.size()-1){
            stepId = 0;
        } else {
            stepId++;
        }
        startStepFragment(stepId);
    }

    private void startStepFragment(int stepId){
        RecipeStepFragment stepFragment =
                RecipeStepFragment
                        .newInstance(steps.get(stepId));

        currentFragment = STEP_FRAGMENT_TAG;

        fragmentManager
                .beginTransaction()
                .addToBackStack(STEP_FRAGMENT_TAG)
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.recipe_main,stepFragment, STEP_FRAGMENT_TAG)
                .commit();
    }

}
