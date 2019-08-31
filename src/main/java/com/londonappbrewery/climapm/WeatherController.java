package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final int REQUEST_CODE = 123;   //SAS: We can place any number as this code. just to validate.
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "b001a691416ad7d57b99d9153fe7ae3e";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    String newCity;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** Starting new activity for changing city. */
                Intent intentChangeCity = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(intentChangeCity);
                //setContentView(R.layout.change_city_layout);
                // Don't use setContentView. This doesn't enable the back button and does not support finish() method on new screen. It doesn't start activity.
            }
        });

    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() called.");
        /**  Getting intent data from ChangeCity activity. */
        Intent intent = getIntent();
        newCity = intent.getStringExtra("new_city_name");

        if (newCity != null) {
            getWeatherForNewCity(newCity);
        } else {
            /** if there is no city change then call weather for current city */
            Log.d("Clima", "Getting weather for current location.");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        networking(params); /** This method adds the params to URL and send to the server and handle the response. */
    }



    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        /** Location manager and handler */
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d("Clima", "onLocationChanged() callback received");
                String longitude = String.valueOf(location.getLongitude());
                String latitude = String.valueOf(location.getLatitude());
                Log.d("Clima", "Longitude = "+longitude+", Latitude = "+latitude);

                /** This "RequestParams" is from a library "https://loopj.com/android-async-http/". Include it in dependencies and sync */
                RequestParams params = new RequestParams();
                params.put("lat", latitude);
                params.put("lon", longitude);
                params.put("appid", APP_ID);
                networking(params);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                Log.d("Clima", "onStatusChanged() callback received");
            }

            @Override
            public void onProviderEnabled(String s) {
                Log.d("Clima", "onProviderEnabled() callback received");
            }

            @Override
            public void onProviderDisabled(String s) {
                Log.d("Clima", "onProviderDisabled() callback received");
            }
        };

        /** This location permission check will be added automatically after the the code below it is written. (requestLocationUpdates)
         * But have to write the code inside this manually. */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            Log.d("Clima", "No permission granted just before return");
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
        Log.d("Clima", "Came to this location");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "Permission granted");
                getWeatherForCurrentLocation();
            } else {
                Log.d("Clima", "Permission not granted");
            }
        }
    }

    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    /** This http library is from "https://loopj.com/android-async-http/". It includes the ""RequestParams" above. */
    private void networking(RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            /** SAS: Lesson 134. This goes to line "WeatherDataModel weatherData = WeatherDataModel.fromJson(response);"
             * we are using the fromJson() method to separate the json parsing from the constructor of the WeatherDataModel.
             * It's a design choice more than anything.
             * We are making our WeatherDataModel independent from having to receive a JSON because this logic is now contained insdie a mehtod.
             * Also, we get to see a new design pattern in action for creating objects (called the static factory method) */

            @Override
            public void onSuccess(int statusCode, Header[] header, JSONObject response) {
                Log.d("Clima", "Success. JSON: "+response.toString());
                WeatherDataModel weatherData = WeatherDataModel.fromJson(response);
                Log.d("Clima", "Temperature: "+ weatherData.getTemperature()+", City: "+weatherData.getCity()+", Icon: "+weatherData.getIconName());
                updateUI(weatherData);
            }

            @Override
            public void onFailure(int statusCode, Header[] header, Throwable e, JSONObject response) {
                Log.e("Clima", "Fail "+e.toString());
                Log.d("Clima", "Status code "+statusCode);
                Toast.makeText(WeatherController.this, "Request Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // TODO: Add updateUI() here:
    private void updateUI(WeatherDataModel weather) {
        mCityLabel.setText(weather.getCity());
        mTemperatureLabel.setText(weather.getTemperature());
        int imageId = getResources().getIdentifier(weather.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(imageId);
    }



    // TODO: Add onPause() here:        To save the resources.
    @Override
    protected void onPause() {
        super.onPause();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }
}
