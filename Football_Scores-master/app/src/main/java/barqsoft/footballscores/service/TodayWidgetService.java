package barqsoft.footballscores.service;
/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

        import android.annotation.TargetApi;
        import android.app.IntentService;
        import android.app.PendingIntent;
        import android.appwidget.AppWidgetManager;
        import android.content.ComponentName;
        import android.content.Intent;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Build;
        import android.util.Log;
        import android.widget.RemoteViews;
        import java.sql.Date;
        import java.text.SimpleDateFormat;

        import barqsoft.footballscores.DatabaseContract;
        import barqsoft.footballscores.MainActivity;
        import barqsoft.footballscores.R;
        import barqsoft.footballscores.TodayWidgetProvider;
        import barqsoft.footballscores.Utilies;

/**
 * IntentService which handles updating all Today widgets with the latest data
 */
public class TodayWidgetService extends IntentService {

    final static String LOG_TAG = TodayWidgetService.class.getSimpleName();

    private static final String[] FOOTBALL_COLUMNS = {
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    // these indices must match the projection
    private static final int INDEX_MATCH_ID = 0;
    private static final int INDEX_HOME_COL = 1;
    private static final int INDEX_AWAY_COL = 2;
    private static final int INDEX_HOME_GOALS_COL = 3;
    private static final int INDEX_AWAY_GOALS_COL = 4;

    public TodayWidgetService() {
        super("TodayWidgetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        // Getting today's date as a string
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String today = df.format(new Date(System.currentTimeMillis()));

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                TodayWidgetProvider.class));
        Uri scoresForDateUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(scoresForDateUri, FOOTBALL_COLUMNS, null, new String[]{today}, null);

        if (data == null){
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String match_id = data.getString(INDEX_MATCH_ID);
        String home_name = data.getString(INDEX_HOME_COL);
        String away_name = data.getString(INDEX_AWAY_COL);
        int home_score = data.getInt(INDEX_HOME_GOALS_COL);
        int away_score = data.getInt(INDEX_AWAY_GOALS_COL);
        //score for content descrip...
        String score = "Score" + home_score + "-" + away_score;
        data.close();


        Log.e(LOG_TAG,"todays match:" + match_id);

    for (int appWidgetId : appWidgetIds) {

        int layoutId = R.layout.widget_today;
        RemoteViews views = new RemoteViews(getPackageName(), layoutId);

        views.setTextViewText(R.id.home_name, home_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views,R.id.home_name, home_name);
        }

        views.setTextViewText(R.id.away_name, away_name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views,R.id.away_name, away_name);
        }

        views.setTextViewText(R.id.score, Utilies.getScores(home_score, away_score));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views,R.id.score, score);
        }

        views.setImageViewResource(R.id.home_crest, Utilies.getTeamCrestByTeamName(home_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views,R.id.home_crest, home_name);
        }

        views.setImageViewResource(R.id.away_crest, Utilies.getTeamCrestByTeamName(away_name));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            setRemoteContentDescription(views,R.id.away_crest, away_name);
        }

        Intent launchIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.score, pendingIntent);

      //finally update widget...
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views,int viewID, String description) {
        views.setContentDescription(viewID, description);
    }
}