package fr.stormlab.crowdsourcing.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import fr.stormlab.crowdsourcing.wifi.WifiJobService;

// This service permits to continue to execute the service, whereas the application is closed
public class ForegroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("FOREGROUND_SERVICE", "Started");
        // Launch the job
        WifiJobService.RESCHEDULE = true;
        WifiJobService.scheduleJob(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("FOREGROUND_SERVICE", "Stopped");
        WifiJobService.RESCHEDULE = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
