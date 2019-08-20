package com.example.bakingtime.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.bakingtime.R;
import com.example.bakingtime.database.AppDatabase;
import com.example.bakingtime.models.Cake;

import java.util.List;

import static com.example.bakingtime.widgets.BakeTimeProvider.WIDGET_CAKE_ID;

/**
 * Cake list widget service class
 */
public class ListWidgetService extends RemoteViewsService {

    private final static String TAG = ListWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetGetViewFactory(this.getApplicationContext());
    }
}

/**
 * Widget factory class
 */
class WidgetGetViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private final static String TAG = WidgetGetViewFactory.class.getSimpleName();
    private Context mContext;
    private List<Cake> cakes;

    AppDatabase db;


    /**
     *
     * @param applicationContext
     */
    public WidgetGetViewFactory(Context applicationContext) {
        mContext = applicationContext;
    }

    /**
     *
     */
    @Override
    public void onCreate() { }

    /**
     *
     */
    @Override
    public void onDataSetChanged() {

        if (cakes != null) {
            closeDb();
        }

        db = AppDatabase.getInstance();
        final long identityToken = Binder.clearCallingIdentity();
        cakes = db.cakeDao().getAll();

        Binder.restoreCallingIdentity(identityToken);

    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        closeDb();
        cakes = null;
    }

    /**
     *
     * @return
     */
    @Override
    public int getCount() {
        return cakes == null ? 0 : cakes.size();
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                cakes == null || cakes.size() == 0) {
            return null;
        }

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);
        rv.setTextViewText(R.id.widget_text_item, cakes.get(position).getName());

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(WIDGET_CAKE_ID, cakes.get(position).getId());
        rv.setOnClickFillInIntent(R.id.widget_text_item, fillInIntent);

        return rv;
    }

    /**
     *
     * @return
     */
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return cakes.size() < position ? 0 : position;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     *
     */
    private void closeDb(){
        if( db != null) db.close();
        db = null;
    }

}
