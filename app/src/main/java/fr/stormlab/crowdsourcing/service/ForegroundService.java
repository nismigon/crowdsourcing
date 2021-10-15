package fr.stormlab.crowdsourcing.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import fr.stormlab.crowdsourcing.R;
import fr.stormlab.crowdsourcing.wifi.WifiJobService;

// This service permits to continue to execute the service, whereas the application is closed
public class ForegroundService extends Service {

    public static final int NOTIFICATION_ID = 1789;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("FOREGROUND_SERVICE", "Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        WifiJobService.RESCHEDULE = true;
        WifiJobService.scheduleJob(getApplicationContext());
        this.generateNotification(this.getApplicationContext(), "Application still running");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("FOREGROUND_SERVICE", "Stopped");
        WifiJobService.RESCHEDULE = false;
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("RestartService");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    // Generate (or modify if existing) a notification
    private void generateNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // TODO : Put this in configuration
        String channelId = "CrowdsourcingID";
        // TODO : Put this in configuration
        String channelName = "Crowdsourcing";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        // TODO : Put this in configuration
        builder.setContentTitle("Crowdsourcing Results");
        builder.setContentText(message);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        startForeground(ForegroundService.NOTIFICATION_ID, builder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
