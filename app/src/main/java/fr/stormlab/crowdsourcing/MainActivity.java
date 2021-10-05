package fr.stormlab.crowdsourcing;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import fr.stormlab.crowdsourcing.service.ForegroundService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
       startService(intent);
    }
}