package fr.stormlab.crowdsourcing.data;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GPSObject {

    public double latitude;     // Latitude of the position
    public double longitude;    // Longitude of the position
    public long timestamp;      // Timestamp when the position have been recorded

    private static final DecimalFormat df = new DecimalFormat("0.0000");

    public GPSObject(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        Date date = new Date(this.timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.FRANCE);
        return df.format(latitude) + " - " + df.format(longitude) + " ( " + formatter.format(date) + " ) ";
    }

}
