package com.example.aran.sagi.locationlogger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sagi on 4/2/16.
 *
 * SINGLETON
 */
public class DBHelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "Ex2.db";
    private static DBHelper self;

    private DBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + appDB.LocationsEntry.TABLE_NAME + " ( "+
                appDB.LocationsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                appDB.LocationsEntry.LAT + " DOUBLE, " +
                appDB.LocationsEntry.LON + " DOUBLE, " +
                appDB.LocationsEntry.TIME_STAMP + " TEXT, " +
                appDB.LocationsEntry.ADDRESS + " TEXT ); ";

        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + appDB.LocationsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }

    public static DBHelper getInstance(Context context){
        if(self == null){
            self = new DBHelper(context);
        }
        return self;
    }

}
