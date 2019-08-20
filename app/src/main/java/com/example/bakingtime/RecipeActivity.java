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
    private int currentStep = -1;
    private List<Step> steps;
    private List<Ingredient> ingredients;

    private Boolean isTwoPane;
    private static String currentFragment;

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
        Log.d(TAG, "[RecipeActivity] isTwoPane: " + isTwoPane);

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
            currentFragment = DETAIL_FRAGMENT_TAG;
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
                if( currentStep == -1 || currentFragment.equals(INGREDIENT_FRAGMENT_TAG) ) {
                    startIngredientFragment(true);
                } else {
                    startStepFragment(currentStep);
                }
            } else {
                if(currentFragment.equals(DETAIL_FRAGMENT_TAG)){
                    startDetailsFragment();
                } else if(currentFragment.equals(STEP_FRAGMENT_TAG)){
                    Fragment fragment = fragmentManager.findFragmentByTag(STEP_FRAGMENT_TAG);
                    if(fragment == null) {
                        startStepFragment(0);
                    } else {
                        replaceFragmentWithoutBackstack(R.id.recipe_main, STEP_FRAGMENT_TAG, fragment);
                    }
                } else{
                    Fragment fragment = fragmentManager.findFragmentByTag(INGREDIENT_FRAGMENT_TAG);
                    if(fragment == null){
                     startIngredientFragment(true);
                    } else {
                        replaceFragmentWithoutBackstack(R.id.recipe_main, INGREDIENT_FRAGMENT_TAG, fragment);
                    }
                }
            }
        }
    }

    /**
     * Creating the menu
     * @param menu object
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu);

        menu.findItem(R.id.menu_back_to_list).setVisible(true);
        return true;
    }

    /**
     * The menu items selected
     * @param item that was selected
     * @return super or true
     */
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

    // To see the logs if orientation changes
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

    /**
     * Saving instance state
     * @param outState of the saved data
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", id);
        outState.putParcelableArrayList("steps", (ArrayList<? extends Parcelable>) steps);
        outState.putParcelableArrayList("ingredients", (ArrayList<? extends Parcelable>) ingredients);
        outState.putString("currentFragment", currentFragment);
        outState.putInt("currentStep",currentStep);
    }

    /**
     * Implemented interface of the @RecipeDetailsFragment
     * @param clickedItemIndex id the step id that we want to load
     */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        startStepFragment(clickedItemIndex);
        Log.d(TAG, "steps clicked");
    }

    /**
     * Implemented interface @RecipeIngredientFragment
     * This will lunch the ingredients list fragment
     */
    @Override
    public void onIngredientClickedListener() {
        startIngredientFragment(true);
    }

    /**
     * Implemented interface from @RecipeStepFragment
     * It will lunch an other fragment that shows the next step of the recipe list
     * @param stepId is the recipe step id
     */
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

    // DETAIL FRAGMENT
    /**
     * loads the step data and starts the details fragment
     */
    private void loadStepsAndStartDetailFragment(){
        Log.d(TAG, "Loading steps from db.");

        Thread thread = new Thread( () -> {
            steps = appDatabase.stepDao().loadAllById(id);
            startDetailsFragment();
        });
        thread.start();
    }

    /**
     * Starting the details fragment
     */
    private void startDetailsFragment(){
        RecipeDetailsFragment recipeDetailsFragment
                = RecipeDetailsFragment
                .newInstance(steps, id);

        if(!isTwoPane) currentFragment = DETAIL_FRAGMENT_TAG;

        Fragment fragment = fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
        if(fragment == null)
            addFragment(R.id.recipe_main, DETAIL_FRAGMENT_TAG, recipeDetailsFragment);
    }

    // STEP FRAGMENT

    /**
     * Starting the step fragment
     * @param stepId is the id of the step that we are loading
     */
    private void startStepFragment(int stepId){
        if(stepId <= 0 || stepId > steps.size() -1){
            stepId = 0;
        }
        currentStep = stepId;
        RecipeStepFragment stepFragment =
                RecipeStepFragment
                        .newInstance(steps.get(stepId));

        currentFragment = STEP_FRAGMENT_TAG;

        if(isTwoPane) {
            replaceFragmentWithoutBackstack(R.id.recipe_second_frame, STEP_FRAGMENT_TAG,stepFragment);
        } else {
            replaceFragmentWithBackstack(STEP_FRAGMENT_TAG, stepFragment);
        }
    }

    // INGREDIENT FRAGMENT

    /**
     * Starting the ingredient fragment
     * @param isReplace is a boolean to see if we need to lunch it on start
     */
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
        } else
            addFragment(R.id.recipe_second_frame, INGREDIENT_FRAGMENT_TAG, ingredientsFragment);
    }

    /**
     * loads the ingredients and starts ingredients fragment
     * @param start is used to automatically start the fragment or not depending on the screen size
     *              if two pane mode it will lunch otherwise we start it on button click
     */
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

    /**
     * Adding the fragment to the container
     * @param container we want to load the fragment
     * @param name name of the fragment
     * @param fragment the fragment we are adding
     */
    private void addFragment(int container, String name, Fragment fragment){
        Log.d(TAG, name + " fragment was added");
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .add(container ,fragment, name)
                .commit();
    }

    /**
     * Adding the fragment to the container WITHOUT adding it to the back stack
     * We are checking if fragments already added due to multi screen load error
     * @param container we want to load the fragment
     * @param name name of the fragment
     * @param fragment the fragment we are adding
     */
    private void replaceFragmentWithoutBackstack(int container, String name, Fragment fragment){
        Log.d(TAG, name + " fragment was replaced without backstack");
        fragmentManager
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(container, fragment, name)
                .commit();
    }

    /**
     * Replacing the fragment in the container and adding it to the back stack
     * We are checking if there is any fragment already added due to multi screen load error
     * @param name name of the fragment
     * @param fragment the fragment we are adding
     */
    private void replaceFragmentWithBackstack(String name, Fragment fragment){
        Log.d(TAG, name + " fragment was replaced with backstack");
        fragmentManager
                .beginTransaction()
                .addToBackStack(name)
                //.setReorderingAllowed(true)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.recipe_main, fragment, name)
                .commit();
    }
}
