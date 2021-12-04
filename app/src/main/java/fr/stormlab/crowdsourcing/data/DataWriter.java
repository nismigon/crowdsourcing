package fr.stormlab.crowdsourcing.data;

import java.util.Map;
import java.util.List;

public interface DataWriter {
    boolean addData(double latitude, double longitude, List<String> wifiPoints);
    Map<Integer, List<String>> getData();
    void clearData();
    Position getLocation(int gps_id);
}
