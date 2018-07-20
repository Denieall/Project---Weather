package com.denieall.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeCityActivity extends AppCompatActivity {

    ConstraintLayout sl;
    EditText et;

    String city;

    public static final String CITY_ERROR_CHANNEL = "com.weather.city_error";
    public static final String CITY_OK_CHANNEL = "com.weather.city_ok";

    BroadcastReceiver city_error_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(), "City not found", Toast.LENGTH_SHORT).show();

        }
    };

    BroadcastReceiver city_ok_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("test", "onReceive: ");

            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("temp", intent.getDoubleExtra("temp", 0));
            i.putExtra("wallpaper", intent.getIntExtra("wallpaper", 0));
            i.putExtra("logo", intent.getIntExtra("logo", 0));
            i.putExtra("city", city);
            startActivity(i);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_city);

        sl = findViewById(R.id.settingLayout);
        et = findViewById(R.id.editText);

        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                city = textView.getText().toString();

                Intent intent = new Intent(getApplicationContext(), WeatherByCityIntentService.class);
                intent.putExtra("city", city);
                startService(intent);

                return true;
            }
        });

        IntentFilter city_error_intent_filter = new IntentFilter();
        city_error_intent_filter.addAction(CITY_ERROR_CHANNEL);
        registerReceiver(city_error_receiver, city_error_intent_filter);

        IntentFilter city_ok_intent_filter = new IntentFilter();
        city_ok_intent_filter.addAction(CITY_OK_CHANNEL);
        registerReceiver(city_ok_receiver, city_ok_intent_filter);
    }

    public void back_clicked(View view) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            unregisterReceiver(city_error_receiver);
            unregisterReceiver(city_ok_receiver);
        } catch (Exception e) {
            Log.i("Error", e.getMessage());
        }
    }
}
