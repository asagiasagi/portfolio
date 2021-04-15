package com.asg.weatherwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.asg.weatherwidget.activity.PermissionActivity;
import com.asg.weatherwidget.activity.WeatherWidgetActivity;
import com.asg.weatherwidget.citydata.CityDataDownloadService;
import com.asg.weatherwidget.weatherdata.WeatherDataManager;

public class WeatherWidgetProvider extends AppWidgetProvider /*BroadcastReceiver*/ {
    private static final String TAG = "WeatherWidgetProvider";

    //----------------------------------------------------------------------------------------------
    // Broadcast Action
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_APPWIDGET_UPDATE_LOCATION = "com.asg.weatherwidget.ACTION_APPWIDGET_UPDATE_LOCATION";
    public static final String ACTION_APPWIDGET_UPDATE_WEATHER = "com.asg.weatherwidget.ACTION_APPWIDGET_UPDATE_WEATHER";
    public static final String ACTION_APPWIDGET_UPDATE_WIDGET = "com.asg.weatherwidget.ACTION_APPWIDGET_UPDATE_WIDGET";
    public static final String ACTION_APPWIDGET_START_ACTIVITY = "com.asg.weatherwidget.ACTION_APPWIDGET_START_ACTIVITY";
    public static final String ACTION_APPWIDGET_DOWNLOAD_CITY_DATA = "com.asg.weatherwidget.ACTION_APPWIDGET_DOWNLOAD_CITY_DATA";

    //----------------------------------------------------------------------------------------------
    // Receiver Method (Public)
    //----------------------------------------------------------------------------------------------

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(TAG, "onReceive:" + action);
        if (action == null) return;

        switch (action) {
            case ACTION_APPWIDGET_UPDATE_LOCATION:
                updateLocation(context, intent);
                break;
            case ACTION_APPWIDGET_UPDATE_WEATHER:
                updateWeather(context, intent);
                break;
            case ACTION_APPWIDGET_UPDATE_WIDGET:
                updateWidget(context, intent);
                break;
            case ACTION_APPWIDGET_START_ACTIVITY:
                startActivity(context, intent);
                break;
            case ACTION_APPWIDGET_DOWNLOAD_CITY_DATA:
                downloadCityData(context, intent);
                break;
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setupWidget(context, appWidgetManager, appWidgetIds);
        if (PermissionActivity.checkGrantedPermission(context)) {
            updateLocation(context, null);
        }
    }

    @Override
    public void onEnabled(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
        setupWidget(context, appWidgetManager, appWidgetIds);
    }

    //----------------------------------------------------------------------------------------------
    // Receiver Method (Private)
    //----------------------------------------------------------------------------------------------

    private void setupWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // それぞれのwidgetに初期設定（タップ時の動作）を設定する
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_weather_widget);

            // タップ時の動作をPendingIntentで登録する。requestCodeはwidgetのIDとする。
            Intent startActivityIntent = new Intent(context, WeatherWidgetProvider.class);
            startActivityIntent.setAction(ACTION_APPWIDGET_START_ACTIVITY);
            PendingIntent startActivityPending = PendingIntent.getBroadcast(context, appWidgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.container_weather_widget, startActivityPending);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }


    private void updateLocation(Context context, Intent intent) {
        Log.d(TAG, "updateLocation");
        LocationManager locationManager = new LocationManager(context);
        locationManager.updateLocation();
    }

    private void updateWeather(Context context, Intent intent) {
        Log.d(TAG, "updateWeather");
        WeatherDataManager weatherDataManager = new WeatherDataManager(context);
        weatherDataManager.updateWeather(intent);
    }

    private void updateWidget(Context context, Intent intent) {
        Log.d(TAG, "updateWidget");
        String date = intent.getStringExtra(WeatherDataManager.EXTRA_WEATHER_DATE_DATA);
        String main = intent.getStringExtra(WeatherDataManager.EXTRA_WEATHER_MAIN_DATA);
        String city = intent.getStringExtra(WeatherDataManager.EXTRA_WEATHER_CITY_DATA);

        byte[] iconData = intent.getByteArrayExtra(WeatherDataManager.EXTRA_WEATHER_ICON_DATA);
        Bitmap icon = BitmapFactory.decodeByteArray(iconData, 0, iconData.length);

        // WidgetManagerを取得
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        // すべてのWidgetの日付／緯度・経度／天気を更新し、タップ時にActivityを起動するようにする。
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WeatherWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            // タップ時の動作をPendingIntentで登録する。requestCodeはwidgetのIDとする。
            Intent startActivityIntent = new Intent(context, WeatherWidgetProvider.class);
            startActivityIntent.setAction(ACTION_APPWIDGET_START_ACTIVITY);
            PendingIntent startActivityPending = PendingIntent.getBroadcast(context, appWidgetId, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Widgetの各Viewに更新内容を反映する
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_weather_widget);
            views.setTextViewText(R.id.textview_date_weather_widget, date);
            views.setTextViewText(R.id.textview_main_weather_widget, main);
            views.setTextViewText(R.id.textview_city_weather_widget, city);
            views.setImageViewBitmap(R.id.imageview_icon_weather_widget, icon);
            views.setOnClickPendingIntent(R.id.container_weather_widget, startActivityPending);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    private void startActivity(Context context, Intent intent) {
        Log.d(TAG, "startActivity");
        context.startActivity(new Intent(context, WeatherWidgetActivity.class));
    }

    private void downloadCityData(Context context, Intent intent) {
        Log.d(TAG, "downloadCityData");
        Intent service = new Intent(context, CityDataDownloadService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(service);
        } else {
            context.startService(service);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Delete Method
    //----------------------------------------------------------------------------------------------

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            deleteWidgetIntent(context, appWidgetId);
        }
    }

    private static void deleteWidgetIntent(Context context, int appWidgetId) {
        Intent containerIntent = new Intent(context, WeatherWidgetProvider.class);
        containerIntent.setAction(ACTION_APPWIDGET_START_ACTIVITY);
        PendingIntent containerPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, containerIntent, PendingIntent.FLAG_NO_CREATE);

        if (containerPendingIntent != null) {
            containerPendingIntent.cancel();
        }
    }
}
