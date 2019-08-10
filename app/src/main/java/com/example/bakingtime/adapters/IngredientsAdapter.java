package com.example.bakingtime.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bakingtime.R;
import com.example.bakingtime.models.Ingredient;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    private List<Ingredient> mIngredients;
    private int mNumberOfItems;

    public IngredientsAdapter(List<Ingredient> mIngredients) {
        this.mIngredients = mIngredients;

        int size = 0;
        if(mIngredients.size() > 0) size = mIngredients.size();
        this.mNumberOfItems = size;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.ingredient_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mIngredients.get(i));
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mIngName;
        private TextView mIngQuantity;
        private TextView mIngMeasure;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIngName = itemView.findViewById(R.id.tv_ingredient_name);
            mIngQuantity = itemView.findViewById(R.id.tv_ingredient_quantity);
            mIngMeasure = itemView.findViewById(R.id.tv_ingredient_measure);
        }

        private void bind(Ingredient ingredient){
            mIngName.setText(ingredient.getIngredient());
            mIngQuantity.setText(String.valueOf(ingredient.getQuantity()));
            mIngMeasure.setText(ingredient.getMeasure());
        }
    }
}
