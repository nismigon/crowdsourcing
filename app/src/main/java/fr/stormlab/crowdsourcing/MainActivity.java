package fr.stormlab.crowdsourcing;

import android.app.Activity;
import android.os.Bundle;

import fr.stormlab.crowdsourcing.wifi.WifiJobService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WifiJobService.scheduleJob(this);
    }
}