package com.example.bakingtime.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.bakingtime.R;

/**
 * Implementation of App Widget functionality.
 */
public class BakeTimeProvider extends AppWidgetProvider {

    public static final String WIDGET_CAKE_ID = "cake_id";

    public static final String ACTION_BACK = "com.example.android.bakeTime.action.go_back";
    public static final String ACTION_LIST_INGREDIENTS = "com.example.android.bakeTime.action.ingredients";
    private static final String TAG = BakeTimeProvider.class.getSimpleName();
    public static final String RECIPES = "recipes";
    public static final String INGREDIENTS = "ingredients";
    private static String currentAction = "recipes";

    /**
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     */
    private static void getBakingTimeRecipesRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "[getBakingTimeRecipesRemoteView]");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_recipe_view);

        Intent intent = new Intent(context, ListWidgetService.class);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        views.setEmptyView(R.id.widget_list_view, R.id.appwidget_text);
        // hide button
       views.setViewVisibility(R.id.widget_list_view, View.INVISIBLE);
       views.setTextViewText(R.id.widget_title, "Recipes");

        // setting up the click event
        Intent ingredientsIntent = new Intent(context, BakeTimeProvider.class);
        ingredientsIntent.setAction(ACTION_LIST_INGREDIENTS);
        ingredientsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent ingredientsPendingIntent = PendingIntent.getBroadcast(context, 0, ingredientsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, ingredientsPendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param cakeId
     */
    private static void getBakingTimeIngredientsRemoteView(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int cakeId) {
        Log.d(TAG, "[getBakingTimeIngredientsRemoteView]");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_recipe_view);

        Intent intent = new Intent(context, ListWidgetIngService.class);
        intent.putExtra(WIDGET_CAKE_ID, cakeId);
        views.setRemoteAdapter(R.id.widget_list_view, intent);
        views.setEmptyView(R.id.widget_list_view, R.id.appwidget_text);
        views.setTextViewText(R.id.widget_title, "Ingredients");
        views.setViewVisibility(R.id.widget_btn_back, View.VISIBLE);

        Intent backIntent = new Intent(context, BakeTimeProvider.class);
        backIntent.setAction(ACTION_BACK);
        backIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, backIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_btn_back, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     * @param cakeId
     */
    static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, int cakeId){

        for (int appWidgetId : appWidgetIds) {
            if(currentAction.equals(RECIPES))
                getBakingTimeRecipesRemoteView(context, appWidgetManager, appWidgetId);
            else
                getBakingTimeIngredientsRemoteView(context, appWidgetManager, appWidgetId, cakeId);
        }
    }

    /**
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG,"[onReceive] "+action);
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(context, BakeTimeProvider.class));

        if (action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE) || action.equals(ACTION_BACK)) {
            currentAction = RECIPES;
            updateAppWidgets(context,mgr,ids, 0);
        }else if (intent.getAction().equals(ACTION_LIST_INGREDIENTS)) {
            currentAction = INGREDIENTS;
            int cakeId = intent.getIntExtra(WIDGET_CAKE_ID, 0);
            updateAppWidgets(context,mgr,ids, cakeId);
        }
        super.onReceive(context, intent);
    }

    /**
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "[onUpdate]");
        updateAppWidgets(context,appWidgetManager, appWidgetIds, 0);
    }

    /**
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, "[onAppWidgetOptionsChanged]");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     *
     * @param context
     * @param appWidgetIds
     */
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "[onDeleted]");
        super.onDeleted(context, appWidgetIds);
    }

    /**
     *
     * @param context
     */
    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "[onEnabled]");
        super.onEnabled(context);
    }

    /**
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, "[onDisabled]");
        super.onDisabled(context);
    }

    /**
     *
     * @param context
     * @param oldWidgetIds
     * @param newWidgetIds
     */
    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Log.d(TAG, "[onRestored]");
        super.onRestored(context, oldWidgetIds, newWidgetIds);
    }

}

