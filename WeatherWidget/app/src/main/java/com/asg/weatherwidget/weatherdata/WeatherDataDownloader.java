package com.asg.weatherwidget.weatherdata;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.asg.weatherwidget.HttpThread;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WeatherDataDownloader {
    private static final String TAG = "WeatherDataDownloader";

    private Context mContext;

    public WeatherDataDownloader(Context context) {
        mContext = context;
    }

    //----------------------------------------------------------------------------------------------
    // Common
    //----------------------------------------------------------------------------------------------
    private static final long RETRY_TIMEOUT_MS = 1000;
    private static final long RETRY_DELAY_MS = 200;

    private void downloadData(final String requestURL, final HttpThread.Callback callback, final long currentTimeMillis) {

        long timeout = System.currentTimeMillis() - currentTimeMillis;
        if (timeout > RETRY_TIMEOUT_MS) {
            Log.d(TAG, "timeout download data!");
            return;
        }

        HttpThread httpThread = HttpThread.getInstance();

        boolean isStared = httpThread.startDownload(requestURL, callback);
        if (!isStared) {
            Log.d(TAG, "set retry download data.");
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    downloadData(requestURL, callback, currentTimeMillis);
                }
            };

            handler.postDelayed(runnable, RETRY_DELAY_MS);
        }
    }

    //----------------------------------------------------------------------------------------------
    // JSON Data (Main Data)
    //----------------------------------------------------------------------------------------------
    private static final String OPEN_WEATHER_MAP_PREFIX = "http://api.openweathermap.org/data/2.5/weather?";
    private static final String OPEN_WEATHER_MAP_LATITUDE_PREFIX = "&lat=";
    private static final String OPEN_WEATHER_MAP_LONGITUDE_PREFIX = "&lon=";
    private static final String OPEN_WEATHER_MAP_API_KEY_PREFIX = "&appid=";
    // ↓公開するため、削除。OpenWeatherMapより頂戴したAPIキーを入れる。
    private static final String OPEN_WEATHER_MAP_API_KEY = "ここにAPIキーを入れる";

    public static final String JSON_ARRAY_OBJECT_WEATHER = "weather";
    public static final String JSON_VARIABLE_WEATHER_MAIN = "main";
    public static final String JSON_VARIABLE_WEATHER_ICON = "icon";
    public static final String JSON_OBJECT_COORDINATE = "coord";
    public static final String JSON_VARIABLE_COORDINATE_LATITUDE = "lat";
    public static final String JSON_VARIABLE_COORDINATE_LONGITUDE = "lon";
    public static final String JSON_VARIABLE_CITY_ID = "id";

    private HttpThread.Callback mWeatherDataCallback = new HttpThread.Callback() {
        private JSONObject weatherData;

        @Override
        public void finishDownload(InputStream dlDataInputStream) {
            BufferedReader bufferedReader = null;
            try {
                // 取得したデータをJSONに変換
                bufferedReader = new BufferedReader(new InputStreamReader(dlDataInputStream));
                weatherData = createJSON(bufferedReader);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // BufferedReaderを閉じる
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    Log.w(TAG, "BufferedReader close failed");
                }
            }
        }

        @Override
        public void finishRunning() {
            // JSONの天気データを送信する
            try {
                sendWeatherData(weatherData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void notifyError() {

        }
    };

    void downloadWeatherData(double latitude, double longitude) {
        final String requestURL =
                OPEN_WEATHER_MAP_PREFIX
                        + OPEN_WEATHER_MAP_LATITUDE_PREFIX + latitude
                        + OPEN_WEATHER_MAP_LONGITUDE_PREFIX + longitude
                        + OPEN_WEATHER_MAP_API_KEY_PREFIX + OPEN_WEATHER_MAP_API_KEY;

        downloadData(requestURL, mWeatherDataCallback, System.currentTimeMillis());
    }

    private JSONObject createJSON(BufferedReader reader) throws IOException, JSONException {
        StringBuilder builder = new StringBuilder();
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) {
                break;
            }
            builder.append(line);
        }

        return new JSONObject(builder.toString());
    }

    private void sendWeatherData(JSONObject root) throws JSONException {
        Log.d(TAG, "sendWeatherData");

        // 天気情報を保持している場合のみ更新を行う。
        JSONArray weathers = root.getJSONArray(JSON_ARRAY_OBJECT_WEATHER);
        if (weathers.length() > 0) {

            // JSONから天気情報を取得
            JSONObject weather = weathers.getJSONObject(0);
            String main = weather.getString(JSON_VARIABLE_WEATHER_MAIN);
            String icon = weather.getString(JSON_VARIABLE_WEATHER_ICON);

            // JSONから現在の位置情報を取得する
            JSONObject coordinate = root.getJSONObject(JSON_OBJECT_COORDINATE);
            double latitude = coordinate.getDouble(JSON_VARIABLE_COORDINATE_LATITUDE);
            double longitude = coordinate.getDouble(JSON_VARIABLE_COORDINATE_LONGITUDE);

            // JSONから現在の都市IDを取得する
            int cityId = root.getInt(JSON_VARIABLE_CITY_ID);

            // 都市IDと天気情報をwidgetに通知する
            Intent intent = new Intent();
            intent.setAction(WeatherDataManager.ACTION_FINISH_DOWNLOAD_JSON);
            intent.putExtra(WeatherDataManager.EXTRA_WEATHER_MAIN, main);
            intent.putExtra(WeatherDataManager.EXTRA_WEATHER_ICON_ID, icon);
            intent.putExtra(WeatherDataManager.EXTRA_WEATHER_LATITUDE, latitude);
            intent.putExtra(WeatherDataManager.EXTRA_WEATHER_LONGITUDE, longitude);
            intent.putExtra(WeatherDataManager.EXTRA_WEATHER_CITY_ID, cityId);

            mContext.sendBroadcast(intent);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Icon Data
    //----------------------------------------------------------------------------------------------
    private static final String REQUEST_URL_ICON_PREFIX = "http://openweathermap.org/img/wn/";
    private static final String REQUEST_URL_ICON_SUFFIX = "@2x.png";

    private HttpThread.Callback mIconDataCallback = new HttpThread.Callback() {
        byte[] iconData;

        @Override
        public void finishDownload(InputStream dlDataInputStream) {

            try {
                iconData = createIconData(dlDataInputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void finishRunning() {
            sendIconData(iconData);
        }

        @Override
        public void notifyError() {

        }
    };

    void downloadIconData(String icon) {
        final String requestURL = REQUEST_URL_ICON_PREFIX + icon + REQUEST_URL_ICON_SUFFIX;

        downloadData(requestURL, mIconDataCallback, System.currentTimeMillis());
    }

    private byte[] createIconData(InputStream dlDataInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        while (true) {
            int len = dlDataInputStream.read(buffer);
            if (len < 0) {
                break;
            }
            byteArrayOutputStream.write(buffer, 0, len);
        }

        byte[] iconData = byteArrayOutputStream.toByteArray();

        // ByteArrayOutputStreamを閉じる
        byteArrayOutputStream.close();

        return iconData;
    }

    private void sendIconData(byte[] iconData) {
        Log.d(TAG, "sendIconData");

        // 都市IDと天気情報をwidgetに通知する
        Intent intent = new Intent();
        intent.setAction(WeatherDataManager.ACTION_FINISH_DOWNLOAD_ICON);
        intent.putExtra(WeatherDataManager.EXTRA_WEATHER_ICON, iconData);

        mContext.sendBroadcast(intent);
    }

}
