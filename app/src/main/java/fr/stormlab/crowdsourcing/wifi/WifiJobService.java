package fr.stormlab.crowdsourcing.wifi;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static androidx.core.app.ActivityCompat.requestPermissions;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

// In order to have the Wifi Job service working, I have disable  :
// - Wifi scan limit
// - Standby mode when in charge
// All of this are options are in developer options

public class WifiJobService extends JobService {

    // TODO : Put this in configuration
    public static int PERIOD_MILLISECONDS = 3000;
    public static boolean RESCHEDULE = true;
    private static Date lastScan = null;

    // Method launch when the job start
    // It basically creates an handler when the results of the scan arrived, and start the scan
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.i("WIFI_JOB_SERVICE", "Started" );
        Context context = getApplicationContext();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        // You have to use this code if you don't have disabled the wifi restriction in the developers options
        // Date current = Calendar.getInstance().getTime();
        // if (lastScan == null || current.getTime() - lastScan.getTime() > 60000) {
        //    lastScan = current;
        //    if (!wifiManager.startScan()) {
        //        Log.i("WIFI_JOB_SERVICE", "Failed to launch startScan");
        //    }
        //}
        if(!wifiManager.startScan()) {
            Log.i("WIFI_JOB_SERVICE", "Failed to launch startScan");
        }
        List<ScanResult> scanResultList = wifiManager.getScanResults();
        Log.i("WIFI_JOB_SERVICE", "Founded network : " + scanResultList.size());
        for (ScanResult scanResult : scanResultList) {
            Log.i("WIFI_JOB_SERVICE", scanResult.SSID);
        }
        scheduleJob(this.getApplicationContext());
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
