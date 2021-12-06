package fr.stormlab.crowdsourcing.data;

import java.util.Map;
import java.util.List;

public interface DataWriter {
    void addData(double latitude, double longitude, List<String> wifiPoints);
    Map<Integer, List<String>> getData();
    void clearData();
    GPSObject getLocation(int gps_id);
}
