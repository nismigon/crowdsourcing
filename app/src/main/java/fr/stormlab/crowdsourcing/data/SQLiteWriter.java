package fr.stormlab.crowdsourcing.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteWriter implements DataWriter {

    private static class SQLiteHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "CROWDSOURCING_DB";
        private static final int DATABASE_VERSION = 2;

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
                    "longitude REAL NOT NULL" +
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
    public boolean addData(double latitude, double longitude, List<String> wifiPoints) {
        int gps_id = this.getGPSLocation(latitude, longitude);
        if (gps_id == -1) {
            insertGPSLocation(latitude, longitude);
            gps_id = this.getGPSLocation(latitude, longitude);
        }
        this.removePreviousData(gps_id);
        for (String wifiPoint : wifiPoints) {
            Log.i("SQLITEWRITER", wifiPoint);
            ContentValues values = new ContentValues();
            values.put("bssid", wifiPoint);
            values.put("gps_id", gps_id);
            database.insert("crowdsourcing_data", null, values);
        }
        return false;
    }

    private int getGPSLocation(double latitude, double longitude) {
        Cursor cursor = database.rawQuery(
                "SELECT gps_id FROM crowdsourcing_gps WHERE latitude = ? AND longitude = ?",
                new String[]{String.valueOf(round(latitude,5)), String.valueOf(round(longitude, 5))});
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            cursor.close();
            return -1;
        }
        else {
            int id = cursor.getInt(0);
            cursor.close();
            return id;
        }
    }

    private void insertGPSLocation(double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put("latitude", round(latitude, 5));
        values.put("longitude", round(longitude, 5
        ));
        database.insert("crowdsourcing_gps", null, values);
    }

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

    public Position getLocation(int gps_id) {
        Cursor cursor = database.rawQuery(
                "SELECT latitude, longitude FROM crowdsourcing_gps WHERE gps_id = ?",
                new String[]{String.valueOf(gps_id)});
        cursor.moveToFirst();
        if (cursor.isAfterLast()) {
            cursor.close();
            return new Position(-1, -1);
        }
        double latitude = cursor.getDouble(0);
        double longitude = cursor.getDouble(1);
        cursor.close();
        return new Position(latitude, longitude);
    }


    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
