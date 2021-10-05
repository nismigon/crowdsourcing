package fr.stormlab.crowdsourcing.wifi;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;

// In order to have the Wifi Job service working, I have disable  :
// - Wifi scan limit
// - Standby mode when in charge
// All of this are options are in developer options

public class WifiJobService extends JobService {

    // TODO : Put this in configuration
    public static int PERIOD_MILLISECONDS = 1000;
    public static boolean RESCHEDULE = true;

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
        WifiReceiver wifiReceiver = new WifiReceiver(wifiManager);
        // Register the event
        context.registerReceiver(wifiReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            Log.i("WIFI_JOB_SERVICE", "Failed to launch the scan");
            // We need to unregister the receiver and schedule a new job
            context.unregisterReceiver(wifiReceiver);
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
        if (!WifiJobService.RESCHEDULE) return;
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
