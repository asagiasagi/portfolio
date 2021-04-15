package com.asg.weatherwidget.weatherdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.asg.weatherwidget.LocationManager;
import com.asg.weatherwidget.WeatherWidgetProvider;
import com.asg.weatherwidget.citydata.CityDataSQLiteOpenHelper;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class WeatherDataManager extends BroadcastReceiver {
    private static final String TAG = "WeatherDataManager";

    //----------------------------------------------------------------------------------------------
    // Define
    //----------------------------------------------------------------------------------------------
    public static final String EXTRA_WEATHER_MAIN = "EXTRA_WEATHER_MAIN";
    public static final String EXTRA_WEATHER_ICON_ID = "EXTRA_WEATHER_ICON_ID";
    public static final String EXTRA_WEATHER_ICON = "EXTRA_WEATHER_ICON";
    public static final String EXTRA_WEATHER_LATITUDE = "EXTRA_WEATHER_LATITUDE";
    public static final String EXTRA_WEATHER_LONGITUDE = "EXTRA_WEATHER_LONGITUDE";
    public static final String EXTRA_WEATHER_CITY_ID = "EXTRA_WEATHER_CITY_ID";

    public static final String EXTRA_WEATHER_DATE_DATA = "EXTRA_WEATHER_DATE_DATA";
    public static final String EXTRA_WEATHER_MAIN_DATA = "EXTRA_WEATHER_MAIN_DATA";
    public static final String EXTRA_WEATHER_CITY_DATA = "EXTRA_WEATHER_CITY_DATA";
    public static final String EXTRA_WEATHER_ICON_DATA = "EXTRA_WEATHER_ICON_DATA";
    //----------------------------------------------------------------------------------------------
    // Field
    //----------------------------------------------------------------------------------------------
    private WeatherDataDownloader mWeatherDataDownloader;

    private String mWeatherDataDate;
    private String mWeatherDataMain;
    private String mWeatherDataCity;
    private byte[] mWeatherDataIcon;


    //----------------------------------------------------------------------------------------------
    // Constructor
    //----------------------------------------------------------------------------------------------
    public WeatherDataManager(Context context) {
        mWeatherDataDownloader = new WeatherDataDownloader(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FINISH_DOWNLOAD_JSON);
        intentFilter.addAction(ACTION_FINISH_DOWNLOAD_ICON);

        context.getApplicationContext().registerReceiver(this, intentFilter);
    }

    //----------------------------------------------------------------------------------------------
    // Receiver
    //----------------------------------------------------------------------------------------------
    public static final String ACTION_FINISH_DOWNLOAD_JSON = "com.asg.weatherwidget.weatherdata.ACTION_WEATHER_DATA_MANAGER_FINISH_DOWNLOAD_JSON";
    public static final String ACTION_FINISH_DOWNLOAD_ICON = "com.asg.weatherwidget.weatherdata.ACTION_WEATHER_DATA_MANAGER_FINISH_DOWNLOAD_ICON";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(TAG, "onReceive:" + action);
        if (action == null) return;

        switch (action) {
            case ACTION_FINISH_DOWNLOAD_JSON:
                createSendDataAndDownloadIcon(context, intent);
                break;
            case ACTION_FINISH_DOWNLOAD_ICON:
                createSendDataAndSendData(context, intent);

                context.getApplicationContext().unregisterReceiver(this);
                break;
            default:
                break;
        }
    }

    public void updateWeather(Intent intent) {
        double latitude = intent.getDoubleExtra(LocationManager.EXTRA_LOCATION_LATITUDE, 0);
        double longitude = intent.getDoubleExtra(LocationManager.EXTRA_LOCATION_LONGITUDE, 0);

        // 天気情報(JSON)ダウンロード
        mWeatherDataDownloader.downloadWeatherData(latitude, longitude);
    }

    private void createSendDataAndDownloadIcon(Context context, Intent intent) {
        mWeatherDataDate = getDateData();                       // 日付
        mWeatherDataCity = getCityData(context, intent);        // 都市名
        mWeatherDataMain = getMainData(intent);                 // 天気

        // icon ダウンロード
        downloadIcon(intent);
    }

    private void downloadIcon(Intent intent) {
        String icon = intent.getStringExtra(EXTRA_WEATHER_ICON_ID);
        mWeatherDataDownloader.downloadIconData(icon);
    }

    private void createSendDataAndSendData(Context context, Intent intent) {
        mWeatherDataIcon = getIconData(intent);

        sendData(context);
    }

    private void sendData(Context context) {
        if (mWeatherDataDate == null
                || mWeatherDataMain == null
                || mWeatherDataCity == null
                || mWeatherDataIcon == null) {

            Log.d(TAG, "illegal weather data. update failed."
                    + ", date=" + mWeatherDataDate
                    + ", main=" + mWeatherDataMain
                    + ", city=" + mWeatherDataCity
                    + ", icon=" + mWeatherDataIcon
            );

            return;
        }

        Intent intent = new Intent(context, WeatherWidgetProvider.class);
        intent.setAction(WeatherWidgetProvider.ACTION_APPWIDGET_UPDATE_WIDGET);
        intent.putExtra(EXTRA_WEATHER_DATE_DATA, mWeatherDataDate);
        intent.putExtra(EXTRA_WEATHER_MAIN_DATA, mWeatherDataMain);
        intent.putExtra(EXTRA_WEATHER_CITY_DATA, mWeatherDataCity);
        intent.putExtra(EXTRA_WEATHER_ICON_DATA, mWeatherDataIcon);

        context.sendBroadcast(intent);
    }

    private String getDateData() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm", Locale.getDefault());
        return dateFormat.format(date);
    }

    private String getCityData(Context context, Intent intent) {
        int cityId = intent.getIntExtra(EXTRA_WEATHER_CITY_ID, -1);

        CityDataSQLiteOpenHelper cityDataSQLiteOpenHelper = new CityDataSQLiteOpenHelper(context);
        SQLiteDatabase cityDataSqLiteDatabase = cityDataSQLiteOpenHelper.getReadableDatabase();
        String cityName = cityDataSQLiteOpenHelper.readData(cityDataSqLiteDatabase, cityId);

        if (cityName == null) {
            double latitude = intent.getDoubleExtra(EXTRA_WEATHER_LATITUDE, LocationManager.DEFAULT_LATITUDE);
            double longitude = intent.getDoubleExtra(EXTRA_WEATHER_LONGITUDE, LocationManager.DEFAULT_LONGITUDE);
            cityName = String.format(Locale.getDefault(), "%.2f/%.2f", latitude, longitude);
        }

        return cityName;
    }

    private String getMainData(Intent intent) {
        return intent.getStringExtra(EXTRA_WEATHER_MAIN);
    }

    private byte[] getIconData(Intent intent) {
        return intent.getByteArrayExtra(EXTRA_WEATHER_ICON);
    }
}
