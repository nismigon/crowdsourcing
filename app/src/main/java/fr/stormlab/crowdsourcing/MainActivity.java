package fr.stormlab.crowdsourcing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;

import fr.stormlab.crowdsourcing.data.DataWriter;
import fr.stormlab.crowdsourcing.data.Position;
import fr.stormlab.crowdsourcing.data.SQLiteWriter;
import fr.stormlab.crowdsourcing.service.ForegroundService;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private CountDownTimer countDownTimer = null;
    private GoogleMap googleMap = null;

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

    }

    //Custom CountDownTimer
    private class ActualizeDataTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public ActualizeDataTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        // When the timer is firing, actualization of the graph
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onTick(long millisUntilFinished) {
            showData();
        }

        @Override
        public void onFinish() {
            start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start the foreground service
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        startForegroundService(intent);
        // Set button action
        Button button = findViewById(R.id.activity_main_button_clear_data);
        button.setOnClickListener(this);
        button = findViewById(R.id.activity_main_button_update_map);
        button.setOnClickListener(this);
        // Show data
        showData();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_map);
        mapFragment.getMapAsync(this);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
        params.height = height - 600;
        mapFragment.getView().setLayoutParams(params);
        // Set timer
        this.countDownTimer = new ActualizeDataTimer(10000,10000);
        this.countDownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.countDownTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.countDownTimer.cancel();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        if (button.getId() == R.id.activity_main_button_update_map) {
            showData();
        }
        else {
            // Clear the data from the database
            DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
            dataWriter.clearData();
        }
    }

    private void showData() {
        if (this.googleMap != null) {
            this.googleMap.clear();
            DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
            Map<Integer, List<String>> data = dataWriter.getData();
            for (Map.Entry<Integer, List<String>> entry : data.entrySet()) {
                // Get Position point
                Position position = dataWriter.getLocation(entry.getKey());
                LatLng tmpPosition = new LatLng(position.latitude, position.longitude);
                StringBuilder snippet = new StringBuilder();
                for (String wifiPoint : entry.getValue()) {
                    snippet.append(wifiPoint).append("\n");
                }
                this.googleMap.addMarker(new MarkerOptions()
                        .position(tmpPosition)
                        .title(entry.getValue().size() + " wifi point detected")
                );
            }
            LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null){
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                LatLng current = new LatLng(latitude, longitude);
                this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            }
        }
    }

}