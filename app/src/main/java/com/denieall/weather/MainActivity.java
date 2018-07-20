package com.denieall.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.Format;

public class MainActivity extends AppCompatActivity {

    TextView city_tv, temp_tv;
    ImageView weather_symbol;

    String city_name, temp = null;
    int logo;

    // Make onResume don't run on the first time app launch
    boolean startup = true;

    // Broadcast receiver
    public static final String RECEIVER_CHANNEL = "com.weather.location_receiver_1";
    public static final String CITY_RECEIVER_CHANNEL = "com.weather.location_receiver_city";
    public static final String WEATHER_RECEIVER_CHANNEL = "com.weather.weather_data_receiver";

    // For getting the latitude and longitude from the foreground service
    BroadcastReceiver location_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double[] latlong = intent.getDoubleArrayExtra("location");

            if (latlong != null) {

                Log.i("lat", "" + latlong[0]);
                Log.i("lat", "" + latlong[1]);

                // Intent to get city name
                Intent i = new Intent(getApplicationContext(), CityNameIntentService.class);
                i.putExtra("lat", latlong[0]);
                i.putExtra("long", latlong[1]);
                startService(i);

                // Intent to get weather
                Intent i2 = new Intent(getApplicationContext(), WeatherIntentService.class);
                i2.putExtra("lat", latlong[0]);
                i2.putExtra("long", latlong[1]);
                startService(i2);

            }
        }
    };

    // For getting the city name from the generated latitude and longitude
    BroadcastReceiver city_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            city_name = intent.getStringExtra("City");

            city_tv.setText(city_name);
        }
    };

    // Broadcast receiver for weather data
    BroadcastReceiver weather_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            temp = Double.toString(intent.getDoubleExtra("temp", 0)) + "°";
            logo = intent.getIntExtra("logo", 0);

            temp_tv.setText(temp);

            Glide.with(getApplicationContext()).load(logo).into(weather_symbol);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city_tv = findViewById(R.id.locationTV);
        temp_tv = findViewById(R.id.tempTV);
        weather_symbol = findViewById(R.id.weatherSymbol);

        IntentFilter location_receiver_filter = new IntentFilter();
        location_receiver_filter.addAction(RECEIVER_CHANNEL);
        registerReceiver(location_receiver, location_receiver_filter);

        IntentFilter city_receiver_filter = new IntentFilter();
        city_receiver_filter.addAction(CITY_RECEIVER_CHANNEL);
        registerReceiver(city_receiver, city_receiver_filter);

        IntentFilter weather_intent_filter = new IntentFilter();
        weather_intent_filter.addAction(WEATHER_RECEIVER_CHANNEL);
        registerReceiver(weather_receiver, weather_intent_filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent i = getIntent();
        city_name = i.getStringExtra("city");
        temp = Double.toString(i.getDoubleExtra("temp", 0)) + "°";
        logo = i.getIntExtra("logo", 0);

        if (city_name == null) {

            runWeatherService();

        } else {

            city_tv.setText(city_name);
            temp_tv.setText(temp);
            weather_symbol.setImageResource(logo);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i("receiver", "" + location_receiver.getDebugUnregister());

        try {
            unregisterReceiver(location_receiver);
            unregisterReceiver(city_receiver);
            unregisterReceiver(weather_receiver);

        } catch (Exception e) {
            Log.i("Error", e.getMessage());
        }
    }

    // Sharing to all social clients
    public void share_clicked(View view) {
        String shareBody = "The temperature at " + city_name + " is currently " + temp;
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Current temperature");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share via ..."));
    }

    // Start location service
    private void runWeatherService() {
        // Ask foreground service permission for android 8.0 and above
        if (Build.VERSION.SDK_INT >= 28) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, 555);

            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Asking permission from user to use location service --- the pop up Allow or Deny
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 444);
        }

        Intent i = new Intent(this, LocationForegroundService.class);
        i.setAction("START FOREGROUND");
        startService(i);
    }

    public void settings_clicked(View view) {
        Intent i = new Intent(this, ChangeCityActivity.class);
        startActivity(i);
    }

    // Checking to see if the user allow or deny the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 444) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Location Permission: ", "Granted");

                runWeatherService();

            } else{
                Log.d("Location Permission: ", "Denied");

                // Creating alert dialog
                AlertDialog.Builder alert_dialog = new AlertDialog.Builder(this);
                alert_dialog.setTitle("Permissions");
                alert_dialog.setMessage("All the permissions are needed. The app will now be closed");
                alert_dialog.setCancelable(false);

                alert_dialog.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            System.exit(0);
                        }
                    }
                );

                alert_dialog.create().show();

            }

        }

        if (requestCode == 555) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Location Permission: ", "Granted");

                runWeatherService();

            } else{
                Log.d("Location Permission: ", "Denied");

                // Creating alert dialog
                AlertDialog.Builder alert_dialog_2 = new AlertDialog.Builder(this);
                alert_dialog_2.setMessage("All the permissions are needed. The app will now be closed");

                alert_dialog_2.setPositiveButton(
                        "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                System.exit(0);
                            }
                        }
                );

                alert_dialog_2.create().show();
            }

        }
    }
}
