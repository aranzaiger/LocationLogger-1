package com.example.aran.sagi.locationlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by aranz on 19-Apr-16.
 */
public class Location {

    protected int id;
    protected double lat, lon;
    protected String address;
    protected long timestamp;
    protected SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
    ;


    public Location(int id, double lat, double lon, String address, long timestamp) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.timestamp = timestamp;

    }

    public static void insertNewLocation(SQLiteOpenHelper dbHelper, Location location) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(appDB.LocationsEntry.LAT, location.getLat());
        values.put(appDB.LocationsEntry.LON, location.getLon());
        values.put(appDB.LocationsEntry.TIME_STAMP, location.getTimestamp());
        values.put(appDB.LocationsEntry.ADDRESS, location.getAddress());

        db.insert(appDB.LocationsEntry.TABLE_NAME, null, values);

        db.close();

        Log.d("DB", "insertNewLocation: Location Saved!");
    }

    public double getLon() {
        return lon;
    }


    public double getLat() {
        return lat;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTimestamp() {
        Date rd = new Date(timestamp);
        return sdf.format(rd);
    }


    public void save(SQLiteOpenHelper dbHelper, Context context) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(appDB.LocationsEntry.ADDRESS, this.address);

        String where = appDB.LocationsEntry._ID + " =?";
        String[] whereArgs = {Integer.toString(this.id)};

        db.update(appDB.LocationsEntry.TABLE_NAME, values, where, whereArgs);

        db.close();

        Toast.makeText(context, "Address saved!", Toast.LENGTH_LONG).show();
    }

    public static Cursor getAll(DBHelper dbHelper) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT * FROM " + appDB.LocationsEntry.TABLE_NAME, selectionArgs);
        while(c.moveToNext()){
            Log.d("TAG", "LAT: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LAT)));
        }
        return c;
    }

    public static Cursor getByLat(DBHelper dbHelper, int lat){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT * FROM " + appDB.LocationsEntry.TABLE_NAME+
                " WHERE "+appDB.LocationsEntry.LAT+" >= "+lat+
                " AND "+appDB.LocationsEntry.LAT+" < "+(lat+1),
                selectionArgs);
        while(c.moveToNext()){
            Log.d("TAG", "LAT: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LAT)));
            Log.d("TAG", "LON: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LON)));
        }
        return c;
    }

    public static Cursor getByLon(DBHelper dbHelper, int lon){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT * FROM " + appDB.LocationsEntry.TABLE_NAME+
                        " WHERE "+appDB.LocationsEntry.LON+" >= "+lon+
                        " AND "+appDB.LocationsEntry.LON+" < "+(lon+1),
                selectionArgs);
        while(c.moveToNext()){
            Log.d("TAG", "LAT: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LAT)));
            Log.d("TAG", "LON: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LON)));
        }
        return c;
    }

    public static Cursor getByLatAndLon(DBHelper dbHelper, int lat, int lon){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {};
        Cursor c = db.rawQuery("SELECT * FROM " + appDB.LocationsEntry.TABLE_NAME+
                        " WHERE "+appDB.LocationsEntry.LAT+" >= "+lat+
                        " AND "+appDB.LocationsEntry.LAT+" < "+(lat+1)+
                        " AND "+appDB.LocationsEntry.LON+" >= "+lon+
                        " AND "+appDB.LocationsEntry.LON+" < "+(lon+1),
                selectionArgs);
        while(c.moveToNext()){
            Log.d("TAG", "LAT: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LAT)));
            Log.d("TAG", "LON: " + c.getDouble(c.getColumnIndex(appDB.LocationsEntry.LON)));
        }
        return c;
    }

}
