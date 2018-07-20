package com.denieall.weather;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class WeatherDataModel {
    public double temperature;
    public int logo;



    public static WeatherDataModel fromJSON(JSONObject jsonObject) {

        Log.i("Conversion", Thread.currentThread().getName());

        WeatherDataModel weatherDataModel = new WeatherDataModel();

        try {

            int weather = Integer.parseInt(jsonObject.getJSONArray("weather").getJSONObject(0).getString("id"));


            String temperatureKelvin = jsonObject.getJSONObject("main").getString("temp");

            DecimalFormat decimalplace = new DecimalFormat("#,#0.0");

            weatherDataModel.temperature = Double.parseDouble(decimalplace.format(Double.parseDouble(temperatureKelvin) - 273.15));

            String icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");

            // Get if weather requested on day or night
            if (icon.indexOf('d') >= 0) {
                icon = "Day";
            } else {
                icon = "Night";
            }

            // Set the correct wallpaper and logo matching in weather
            if (weather == 800) {

                if (icon.equals("Day")) {

                    weatherDataModel.logo = R.drawable.clear_day;

                } else if (icon.equals("Night")) {

                    weatherDataModel.logo = R.drawable.clear_night;

                }

            } else if ((weather >= 801 && weather <= 804) || (weather >= 701 && weather <= 781)) {

                if (icon.equals("Day")) {

                    weatherDataModel.logo = R.drawable.cloudy_day;

                } else if (icon.equals("Night")) {

                    weatherDataModel.logo = R.drawable.cloudy_night;

                }

            } else if ((weather >= 500 && weather <= 531) || (weather >= 300 && weather <= 321)) {

                weatherDataModel.logo = R.drawable.rainy;

            } else if (weather >= 200 && weather <= 232) {

                weatherDataModel.logo = R.drawable.thunderstorm;

            } else if (weather >= 600 && weather <= 622) {

                weatherDataModel.logo = R.drawable.snowy;

            }


        } catch (JSONException e) {

            e.printStackTrace();
            return null;
        }

        return  weatherDataModel;
    }
}
