package com.denieall.weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LocationForegroundService extends Service {

    FusedLocationProviderClient mClient;
    NotificationCompat.Builder mBuilder;

    // Constants
    public static final String CHANNEL_ID = "com.weather.locator.notification";
    public static final String TAG = "Location Service";

    public LocationForegroundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create notification channel for android oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel nc = new NotificationChannel(CHANNEL_ID, "Locator service notification", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(nc);
            }

        }

        // Build the notification
        Intent notifyServiceIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyServiceIntent, 0);

        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Weather App")
                .setContentText("Location service running...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        mClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals("START FOREGROUND")) {

            // Start the foreground service and display the notification
            startForeground(95, mBuilder.build());

            Thread location_thread = new Thread() {

                @Override
                public void run() {
                    super.run();

                    getCurrentLocation();

                }
            };

            location_thread.start();

        } else if (intent.getAction().equals("STOP FOREGROUND")) {

            // Stop the foreground service
            stopForeground(true);
            stopSelf();

        }


        return START_NOT_STICKY;
    }

    // Get last known position
    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {

        Log.i("GPS", Thread.currentThread().getName());

        OnSuccessListener<Location> locationListener = new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                Intent res_intent = new Intent();
                res_intent.putExtra("location", new double[] {location.getLatitude(), location.getLongitude()});
                res_intent.setAction(MainActivity.RECEIVER_CHANNEL);
                sendBroadcast(res_intent);

            }
        };

        mClient.getLastLocation().addOnSuccessListener(locationListener);

    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
