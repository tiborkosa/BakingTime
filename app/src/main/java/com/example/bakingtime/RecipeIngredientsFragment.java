package com.example.bakingtime;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bakingtime.adapters.IngredientsAdapter;
import com.example.bakingtime.models.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class RecipeIngredientsFragment extends Fragment {

    public static final String CAKE_INGREDIENTS = "ingredients";
    private static final String TAG = RecipeIngredientsFragment.class.getSimpleName();

    private RecyclerView rv;
    private IngredientsAdapter adapter;
    private List<Ingredient> ingredients;

    public RecipeIngredientsFragment() {}

    public static RecipeIngredientsFragment newInstance(List<Ingredient> ingredients){
        Log.d(TAG, "New instance of " + TAG + " created.");

        RecipeIngredientsFragment fragment = new RecipeIngredientsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(CAKE_INGREDIENTS, (ArrayList<? extends Parcelable>) ingredients);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        Log.d(TAG, "onCreate.");
        if(getArguments() != null){
            ingredients = getArguments().getParcelableArrayList(CAKE_INGREDIENTS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView.");
        View rootView = inflater.inflate(R.layout.recipe_ingredient_fragment, container, false);
        rv = rootView.findViewById(R.id.rv_ingredients);

        adapter = new IngredientsAdapter(ingredients);
        rv.setAdapter(adapter);

        return rootView;
    }
}
