package fr.stormlab.crowdsourcing.wifi;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;

import fr.stormlab.crowdsourcing.R;

public class WifiReceiver extends BroadcastReceiver {

    private final WifiManager wifiManager;
    // TODO : Put this in configuration
    public static final int NOTIFICATION_ID = 1789;

    public WifiReceiver(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    // Important part : What we do when we received a message
    // We get the results of the scans, and we show the different network found
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WIFI_JOB_SERVICE", "Result of the scan received");
        boolean success = intent.getBooleanExtra(
                WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (success) {
            List<ScanResult> scanResultList = this.wifiManager.getScanResults();
            Log.i("WIFI_JOB_SERVICE", "Founded network : " + scanResultList.size());
            generateNotification(context, "Founded network : " + scanResultList.size());
            for (ScanResult scanResult : scanResultList) {
                Log.i("WIFI_JOB_SERVICE", scanResult.SSID);
            }
        } else {
            Log.i("WIFI_JOB_SERVICE", "Failed to launch the Wifi scan");
        }
        // We need to unregister the receiver and schedule a new job
        context.unregisterReceiver(this);
        WifiJobService.scheduleJob(context);
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
        notificationManager.notify(WifiReceiver.NOTIFICATION_ID, builder.build());
    }
}
