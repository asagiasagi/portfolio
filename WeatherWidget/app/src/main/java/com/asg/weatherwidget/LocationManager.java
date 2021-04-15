package com.asg.weatherwidget;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.asg.weatherwidget.activity.PermissionActivity;

public class LocationManager implements LocationListener {
    private static final String TAG = "LocationManager";

    //----------------------------------------------------------------------------------------------
    // Define
    //----------------------------------------------------------------------------------------------
    public static final String EXTRA_LOCATION_LATITUDE = "EXTRA_LOCATION_LATITUDE";
    public static final String EXTRA_LOCATION_LONGITUDE = "EXTRA_LOCATION_LONGITUDE";

    // 日本経緯度原点
    public static final double DEFAULT_LATITUDE = 35.39291572;     // 緯度：北緯35度39分29秒1572
    public static final double DEFAULT_LONGITUDE = 139.44288869;   // 経度：東経139度44分28秒8869

    private static final long INTERVAL_LOCATION_UPDATE_TIME_MS = 0;
    private static final float INTERVAL_LOCATION_UPDATE_DISTANCE_M = 0;

    //----------------------------------------------------------------------------------------------
    // Field
    //----------------------------------------------------------------------------------------------

    Context mContext;

    //----------------------------------------------------------------------------------------------
    // Method
    //----------------------------------------------------------------------------------------------
    public LocationManager(Context context) {
        mContext = context;
    }

    public void updateLocation() {
        if (PermissionActivity.checkGrantedPermission(mContext)) {
            // LocationManager をシステムから取得
            android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            // 位置情報取得条件を設定
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
            String provider = locationManager.getBestProvider(criteria, true);
            // 位置情報取得要求。取得結果はリスナで処理する。
            if (provider != null) {
                locationManager.requestLocationUpdates(
                        provider,
                        INTERVAL_LOCATION_UPDATE_TIME_MS,
                        INTERVAL_LOCATION_UPDATE_DISTANCE_M,
                        this
                );
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // Location Method (LocationListener)
    //----------------------------------------------------------------------------------------------
    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "onLocationChanged");

        // 緯度・経度を取得
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        // 緯度・経度の取得を通知
        Intent intent = new Intent(mContext, WeatherWidgetProvider.class);
        intent.setAction(WeatherWidgetProvider.ACTION_APPWIDGET_UPDATE_WEATHER);
        intent.putExtra(EXTRA_LOCATION_LATITUDE, latitude);
        intent.putExtra(EXTRA_LOCATION_LONGITUDE, longitude);
        mContext.sendBroadcast(intent);

        // 時間間隔で位置情報を更新すると、バッテリ消費が大きくなるため、
        // widget更新時または、更新ボタンによる更新でのみ位置情報を更新する。
        // (→位置情報の更新が完了したら、位置情報取得のリスナを解除する）
        android.location.LocationManager locationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(this);
    }
}
