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

import com.example.bakingtime.adapters.RecipeAdapter;
import com.example.bakingtime.models.Step;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeDetailsFragment extends Fragment {

    private static final String TAG = RecipeActivity.class.getSimpleName();
    public static final String STEPS = "recipe_steps";
    public static final String RECIPE_ID = "recipe_id";

    @BindView(R.id.rv_recipe) RecyclerView rv;
    private RecipeAdapter adapter;
    @BindView(R.id.btn_ingredients) Button mIngButton;
    private static List<Step> steps;
    private OnIngredientClickedListener mCallback;
    RecipeAdapter.ListItemClickListener mNextStepCallback;

    // Required empty public constructor
    public RecipeDetailsFragment() {}

    /**
     * Setting up the fragment instance
     * @param steps of the cake
     * @param id of the cake
     * @return fragment
     */
    public static RecipeDetailsFragment newInstance(List<Step> steps, int id) {
        RecipeDetailsFragment fragment = new RecipeDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(STEPS, (ArrayList<? extends Parcelable>) steps);
        args.putInt(RECIPE_ID, id);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Create method
     * @param savedInstanceState of the fragment
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            steps = getArguments().getParcelableArrayList(STEPS);
        }
    }

    /**
     * Attach method that will check if interface is implemented
     * @param context
     * Throws runtime exception if interface is not implemented
     */
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

    /**
     * Creating the view and setting up the fields
     * @param inflater layout inflater
     * @param container container we are loading the fragment into
     * @param savedInstanceState saved data
     * @return view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recipe_details_fragment, container, false);
        ButterKnife.bind(this, rootView);

        mIngButton.setOnClickListener( view -> mCallback.onIngredientClickedListener());
        adapter = new RecipeAdapter(steps, mNextStepCallback);
        rv.setAdapter(adapter);

        return rootView;
    }

    /**
     * Cleaning up the callback
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    /**
     * Interface the will be implemented in the @RecipeActivity
     */
    public interface OnIngredientClickedListener {
        void onIngredientClickedListener();
    }
}
