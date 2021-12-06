package fr.stormlab.crowdsourcing;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import fr.stormlab.crowdsourcing.data.DataWriter;
import fr.stormlab.crowdsourcing.data.GPSObject;
import fr.stormlab.crowdsourcing.data.SQLiteWriter;

public class RawDataActivity extends AppCompatActivity implements View.OnClickListener{

    private CountDownTimer countDownTimer = null;

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

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_data);
        // Start location request
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0,
                location -> Log.i("ACTIVITY_RAW_DATA", "Location Updated"));
        // Set button action
        Button button = findViewById(R.id.activity_raw_data_button_update_data);
        button.setOnClickListener(this);
        button = findViewById(R.id.activity_raw_data_clear_data);
        button.setOnClickListener(this);
        button = findViewById(R.id.activity_raw_data_map);
        button.setOnClickListener(this);
        // Show data
        showData();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // Set height for raw data
        int height = displayMetrics.heightPixels;
        View v = findViewById(R.id.scrollView2);
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = height - 650;
        v.setLayoutParams(params);
        // Set timer
        this.countDownTimer = new RawDataActivity.ActualizeDataTimer(30000,30000);
        this.countDownTimer.start();
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        if (button.getId() == R.id.activity_raw_data_button_update_data) {
            showData();
        }
        else if (button.getId() == R.id.activity_raw_data_map) {
            Intent myIntent = new Intent(this, MainActivity.class);
            this.startActivity(myIntent);
        }
        else {
            // Clear the data from the database
            DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
            dataWriter.clearData();
            showData();
        }
    }

    private void showData() {
        DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
        Map<Integer, List<String>> data = dataWriter.getData();
        StringBuilder textToShow = new StringBuilder();
        for (Map.Entry<Integer, List<String>> entry : data.entrySet()) {
            // Get Position point
            GPSObject position = dataWriter.getLocation(entry.getKey());
            // Append data to String
            textToShow.append(position).append("\n");
            for (String wifiPoint : entry.getValue()) {
                textToShow.append("\t\t").append(wifiPoint).append("\n");
            }
            textToShow.append("\n");
        }
        TextView textView = findViewById(R.id.activity_raw_data_raw_data);
        textView.setText(textToShow);
    }
}