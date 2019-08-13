package com.example.bakingtime;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.bakingtime.adapters.RecipeAdapter;
import com.example.bakingtime.database.AppDatabase;
import com.example.bakingtime.models.Ingredient;
import com.example.bakingtime.models.Step;

import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends AppCompatActivity implements
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
    private int currentStep = 0;
    private List<Step> steps;
    private List<Ingredient> ingredients;

    private Boolean isTwoPane;
    private static String currentFragment = DETAIL_FRAGMENT_TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getBoolean(R.bool.isTablet)){
            isTwoPane = true;
        }else {
            isTwoPane = false;
        }

        setContentView(R.layout.activity_recipe);
        Log.d(TAG, "[RecipeActivity] onCreate called!");

        Intent intent = getIntent();
        id = Integer.valueOf(intent.getStringExtra("cakeId") );
        String cakeName = intent.getStringExtra("rec_name");
        setTitle(cakeName);

        fragmentManager = getSupportFragmentManager();

        /**
         * If no instance saved load and start the steps
         * If it is two pane mode start the ingredients as well
         */
        if(savedInstanceState == null){
            loadStepsAndStartDetailFragment();
            if(isTwoPane){
                loadAndStartIngredients(true);
            } else {
                loadAndStartIngredients(false);
            }

        } else {
            // load the data from the state
            id = savedInstanceState.getInt("id");
            steps = savedInstanceState.getParcelableArrayList("steps");
            ingredients = savedInstanceState.getParcelableArrayList("ingredients");
            currentFragment = savedInstanceState.getString("currentFragment");
            currentStep = savedInstanceState.getInt("currentStep");
            if(isTwoPane){
                startDetailsFragment();
                if(currentFragment.equals(STEP_FRAGMENT_TAG)){
                    startStepFragment(currentStep);
                }else{
                    startIngredientFragment(true);
                }
            } else {
                if(currentFragment.equals(DETAIL_FRAGMENT_TAG)){
                    startDetailsFragment();
                } else if(currentFragment.equals(STEP_FRAGMENT_TAG)){
                    RecipeStepFragment rf = (RecipeStepFragment) fragmentManager.findFragmentByTag(STEP_FRAGMENT_TAG);
                    //TODO: need to check if this is visible?
                    replaceFragmentWithoutBackstack(R.id.recipe_main, STEP_FRAGMENT_TAG, rf);
                } else{
                    RecipeIngredientsFragment ingf = (RecipeIngredientsFragment) fragmentManager.findFragmentByTag(INGREDIENT_FRAGMENT_TAG);
                    //TODO: need to check if this is visible?
                    replaceFragmentWithoutBackstack(R.id.recipe_main, INGREDIENT_FRAGMENT_TAG, ingf);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.menu_back_to_list).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                Intent intent = new Intent(RecipeActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_back_to_list:
                // go back to list and clear backstack
                if(steps != null)
                    for(int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
                        fragmentManager.popBackStack();
                    }
                startDetailsFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void startDetailsFragment(){
        RecipeDetailsFragment recipeDetailsFragment
                = RecipeDetailsFragment
                .newInstance(steps,id);
        currentFragment = DETAIL_FRAGMENT_TAG;
        addFragment(R.id.recipe_main,DETAIL_FRAGMENT_TAG, recipeDetailsFragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", id);
        outState.putParcelableArrayList("steps", (ArrayList<? extends Parcelable>) steps);
        outState.putParcelableArrayList("ingredients", (ArrayList<? extends Parcelable>) ingredients);
        outState.putString("currentFragment", currentFragment);
        outState.putInt("currentStep",currentStep);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        currentStep = clickedItemIndex;
        startStepFragment(clickedItemIndex);
        Log.d(TAG, "steps clicked");
    }

    @Override
    public void onIngredientClickedListener() {
        startIngredientFragment(true);
    }

    private void startIngredientFragment(boolean isReplace){
        RecipeIngredientsFragment ingredientsFragment =
                RecipeIngredientsFragment
                        .newInstance(ingredients);

        currentFragment = INGREDIENT_FRAGMENT_TAG;
        if(isReplace) {
            if(isTwoPane)
                replaceFragmentWithoutBackstack(R.id.recipe_second_frame,INGREDIENT_FRAGMENT_TAG, ingredientsFragment);
            else
                replaceFragmentWithBackstack(INGREDIENT_FRAGMENT_TAG, ingredientsFragment);
        }

        else
            addFragment(R.id.recipe_second_frame, INGREDIENT_FRAGMENT_TAG, ingredientsFragment);
    }

    @Override
    public void onNextStepClickedListener(int stepId) {

        Log.d(TAG, "this was clicked: " +stepId);
        Log.d(TAG, steps.toString());
        if(stepId == steps.size()-1){
            stepId = 0;
        } else {
            stepId++;
        }
        startStepFragment(stepId);
    }

    private void startStepFragment(int stepId){
        Step step;
        // taking care of index out of bounce exception
        try{
            step = steps.get(stepId);
        } catch (IndexOutOfBoundsException e){
            Log.e(TAG, "startStepFragment index out of bounce happened.");
            step = steps.get(0);
        }
        RecipeStepFragment stepFragment =
                RecipeStepFragment
                        .newInstance(step);

        currentFragment = STEP_FRAGMENT_TAG;
        if(isTwoPane) {
            replaceFragmentWithoutBackstack(R.id.recipe_second_frame, STEP_FRAGMENT_TAG,stepFragment);
        } else {
            replaceFragmentWithBackstack(STEP_FRAGMENT_TAG, stepFragment);
        }
    }

    // load data methods
    private void loadStepsAndStartDetailFragment(){
        Log.d(TAG, "Loading steps from db.");

        Thread thread = new Thread( () -> {
            steps = appDatabase.stepDao().loadAllById(id);
            startDetailsFragment();
        });
        thread.start();
    }

    private void loadAndStartIngredients(final boolean start){
        Log.d(TAG, "Loading ingredients from db.");
        Thread thread = new Thread( () -> {
            ingredients = appDatabase.ingredientDao().loadAllById(id);
            if(start)
                startIngredientFragment(false);
        });
        thread.start();
    }

    //Fragment helper methods

    private void addFragment(int container, String name, Fragment fragment){
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(container ,fragment,name)
                .commit();
    }

    private void replaceFragmentWithoutBackstack(int container, String name, Fragment fragment){
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(container, fragment, name)
                .commit();
    }

    private void replaceFragmentWithBackstack(String name, Fragment fragment){
        fragmentManager
                .beginTransaction()
                .addToBackStack(name)
                .setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.recipe_main,fragment, name)
                .commit();
    }
}
