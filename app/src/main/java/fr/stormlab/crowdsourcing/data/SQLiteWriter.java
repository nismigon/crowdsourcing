package fr.stormlab.crowdsourcing.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteWriter implements DataWriter {

    private static class SQLiteHelper extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "CROWDSOURCING_DB";
        private static final int DATABASE_VERSION = 1;

        public SQLiteHelper(Context context) {
            // Creation of the database
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Creation of the table in the database
            db.execSQL("CREATE TABLE IF NOT EXISTS crowdsourcing_data (" +
                    "timestamp INTEGER NOT NULL," +
                    "bssid TEXT NOT NULL," +
                    "PRIMARY KEY (timestamp, bssid)" +
                    ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("SQLite", "Upgrading to the next version...");
            // Delete the table and recreate it
            db.execSQL("DROP TABLE IF EXISTS crowdsourcing_data");
            onCreate(db);
        }
    }

    private SQLiteHelper dbHelper;      // Connection to the database
    private SQLiteDatabase database;    // Handler for the database

    public SQLiteWriter(Context context) {
        this.dbHelper = new SQLiteHelper(context);
        this.database = this.dbHelper.getWritableDatabase();
    }

    // Insert data with timestamp and the list of the wifiPoints
    @Override
    public boolean addData(long timestamp, List<String> wifiPoints) {
        for (String wifiPoint : wifiPoints) {
            ContentValues values = new ContentValues();
            values.put("timestamp", timestamp);
            values.put("bssid", wifiPoint);
            this.database.insert("crowdsourcing_data", null, values);
        }
        return false;
    }

    // Get the data from the database
    @Override
    public Map<Long, List<String>> getData() {
        HashMap<Long, List<String>> data = new HashMap<>();
        // Get all the data
        Cursor cursor = this.database.query(
                    "crowdsourcing_data",
                    new String[]{"timestamp", "bssid"},
                    null,
                    null,
                    null,
                    null,
                    null);
        cursor.moveToFirst();
        // Generation of the map
        while(!cursor.isAfterLast()) {
            long timestamp = cursor.getLong(0);
            String wifi = cursor.getString(1);
            List<String> wifiList;
            // Insertion of a new entry or get the list
            if (!data.containsKey(timestamp)) {
                wifiList = new ArrayList<>();
                data.put(timestamp, wifiList);
            } else {
                wifiList = data.get(timestamp);
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
        this.database.execSQL("DELETE FROM crowdsourcing_data");
    }

}
