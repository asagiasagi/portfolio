package com.asg.weatherwidget.citydata;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.asg.weatherwidget.HttpThread;
import com.asg.weatherwidget.R;
import com.asg.weatherwidget.WeatherWidgetProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class CityDataDownloadService extends Service {
    private static final String TAG = "CityDataDownloadService";

    //----------------------------------------------------------------------------------------------
    // Define
    //----------------------------------------------------------------------------------------------
    private static final String NOTIFICATION_TITLE = "Download City Data";
    private static final String NOTIFICATION_CHANNEL_ID = "DownloadCityData";
    private static final String NOTIFICATION_CHANNEL_TITLE = "Download City Data";

    private static final String REQUEST_URL = "http://bulk.openweathermap.org/sample/city.list.json.gz";

    private static final String JSON_VARIABLE_COUNTRY = "country";
    private static final String JSON_VARIABLE_COUNTRY_JP = "JP";
    private static final String JSON_VARIABLE_CITY_ID = "id";
    private static final String JSON_VARIABLE_CITY_NAME = "name";

    //----------------------------------------------------------------------------------------------
    // Field
    //----------------------------------------------------------------------------------------------
    private HttpThread.Callback mCallback = new HttpThread.Callback() {
        @Override
        public void finishDownload(InputStream dlDataInputStream) {
            GZIPInputStream gzipInputStream = null;
            BufferedReader bufferedReader = null;
            try {
                // ストリームへ接続
                gzipInputStream = new GZIPInputStream(dlDataInputStream);
                bufferedReader = new BufferedReader(new InputStreamReader(gzipInputStream));

                JSONObject json;
                while (true) {
                    json = readJSONFromGZIP(bufferedReader);
                    if (json == null) {
                        break;
                    }

                    // json をDatabaseに登録する
                    saveData(json);
                }

            } catch (Exception e) {
                mCallback.notifyError();
                e.printStackTrace();
            }

            // GZIPInputStreamを閉じる
            try {
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "GZIPInputStream close failed");
            }

            // BufferedReaderを閉じる
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (Exception e) {
                Log.w(TAG, "BufferedReader close failed");
            }

        }

        @Override
        public void finishRunning() {
            stopSelf();

            Intent intent = new Intent(CityDataDownloadService.this, WeatherWidgetProvider.class);
            intent.setAction(WeatherWidgetProvider.ACTION_APPWIDGET_UPDATE_LOCATION);
            sendBroadcast(intent);
        }

        @Override
        public void notifyError() {
            stopSelf();
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent.getAction());

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(NOTIFICATION_TITLE)
                    .setSmallIcon(R.drawable.ic_download_city_data)
                    .build();

            startForeground(100, notification);

            HttpThread httpThread = HttpThread.getInstance();
            httpThread.startDownload(REQUEST_URL, mCallback);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private JSONObject readJSONFromGZIP(BufferedReader reader) throws IOException, JSONException {
        JSONObject jsonObject = null;

        // ストリームからデータの読み出し
        StringBuilder builder = new StringBuilder();
        String line;
        while (true) {

            line = reader.readLine();
            if (line != null) {
                if (line.contains("[") || line.contains("]")) {
                    // "[", "]" はスキップする
                } else if (line.contentEquals("    {")) {
                    // JSONデータ読み込み開始
                    builder.delete(0, builder.length());
                    builder.append(line);
                } else if (line.contentEquals("    },")) {
                    // JSONデータ読み込み完了
                    builder.append(line);
                    jsonObject = new JSONObject(builder.toString());
                    break;
                } else {
                    // JSONデータ読み込み中
                    builder.append(line);
                }
            } else {
                break; // 全データの読み込みが終了
            }
        }

        return jsonObject;
    }

    private void saveData(JSONObject json) throws JSONException {
        String country = json.getString(JSON_VARIABLE_COUNTRY);
        if (JSON_VARIABLE_COUNTRY_JP.equals(country)) {
            int id = json.getInt(JSON_VARIABLE_CITY_ID);
            String name = json.getString(JSON_VARIABLE_CITY_NAME);

            CityDataSQLiteOpenHelper helper = new CityDataSQLiteOpenHelper(this);
            SQLiteDatabase database = helper.getWritableDatabase();
            helper.saveData(database, id, name);
            database.close();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Binder
    //----------------------------------------------------------------------------------------------
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
