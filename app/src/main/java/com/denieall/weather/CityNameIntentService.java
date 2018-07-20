package com.denieall.weather;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class CityNameIntentService extends IntentService {

    public CityNameIntentService() {
        super("CityNameIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        double latitude = intent.getDoubleExtra("lat", 0);
        double longitude = intent.getDoubleExtra("long", 0);

        List<Address> addresses;
        String city = null;

        // Getting city name from latitude and longitude
        Geocoder gcd = new Geocoder(this, Locale.getDefault());

        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {

                city = addresses.get(0).getLocality();

            }

        } catch (Exception e) {

            Log.i("Exception: ", e.getMessage());

        }

        if (city != null) {

            // Sending city name to the broadcast receiver
            Intent i = new Intent();
            i.putExtra("City", city);
            i.setAction(MainActivity.CITY_RECEIVER_CHANNEL);
            sendBroadcast(i);

        }

    }
}
