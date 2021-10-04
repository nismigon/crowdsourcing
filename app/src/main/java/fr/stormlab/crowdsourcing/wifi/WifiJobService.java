package fr.stormlab.crowdsourcing.wifi;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;




// In order to have the Wifi Job service working, I have disable  :
// - Wifi scan limit
// - Standby mode when in charge
// All of this are options are in developer options

public class WifiJobService extends JobService {

    public static int PERIOD_MILLISECONDS = 1000;

    // Method launch when the job start
    // It basically creates an handler when the results of the scan arrived, and start the scan
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("WIFI_JOB_SERVICE", "Started" );
        Context context = getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // Add a receiver for the event
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            // Important part : What we do when we received a message
            // We get the results of the scans, and we show the different network found
            @Override
            public void onReceive(Context c, Intent intent) {
                Log.d("WIFI_JOB_SERVICE", "Result of the scan received");
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    List<ScanResult> scanResultList = wifiManager.getScanResults();
                    Log.i("WIFI_JOB_SERVICE", "Founded network : " + scanResultList.size());
                    for (ScanResult scanResult : scanResultList) {
                        Log.i("WIFI_JOB_SERVICE", scanResult.SSID);
                    }
                } else {
                    Log.i("WIFI_JOB_SERVICE", "Failed to launch the Wifi scan");
                }
                // We need to unregister the receiver and schedule a new job
                context.unregisterReceiver(this);
                WifiJobService.scheduleJob(getApplicationContext());
            }
        };
        // Register the event
        context.registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            Log.i("WIFI_JOB_SERVICE", "Failed to launch the scan");
            // We need to unregister the receiver and schedule a new job
            context.unregisterReceiver(wifiScanReceiver);
            WifiJobService.scheduleJob(getApplicationContext());
        }
        return true;
    }

    // Called when the Job is stopped
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("WIFI_JOB_SERVICE", "Stopped" );
        return false;
    }

    /**
     * This method permits to schedule a new Job
     * @param context Call context
     */
    public static void scheduleJob(Context context) {
        // Creation of a new Job
        ComponentName serviceComponent = new ComponentName(context, WifiJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(0, serviceComponent)
                .setMinimumLatency(WifiJobService.PERIOD_MILLISECONDS)
                .setOverrideDeadline(WifiJobService.PERIOD_MILLISECONDS)
                .build();
        // Add the new Job into the scheduler
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(jobInfo);
        if (result != JobScheduler.RESULT_SUCCESS) {
            Log.e("WIFI_JOB_SERVICE", "Failed to launch the job");
        }
    }
}
