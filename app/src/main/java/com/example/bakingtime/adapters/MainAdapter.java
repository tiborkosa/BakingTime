package com.example.bakingtime.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.bakingtime.BakingTime;
import com.example.bakingtime.R;
import com.example.bakingtime.models.Cake;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder>  {

    private static final String TAG = MainAdapter.class.getSimpleName();
    private static int mFoodNumberItems;
    private static List<Cake> mCakes;
    private final ListItemClickLister mOnClickedLister;
    private final Context context = BakingTime.getAppContext();

    public interface ListItemClickLister {
        void onListItemClick(int clickedItemIndex);
    }

    public MainAdapter(List<Cake> cakes, ListItemClickLister mOnClickedLister){
        this.mCakes = cakes;

        int size = 0;
        if(cakes.size() >0) size = cakes.size();
        this.mFoodNumberItems = size;

        this.mOnClickedLister = mOnClickedLister;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.main_grid_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mCakes.get(i));
    }

    @Override
    public int getItemCount() {
        return mFoodNumberItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ConstraintLayout constraintLayout;
        private TextView cakeName;
        private TextView serving;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.ll_grid_item);
            cakeName = itemView.findViewById(R.id.tv_cake_name);
            serving = itemView.findViewById(R.id.tv_serving);

            itemView.setOnClickListener(this);
        }

        public void bind(Cake cake){
            cakeName.setText(cake.getName());
            serving.setText(String.valueOf(cake.getServings()));

            if(cake.getImage() != null || !cake.getImage().isEmpty()){
                constraintLayout.setBackgroundResource(R.drawable.img_not_available);
            } else {
                Picasso.get().load(cake.getImage()).into(new Target(){

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        constraintLayout.setBackgroundDrawable(new BitmapDrawable(context.getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");
                    }
                });
               /* Picasso
                        .get()
                        .load(cake.getImage())
                        .placeholder(R.drawable.img_not_available)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                                Log.e(TAG,"error ");
                            }
                        });*/
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnClickedLister.onListItemClick(position);
        }
    }
}
