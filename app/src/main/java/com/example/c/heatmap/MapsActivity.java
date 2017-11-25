package com.example.c.heatmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.Console;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import GPS_Featuring.GPS_Listener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    ArrayList<WeightedLatLng> dataPoints;
    ArrayList<LatLng> latLngData;
    ArrayList<Float> intensityData;
    GPS_Listener locationListener = new GPS_Listener(getBaseContext());
    WifiReceiver wifiReceiver;
    WifiFinder wifiFinder;
    private int mInterval = 500; // 5 seconds by default, can be changed laterz
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //create data array list(for heatmap points)
        dataPoints = new ArrayList<WeightedLatLng>();


        latLngData = new ArrayList<>();
            latLngData.add(new LatLng(30,40));
        intensityData = new ArrayList<Float>();
            intensityData.add(new Float(0.5f));

        //sieci wifi
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        wifiReceiver = new WifiReceiver(wifiManager);
        registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //WifiManager.WIFI_STATE_DISABLING

        mHandler = new Handler();
        wifiFinder = new WifiFinder(wifiManager);
        startRepeatingNetworkScan();

        //sieci wifi - koniec

        //wektor elementów
        final Vector<String> dostepneSieciVector;
        dostepneSieciVector = new Vector<String>();
        if(wifiReceiver.getScanResults().size()==0) {
            dostepneSieciVector.add("<puste>");
            //dodajemy opcje "puste". Zapytacie po co? Bo jak nie ma żaanego elementu przy tworzeniu to nie
        }    // można wybrać żadnej opcji (wyświetla się, ale się nie wybiera)
        else {
            for (ScanResult sr : wifiReceiver.getScanResults()) {
                dostepneSieciVector.add(sr.SSID+": "+sr.level);
            }
        }
        //spiner
        //ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dostepneSieciVector); //stare śmieci
        ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout,dostepneSieciVector);    //dzięki temu możemy modyfikować wygląd czcionki (plik: spinner_layout.xml z folderu layout)
        final Spinner dostepneSieciSpinner = (Spinner) findViewById(R.id.listaDostepnychSieci);
        dostepneSieciSpinner.setAdapter(dostepneSieciAdapter);

        dostepneSieciSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {           //co się stanie gdy wybierzemy...
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int numer, long l) {          //...element z listy

                if(numer != 0) {        //po prostu by nie wyrzucało informacji na początku aplikacji o "wybraniu" jakiejś opcji
                    Toast.makeText(getApplicationContext(), "Wybrano " + "element numer: " + (numer - 1), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {                                     //...nic (nie wiem jak to zrobić, ale podobno jest i taka opcja)
                Toast.makeText(getApplicationContext(), "Nie wybrano niczego.", Toast.LENGTH_SHORT).show();
            }
        });

//button
        Button sprawdzSieci = (Button) findViewById(R.id.wyszukajSieciButton);
        sprawdzSieci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//co się stanie gdy naciśniemy przycisk
                if (dostepneSieciVector.size() > 0) {                        //jeśli w vectorze istnieją już jakies elementy (naciskamy po raz drugi+ przycisk)
                    // for (int i = dostepneSieciVector.size(); i >1; i--) {  //to trzeba "odświeżyć" listę dostepnych sieci -> usunąć poprzednie i dodać wszystkie raz jeszcze.
                    //     dostepneSieciVector.remove(i-1);              //ALE pozostawiamy element "zerowy", aby była opcja "puste" -> "nie dokonaj wyboru"
                    // }
                    dostepneSieciVector.clear();
                }
                for(ScanResult sr : wifiReceiver.getScanResults()){
                    dostepneSieciVector.add(sr.SSID+": "+sr.level);
                }

                LocationManager locationManager = (LocationManager)getSystemService(getBaseContext().LOCATION_SERVICE);


                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(v.getContext(),"No permission to GPS", Toast.LENGTH_SHORT).show() ;
                }
                else
                {
                    try
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                        Toast.makeText(v.getContext(),"latitude"+locationListener.getLatitude()+" longitude " + locationListener.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                    catch(Exception ex)
                    {
                        Toast.makeText(v.getContext(),ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                //example ends here
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (latLngData.size() > 0 ){
            LatLng start = new LatLng(latLngData.get(0).latitude, latLngData.get(0).longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 12.0f));
            createHeatMap();
        }

    }
    public void zoomOnPoint(double latitude, double longitude, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude,longitude)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,longitude),zoom));
    }
    public void addPointToData(double latitude, double longitude, double intensity){
        dataPoints.add(new WeightedLatLng(new LatLng(latitude,longitude),intensity));
    }
    public void createHeatMap(){
        for (int i=0; i<latLngData.size(); i++){
            addPointToData(latLngData.get(i).latitude,latLngData.get(i).longitude,intensityData.get(i).floatValue());
        }
       mProvider = new HeatmapTileProvider.Builder().weightedData(dataPoints).build();
        // Add a tile overlay to the map, using the heat map tile provider.
       mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void removeHeatMap(){
        mOverlay.remove();
    }


    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(wifiReceiver);
        }catch(IllegalArgumentException ex){

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingNetworkScan();
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                wifiFinder.scanForNetworks();
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingNetworkScan() {
        mStatusChecker.run();
    }

    void stopRepeatingNetworkScan() {
        mHandler.removeCallbacks(mStatusChecker);
    }


}

