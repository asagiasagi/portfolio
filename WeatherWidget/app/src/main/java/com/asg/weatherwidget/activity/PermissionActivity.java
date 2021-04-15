package com.asg.weatherwidget.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

abstract public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "WeatherWidgetActivity";

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // 許可済みの場合は true, 未許可の場合は false
    public static boolean checkGrantedPermission(Context context) {
        // GPS
        boolean grantedAccessFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        // ネットワーク
        boolean grantedAccessCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return grantedAccessFineLocation && grantedAccessCoarseLocation;
    }

    protected void requestPermission() {
        requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");

        // 返答なしの場合は、何もしない。
        // 起動時などに呼ばれるケースがあるが、よくわからない。
        if (grantResults.length <= 0) {
            return;
        }

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 許可が出た場合。
                } else {
                    // 未許可の場合。
                    Toast.makeText(this, "許可が欲しいわ。", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
