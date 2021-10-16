package fr.stormlab.crowdsourcing.data;

import java.util.Map;
import java.util.List;

public interface DataWriter {
    boolean addData(long timestamp, List<String> wifiPoints);
    Map<Long, List<String>> getData();
    void clearData();
}
