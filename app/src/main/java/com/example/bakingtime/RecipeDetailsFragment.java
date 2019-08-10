package com.example.bakingtime;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bakingtime.adapters.MainAdapter;
import com.example.bakingtime.adapters.RecipeAdapter;
import com.example.bakingtime.models.Step;

import java.util.ArrayList;
import java.util.List;

public class RecipeDetailsFragment extends Fragment {

    private static final String TAG = RecipeActivity.class.getSimpleName();
    public static final String STEPS = "recipe_steps";
    public static final String RECIPE_ID = "recipe_id";

    private RecyclerView rv;
    private RecipeAdapter adapter;
    private Button mIngButton;
    private static int id;
    private static List<Step> steps;
    private OnIngredientClickedListener mCallback;
    RecipeAdapter.ListItemClickListener mNextStepCallback;

    public RecipeDetailsFragment() {}

    public static RecipeDetailsFragment newInstance(List<Step> steps, int id) {
        RecipeDetailsFragment fragment = new RecipeDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(STEPS, (ArrayList<? extends Parcelable>) steps);
        args.putInt(RECIPE_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            steps = getArguments().getParcelableArrayList(STEPS);
            id = getArguments().getInt(RECIPE_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIngredientClickedListener) {
            mCallback = (OnIngredientClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIngredientClickedListener");
        }
        if(context instanceof RecipeAdapter.ListItemClickListener) {
            mNextStepCallback = (RecipeAdapter.ListItemClickListener) context;
        }else {
            throw new RuntimeException(context.toString()
                    + " must implement RecipeAdapter.ListItemClickListener!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recipe_details_fragment, container, false);
        rv = rootView.findViewById(R.id.rv_recipe);
        mIngButton = rootView.findViewById(R.id.btn_ingredients);
        mIngButton.setOnClickListener( view -> mCallback.onIngredientClickedListener(id));

        adapter = new RecipeAdapter(steps, mNextStepCallback);
        rv.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public interface OnIngredientClickedListener {
        void onIngredientClickedListener(int cakeId);
    }
}
