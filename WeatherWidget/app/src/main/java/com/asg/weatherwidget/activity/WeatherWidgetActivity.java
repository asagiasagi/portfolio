package com.asg.weatherwidget.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.asg.weatherwidget.R;
import com.asg.weatherwidget.WeatherWidgetProvider;

public class WeatherWidgetActivity extends PermissionActivity implements View.OnClickListener {
    private static final String TAG = "WeatherWidgetActivity";

    protected Button mUpdatePositionButton;   // ボタン

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_widget);

        mUpdatePositionButton = findViewById(R.id.button_update_weather_widget_activity);
        mUpdatePositionButton.setOnClickListener(this);

        if (!checkGrantedPermission(this)) {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkGrantedPermission(this)) {
            requestPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu_weather_widget_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_download_city_id_weather_widget_activity) {
            Intent intent = new Intent(this, WeatherWidgetProvider.class);
            intent.setAction(WeatherWidgetProvider.ACTION_APPWIDGET_DOWNLOAD_CITY_DATA);
            sendBroadcast(intent);
        } else {
            super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        if (checkGrantedPermission(this)) {
            Intent intent = new Intent(this, WeatherWidgetProvider.class);
            intent.setAction(WeatherWidgetProvider.ACTION_APPWIDGET_UPDATE_LOCATION);
            sendBroadcast(intent);
        } else {
            requestPermission();
        }
    }
}