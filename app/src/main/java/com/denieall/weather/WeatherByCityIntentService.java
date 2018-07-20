package com.denieall.weather;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class WeatherByCityIntentService extends IntentService {


    public WeatherByCityIntentService() {
        super("WeatherByCityIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getWeather(intent.getStringExtra("city"));
    }

    // Get the weather from the last known position
    private void getWeather(String city_name) {
        Log.i("Weather", Thread.currentThread().getName());

        // Making GET Request from Open Weather Map API
        RequestParams params = new RequestParams();
        params.put("q", city_name);
        params.put("APPID", "a63f3c359948c677f66c7e2e19ea77c3");

        // Get data synchronously --- Use AsyncHttpClient otherwise
        SyncHttpClient client = new SyncHttpClient();

        try {

            client.get("http://api.openweathermap.org/data/2.5/weather", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    Log.i("Request", "" + statusCode);

                    WeatherDataModel wdm = WeatherDataModel.fromJSON(response);

                    Log.i("Weather data", "" + wdm.temperature);
                    Log.i("Weather data", "" + wdm.logo);

                    Intent i = new Intent();
                    i.putExtra("temp",  wdm.temperature);
                    i.putExtra("logo",  wdm.logo);
                    i.setAction(ChangeCityActivity.CITY_OK_CHANNEL);
                    sendBroadcast(i);

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                    Intent i = new Intent();
                    i.setAction(ChangeCityActivity.CITY_ERROR_CHANNEL);
                    sendBroadcast(i);

                }
            });

        } catch (Exception e) {
            Log.i("Error", "An error has occured");
        }

    }
}
