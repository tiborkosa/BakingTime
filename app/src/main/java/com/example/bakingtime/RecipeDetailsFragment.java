package com.example.bakingtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bakingtime.adapters.RecipeAdapter;
import com.example.bakingtime.models.Step;

import java.util.List;

public class RecipeDetailsFragment extends Fragment {

    private static final String TAG = RecipeActivity.class.getSimpleName();

    private RecyclerView rv;
    private RecipeAdapter adapter;
    private Button mIngButton;
    private int id;
    private RecipeAdapter.ListItemClickListener mClickListener;
    private List<Step> steps;

    OnIngredientClickedListener mCallback;

    public interface OnIngredientClickedListener {
        void onIngredientClickedListener(int cakeId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mCallback = (OnIngredientClickedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnIngredientClickedListener");
        }
    }

    public RecipeDetailsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recipe_details_fragment, container, false);
        rv = rootView.findViewById(R.id.rv_recipe);
        mIngButton = rootView.findViewById(R.id.btn_ingredients);
        mIngButton.setOnClickListener( view -> mCallback.onIngredientClickedListener(id));

        try{
            adapter = new RecipeAdapter(steps, mClickListener);
            rv.setAdapter(adapter);
        } catch (NullPointerException e){
            throw new ClassCastException(container.toString() + " must implement OnIngredientClickedListener");
        }

        return rootView;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setmClickListener(RecipeActivity mClickListener) {
        this.mClickListener = mClickListener;
    }
}
