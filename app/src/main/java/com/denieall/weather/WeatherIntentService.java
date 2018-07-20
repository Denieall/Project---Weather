package com.denieall.weather;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class WeatherIntentService extends IntentService {

    public WeatherIntentService() {
        super("WeatherIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        getWeather(intent.getDoubleExtra("lat", 0), intent.getDoubleExtra("long", 0));

    }

    // Get the weather from the last known position
    private void getWeather(double latitude, double longitude) {
        Log.i("Weather", Thread.currentThread().getName());

        // Making GET Request from Open Weather Map API
        RequestParams params = new RequestParams();
        params.put("lat", latitude);
        params.put("lon", longitude);
        params.put("APPID", "a63f3c359948c677f66c7e2e19ea77c3");

        // Get data synchronously --- Use AsyncHttpClient otherwise
        SyncHttpClient client = new SyncHttpClient();
        client.get("http://api.openweathermap.org/data/2.5/weather", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.i("Request", Thread.currentThread().getName());

                WeatherDataModel wdm = WeatherDataModel.fromJSON(response);

                Log.i("Json", Thread.currentThread().getName());

                Log.i("Weather data", "" + wdm.temperature);
                Log.i("Weather data", "" + wdm.logo);

                Intent i = new Intent();
                i.putExtra("temp",  wdm.temperature);
                i.putExtra("logo",  wdm.logo);
                i.setAction(MainActivity.WEATHER_RECEIVER_CHANNEL);
                sendBroadcast(i);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(getApplicationContext(), "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
