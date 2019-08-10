package com.example.bakingtime.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bakingtime.R;
import com.example.bakingtime.models.Step;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private int mNumberOfItems;
    private List<Step> mSteps;
    private ListItemClickListener mOnClickListener;

    public RecipeAdapter(List<Step> mSteps, ListItemClickListener mOnClikListener) {
        this.mSteps = mSteps;
        this.mOnClickListener = mOnClikListener;

        int size = 0;
        if(mSteps != null && mSteps.size() > 0) size = mSteps.size();
        mNumberOfItems = size;
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater
                .inflate(R.layout.recipe_step_list_item, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mSteps.get(i));
    }

    @Override
    public int getItemCount() {
        return mNumberOfItems;
    }


    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private Button button;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.bt_recipe_list_item);
            itemView.setOnClickListener(this);
        }

        public void bind(Step step){
            String desc = step.getShortDescription();
            button.setText(desc);
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
