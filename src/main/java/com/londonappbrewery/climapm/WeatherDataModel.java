package com.londonappbrewery.climapm;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataModel {

    // TODO: Declare the member variables here
    private String mTemperature;
    private String mCity;
    private String mIconName;
    private int mCondition;

    /** SAS: Lesson 134. We are creating the object here and have made the method static so object is created here when the method is called from somewhere."
     * we are using the fromJson() method to separate the json parsing from the constructor of the WeatherDataModel.
     * It's a design choice more than anything.
     * We are making our WeatherDataModel independent from having to receive a JSON because this logic is now contained insdie a mehtod.
     * Also, we get to see a new design pattern in action for creating objects (called the static factory method) */

    // TODO: Create a WeatherDataModel from a JSON:
    public static WeatherDataModel fromJson (JSONObject jsonObject) {
        try {
            WeatherDataModel weatherData = new WeatherDataModel();
            weatherData.mCity = jsonObject.getString("name");
            weatherData.mCondition = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            weatherData.mIconName = updateWeatherIcon(weatherData.mCondition);

            double tempResult = jsonObject.getJSONObject("main").getDouble("temp") - 273.15; //To convert Kelvin to Celsius
            int roundedValue = (int) Math.rint(tempResult);
            weatherData.mTemperature = String.valueOf(roundedValue);

            return weatherData;
        } catch (JSONException e){
            e.printStackTrace();
            return null;
        }
    }


    // TODO: Uncomment to this to get the weather image name from the condition:
    private static String updateWeatherIcon(int condition) {

        if (condition >= 0 && condition < 300) {
            return "tstorm1";
        } else if (condition >= 300 && condition < 500) {
            return "light_rain";
        } else if (condition >= 500 && condition < 600) {
            return "shower3";
        } else if (condition >= 600 && condition <= 700) {
            return "snow4";
        } else if (condition >= 701 && condition <= 771) {
            return "fog";
        } else if (condition >= 772 && condition < 800) {
            return "tstorm3";
        } else if (condition == 800) {
            return "sunny";
        } else if (condition >= 801 && condition <= 804) {
            return "cloudy2";
        } else if (condition >= 900 && condition <= 902) {
            return "tstorm3";
        } else if (condition == 903) {
            return "snow5";
        } else if (condition == 904) {
            return "sunny";
        } else if (condition >= 905 && condition <= 1000) {
            return "tstorm3";
        }

        return "dunno";
    }

    // TODO: Create getter methods for temperature, city, and icon name:


    public String getTemperature() {
        return mTemperature+"°";
    }

    public String getCity() {
        return mCity;
    }

    public String getIconName() {
        return mIconName;
    }
}
