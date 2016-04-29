package com.example.aran.sagi.locationlogger;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final String TAG = "Ex2";
    private final int FINE_PERM = 0, COARSE_PREM=1;
    protected String newAddress;

    protected EditText lat_et, lon_et;
    protected Button search_btn;
    protected TextView address_tv;
    protected ListView location_lv;
    protected SimpleCursorAdapter location_adapter;
    protected View location_item;
    protected Cursor locations;
    protected DBHelper dbHelper;
    protected GPSTracker gpsTracker;
    protected Boolean locationPermission, addressChanged;
    protected int requestCount = 3;
    private final int SAMPLE_RATE = 5000, SAMPLE_DELAY = 1000;
    protected android.location.Location lastLocation;


    @Override
    public void onDestroy() {
        gpsTracker.stopLocationManager();
        super.onDestroy();
    }
    @Override
    public void onPause() {
        gpsTracker.stopLocationManager();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.locationPermission = true;
//        getPemissions();
        ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERM);


        Log.d("TAG", "=============================== NEW =====================");
        dbHelper = DBHelper.getInstance(this);
        locations = Location.getAll(dbHelper);

        Log.d("TAG","++++++++++++++locations size: "+ locations.getCount());
        lat_et = (EditText) findViewById(R.id.lat_editText);
        lon_et = (EditText) findViewById(R.id.lon_editText);
        search_btn = (Button) findViewById(R.id.search_btn);
        address_tv = (TextView) findViewById(R.id.address_text);

        location_lv = (ListView) findViewById(R.id.location_list);
        gpsTracker = new GPSTracker(this);

        String[] from = {appDB.LocationsEntry.LAT, appDB.LocationsEntry.LON, appDB.LocationsEntry.TIME_STAMP};
        int[] to = {R.id.lbl_lat, R.id.lbl_lng, R.id.lbl_time};

        location_adapter = new SimpleCursorAdapter(this, R.layout.location_item_layout, null, from, to);
        location_item = getLayoutInflater().inflate(R.layout.location_item_layout, null);

//        location_lv.addHeaderView(location_item);

        addressChanged = false;
        location_lv.setAdapter(location_adapter);
        location_lv.setOnItemClickListener(this);



        location_adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == 2 || columnIndex==1){
                    Double loc = cursor.getDouble(columnIndex);
                    TextView textView = (TextView) view;
                    textView.setText(String.format("%.7f", loc));
                    return true;
            }
            return false;

            }
        });


        search_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String tempLat, tempLon;
                tempLat = lat_et.getText().toString();
                tempLon = lon_et.getText().toString();

                //both missing
                if (tempLat.isEmpty() && tempLon.isEmpty()){
                    locations = Location.getAll(dbHelper);
                }
                //only got lon
                else if(tempLat.isEmpty() && !tempLon.isEmpty()) {
                    locations = Location.getByLon(dbHelper,   Integer.parseInt(tempLon));
                }
                //only got lat
                else if(!tempLat.isEmpty() && tempLon.isEmpty()) {
                    locations = Location.getByLat(dbHelper,   Integer.parseInt(tempLat));
                }
                //got both lat & lon
                else {
                    locations = Location.getByLatAndLon(dbHelper,   Integer.parseInt(tempLat),   Integer.parseInt(tempLon));
                }

                location_adapter.changeCursor(locations);

            }
        });



        new Timer().scheduleAtFixedRate(new TimerTask() {
                                            @Override
                                            public void run() {
                                                if(!locationPermission){
                                                    Log.d(TAG,"No permission for location");
                                                    return;
                                                }
                                                Log.d("TAG", "Getting location...");
                                                locationUpdateListener(gpsTracker.getLastKnownLocation());
//                                                gpsTracker.getLocation();
                                            }
                                        },
                SAMPLE_DELAY,
                SAMPLE_RATE
        );
        location_adapter.changeCursor(locations);
    }

    public String toString() {
        return "pin";
    }




    public List<Address> getFromLocation(double latitude, double longitude, int maxResults){
        try {
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this);
            if (latitude != 0 || longitude != 0) {
                addresses = geocoder.getFromLocation(latitude ,
                        longitude, maxResults);
                return addresses;

            } else {
                Toast.makeText(this, "latitude and longitude are null",
                        Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void locationUpdateListener(android.location.Location location) {

        if(location != null && lastLocation == null){
                lastLocation = location;
        }
        Log.d(TAG, "Distance to last location = " + location.distanceTo(lastLocation) );
        if(!locationPermission || location == null || location.distanceTo(lastLocation) < 1){

//            lastLocation = location;
            return;
        }
        Log.d("LOCATION!!!", "location: lat " + location.getLatitude() + " long " + location.getLongitude() + " Provider " + location.getProvider());
        lastLocation = location;
        Location loc = new Location(-1,location.getLatitude(),location.getLongitude(),null, System.currentTimeMillis());
        Log.d(TAG, "lon:" + loc.getLon() + " Lat: " + loc.getLat());
        Location.insertNewLocation(dbHelper, loc);
        locations = Location.getAll(dbHelper);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("TAG", "changing courser ...");
                location_adapter.changeCursor(locations);
            }
        });

    }

    public void setLocationPermission(boolean locationPermission) {
        this.locationPermission = locationPermission;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_PERM:
                requestCount--;
                if(requestCount == 0){
                    Toast.makeText(this, "Please enable location permission via system settings.", Toast.LENGTH_LONG);
                    System.exit(1);
                }
                Log.d("TAG", grantResults.length+"");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(MainActivity.this);
                    locationPermission = true;
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "We need your permission", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERM);
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;

            default:
                break;


            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        Runnable r = new MyThread(view);
//        new Thread(r).start();
        Double lat,lon;
        String address="";
        Log.d(TAG, "clicked position: " + position);
        Log.d(TAG, "clicked adapterview: " + parent.toString());
        Log.d(TAG, "clicked view: " + view.toString());
        Log.d(TAG, "clicked id: " + id);

        lat = Double.parseDouble(((TextView)view.findViewById(R.id.lbl_lat)).getText().toString());
        lon = Double.parseDouble(((TextView)view.findViewById(R.id.lbl_lng)).getText().toString());

        Log.d(TAG, "clicked lat: " + lat);
        Log.d(TAG, "clicked lon: " + lon);


        List<Address> listAddress = getFromLocation(lat,lon,1);
        Log.d(TAG, "listaddress size: "+ listAddress.size());
        Log.d(TAG, "listaddress this: "+ listAddress.toString());
        Log.d(TAG, "listaddress at 0 max line : "+ listAddress.get(0).getMaxAddressLineIndex());

        for (int i = 0; i <= listAddress.get(0).getMaxAddressLineIndex(); i++) {
            listAddress.get(0).getAddressLine(i);
            address += listAddress.get(0).getAddressLine(i)+" ";
        }
        Log.d(TAG, "listaddress at 0 full address : "+ address);
        address_tv.setText(address);


    }

    public class MyThread implements Runnable{
        View view;
        Double lat,lon;

        public MyThread(View view) {
            this.view = view;
        }
        @Override
        public void run() {
            String address="";


            lat = Double.parseDouble(((TextView)view.findViewById(R.id.lbl_lat)).getText().toString());
            lon = Double.parseDouble(((TextView)view.findViewById(R.id.lbl_lng)).getText().toString());

            Log.d(TAG, "clicked lat: " + lat);
            Log.d(TAG, "clicked lon: " + lon);


            List<Address> listAddress = getFromLocation(lat,lon,1);
            Log.d(TAG, "listaddress size: "+ listAddress.size());
            Log.d(TAG, "listaddress this: "+ listAddress.toString());
            Log.d(TAG, "listaddress at 0 max line : "+ listAddress.get(0).getMaxAddressLineIndex());

            for (int i = 0; i <= listAddress.get(0).getMaxAddressLineIndex(); i++) {
                listAddress.get(0).getAddressLine(i);
                address += listAddress.get(0).getAddressLine(i)+" ";
            }
            Log.d(TAG, "listaddress at 0 full address : "+ address);
            address_tv.setText(address);
        }
    }



}

