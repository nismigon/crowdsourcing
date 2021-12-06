package fr.stormlab.crowdsourcing.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This class permits to write data in an SQLiteDatabase
public class SQLiteWriter implements DataWriter {

    // This is the handler for SQLite
    private static class SQLiteHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "CROWDSOURCING_DB";
        private static final int DATABASE_VERSION = 3;

        public SQLiteHelper(Context context) {
            // Creation of the database
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creation of the table gps_location in the database
            db.execSQL("CREATE TABLE IF NOT EXISTS crowdsourcing_gps (" +
                    "gps_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "latitude REAL NOT NULL," +
                    "longitude REAL NOT NULL," +
                    "timestamp INTEGER NOT NULL" +
                    ")");
            // Creation of the table data in the database
            db.execSQL("CREATE TABLE IF NOT EXISTS crowdsourcing_data (" +
                    "data_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "bssid TEXT NOT NULL," +
                    "gps_id INTEGER NOT NULL," +
                    "FOREIGN KEY(gps_id) REFERENCES crowdsourcing_gps(gps_id)" +
                    ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("SQLite", "Upgrading to the next version...");
            // Delete the table and recreate it
            db.execSQL("DROP TABLE IF EXISTS crowdsourcing_data");
            db.execSQL("DROP TABLE IF EXISTS crowdsourcing_gps");
            onCreate(db);
        }
    }

    private static SQLiteHelper dbHelper;      // Connection to the database
    private static SQLiteDatabase database;    // Handler for the database

    public SQLiteWriter(Context context) {
        if (dbHelper == null) dbHelper = new SQLiteHelper(context);
        if (database == null ) database = dbHelper.getWritableDatabase();
    }

    // Insert data with timestamp and the list of the wifiPoints
    @Override
    public void addData(double latitude, double longitude, List<String> wifiPoints) {
        long timestamp = System.currentTimeMillis();
        int gps_id = this.getGPSLocation(latitude, longitude);
        // Insert the new GPS point and get the generated ID
        if (gps_id == -1) {
            insertGPSLocation(latitude, longitude, timestamp);
            gps_id = this.getGPSLocation(latitude, longitude);
        }
        // Just update the timestamp if the id already exist
        else {
            updateTimestampGPS(gps_id, timestamp);
        }
        // Remove all the previous data for this point
        this.removePreviousData(gps_id);
        // Add all the founded wifi in the database
        for (String wifiPoint : wifiPoints) {
            ContentValues values = new ContentValues();
            values.put("bssid", wifiPoint);
            values.put("gps_id", gps_id);
            database.insert("crowdsourcing_data", null, values);
        }
    }

    // This function return the id for a specified location
    private int getGPSLocation(double latitude, double longitude) {
        Cursor cursor = database.rawQuery(
                "SELECT gps_id FROM crowdsourcing_gps WHERE latitude = ? AND longitude = ?",
                new String[]{String.valueOf(round(latitude,4)), String.valueOf(round(longitude, 4))});
        cursor.moveToFirst();
        // If no result, return -1
        if (cursor.isAfterLast()) {
            cursor.close();
            return -1;
        }
        // Get the value of the id
        else {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
    }

    // Insert a new GPS Location
    private void insertGPSLocation(double latitude, double longitude, long timestamp) {
        ContentValues values = new ContentValues();
        values.put("latitude", round(latitude, 4));
        values.put("longitude", round(longitude, 4));
        values.put("timestamp", timestamp);
        database.insert("crowdsourcing_gps", null, values);
    }

    // Update a timestamp inside a location
    private void updateTimestampGPS(int id, long timestamp) {
        database.execSQL("UPDATE crowdsourcing_gps SET timestamp = ? WHERE gps_id = ?",
                new String[]{String.valueOf(timestamp), String.valueOf(id)});
    }

    // Remove all the previous data for a GPS Id
    private void removePreviousData(int gps_id) {
        database.execSQL("DELETE FROM crowdsourcing_data WHERE gps_id = ?",
                new String[]{String.valueOf(gps_id),});
    }

    // Get the data from the database
    @Override
    public Map<Integer, List<String>> getData() {
        HashMap<Integer, List<String>> data = new HashMap<>();
        // Get all the data
        Cursor cursor = database.rawQuery("SELECT gps_id, bssid FROM crowdsourcing_data", null);
        cursor.moveToFirst();
        // Generation of the map
        while(!cursor.isAfterLast()) {
            int gps_id = cursor.getInt(0);
            String wifi = cursor.getString(1);

            List<String> wifiList;
            // Insertion of a new entry or get the list
           if (!data.containsKey(gps_id)) {
                wifiList = new ArrayList<>();
                data.put(gps_id, wifiList);
            } else {
                wifiList = data.get(gps_id);
            }
            assert wifiList != null;
            // Add a new entry
            wifiList.add(wifi);
            cursor.moveToNext();
        }
        // Close the cursor
        cursor.close();
        return data;
    }

    // Clear all the data of the database
    @Override
    public void clearData() {
        database.execSQL("DELETE FROM crowdsourcing_data");
    }

    // Get the location and the timestamp
    public GPSObject getLocation(int gps_id) {
        Cursor cursor = database.rawQuery(
                "SELECT latitude, longitude, timestamp FROM crowdsourcing_gps WHERE gps_id = ?",
                new String[]{String.valueOf(gps_id)});
        cursor.moveToFirst();
        // This case should never happen
        if (cursor.isAfterLast()) {
            cursor.close();
            return new GPSObject(-1, -1, 0);
        }
        double latitude = cursor.getDouble(0);
        double longitude = cursor.getDouble(1);
        long timestamp = cursor.getLong(2);
        cursor.close();
        return new GPSObject(latitude, longitude, timestamp);
    }


    // Round a value
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
