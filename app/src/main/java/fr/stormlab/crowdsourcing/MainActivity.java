package fr.stormlab.crowdsourcing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesRenderer;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.stormlab.crowdsourcing.data.DataWriter;
import fr.stormlab.crowdsourcing.data.SQLiteWriter;
import fr.stormlab.crowdsourcing.service.ForegroundService;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private XYSeries currentSeries = null;
    private XYPlot plot = null;
    private DataToShow currentShow = DataToShow.ALL;
    private CountDownTimer countDownTimer = null;

    // Should be in the same order as ./res/values/strings/activity_main_spinner_values
    private enum DataToShow {ALL, LAST_MINUTE, LAST_5_MINUTES, LAST_15_MINUTES, LAST_HOUR};

    //Custom CountDownTimer
    private class ActualizePlotTimer extends CountDownTimer{

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public ActualizePlotTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onTick(long millisUntilFinished) {
            setDataOnPlot(currentShow);
        }

        @Override
        public void onFinish() {
            start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start the foreground service
        Intent intent = new Intent(getApplicationContext(), ForegroundService.class);
        startForegroundService(intent);
        // Make the plot
        this.plot = findViewById(R.id.activity_main_plot);
        Display display = getWindowManager().getDefaultDisplay();
        ViewGroup.LayoutParams params = this.plot.getLayoutParams();
        params.height = display.getHeight() - 410;
        this.plot.setLayoutParams(params);
        this.plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(
                new Format() {
                    private final DateFormat dateFormat = DateFormat.getTimeInstance();
                    @Override
                    public StringBuffer format(Object obj,
                                               @NonNull StringBuffer toAppendTo,
                                               @NonNull FieldPosition pos) {
                        Number timestamp = (Number) obj;
                        return dateFormat.format(timestamp, toAppendTo, pos);
                    }
                    @Override
                    public Object parseObject(String source, @NonNull ParsePosition pos) {
                        return null;
                    }
                }
        );
        setDataOnPlot(DataToShow.ALL);
        // Set spinner content
        Spinner spinner = findViewById(R.id.activity_main_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.activity_main_spinner_values,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        // Set button action
        Button button = findViewById(R.id.activity_main_button);
        button.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.countDownTimer == null) {
            this.countDownTimer = new ActualizePlotTimer(Long.MAX_VALUE, 10000);
        }
        this.countDownTimer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            countDownTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setDataOnPlot(DataToShow.values()[position]);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        setDataOnPlot(DataToShow.ALL);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
        dataWriter.clearData();
        setDataOnPlot(this.currentShow);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setDataOnPlot(DataToShow dataToShow) {
        DataWriter dataWriter = new SQLiteWriter(this.getApplicationContext());
        Map<Long, List<String>> data  = dataWriter.getData();
        Set<Long> abscissaUnformatted = data.keySet();
        List<Long> abscissa = new LinkedList<>();
        long timestamp = 0;
        switch(dataToShow) {
            case ALL:
                break;
            case LAST_MINUTE:
                timestamp = System.currentTimeMillis() - 60 * 1000;
                break;
            case LAST_5_MINUTES:
                timestamp = System.currentTimeMillis() - 5 * 60 * 1000;
                break;
            case LAST_15_MINUTES:
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000;
                break;
            case LAST_HOUR:
                timestamp = System.currentTimeMillis() - 60 * 60 * 1000;
        }
        for (long entry : abscissaUnformatted) {
            if (entry >= timestamp) {
                abscissa.add(entry);
            }
        }
        abscissa.sort(Comparator.naturalOrder());
        List<Integer> ordinate = new ArrayList<>();
        int maxRange = 0;
        int minRange = 0;
        for (long entry : abscissa) {
            List<String> listWifi = data.get(entry);
            if (listWifi == null) ordinate.add(0);
            else {
                if (maxRange < listWifi.size()) maxRange = listWifi.size();
                ordinate.add(listWifi.size());
            }
        }
        Log.i("Activity", "Number of points : " + ordinate.size());
        XYSeries apsXY =  new SimpleXYSeries(abscissa, ordinate, "Wifi Points");
        LineAndPointFormatter series1Format = new
                LineAndPointFormatter(Color.LTGRAY, Color.parseColor("#3780BF"), null, null);
        if (this.plot == null) {
            this.plot = findViewById(R.id.activity_main_plot);
        }
        if (this.currentSeries != null) {
            this.plot.removeSeries(this.currentSeries);
        }
        this.currentSeries = apsXY;
        this.currentShow = dataToShow;
        this.plot.clear();
        this.plot.addSeries(apsXY, series1Format);
        this.plot.setRangeBoundaries(minRange, maxRange, BoundaryMode.FIXED);
        this.plot.setRangeStep(StepMode.INCREMENT_BY_VAL, Math.round((maxRange - minRange) / 10));
        this.plot.redraw();
    }

}