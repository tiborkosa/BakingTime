package com.example.bakingtime.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.bakingtime.R;
import com.example.bakingtime.database.AppDatabase;
import com.example.bakingtime.models.Ingredient;

import java.util.List;

import static com.example.bakingtime.widgets.BakeTimeProvider.WIDGET_CAKE_ID;
import static com.example.bakingtime.utils.UtilMethods.removeZero;


public class ListWidgetIngService extends RemoteViewsService {

    private final static String TAG = ListWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int id = intent.getIntExtra(WIDGET_CAKE_ID,0);
        Log.d(TAG, "Received the id: " + id);
        return new WidgetGetIngViewFactory(this.getApplicationContext() , id);
    }
}

class WidgetGetIngViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private final static String TAG = WidgetGetIngViewFactory.class.getSimpleName();
    private Context mContext;
    private List<Ingredient> ingredients;
    private static int mCakeId;

    AppDatabase db;

    public WidgetGetIngViewFactory(Context applicationContext, int cakeId) {
        mContext = applicationContext;
        this.mCakeId = cakeId;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (ingredients != null) {
            closeDb();
        }

        db = AppDatabase.getInstance();
        final long identityToken = Binder.clearCallingIdentity();
        if(mCakeId > 0)
            ingredients = db.ingredientDao().loadAllById(mCakeId);

        Binder.restoreCallingIdentity(identityToken);

    }

    @Override
    public void onDestroy() {
        closeDb();
        ingredients = null;
    }

    @Override
    public int getCount() {
        return ingredients == null ? 0 : ingredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                ingredients == null || ingredients.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ingredients.get(position).getIngredient());
        sb.append(" - ");
        sb.append(removeZero(ingredients.get(position).getQuantity()));
        sb.append(" ");
        sb.append(ingredients.get(position).getMeasure());
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
        rv.setTextViewText(R.id.widget_text_item, sb.toString());

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return ingredients.size() < position ? 0 : position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void closeDb(){
        if( db != null) db.close();
        db = null;
    }

}
