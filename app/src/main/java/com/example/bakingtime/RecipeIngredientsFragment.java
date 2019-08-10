package com.example.bakingtime;

import android.os.Bundle;
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

import java.util.List;

public class RecipeIngredientsFragment extends Fragment {

    private RecyclerView rv;
    private IngredientsAdapter adapter;

    private int id;
    private List<Ingredient> ingredients;

    public RecipeIngredientsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recipe_ingredient_fragment, container, false);
        rv = rootView.findViewById(R.id.rv_ingredients);

        try{
            adapter = new IngredientsAdapter(ingredients);
            rv.setAdapter(adapter);
        } catch (NullPointerException e){
            throw new ClassCastException("Must set the ingredients!");
        }

        return rootView;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}
