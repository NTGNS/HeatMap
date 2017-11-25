package com.example.c.heatmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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
    NetworksAndCoordStore networkAndCoordStore = new NetworksAndCoordStore();
    private int mInterval = 500;
    private Handler mHandler;
    private FusedLocationProviderClient mFusedLocationClient;

    LatLng lastPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setLastPosition();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //create data array list(for heatmap points)
        dataPoints = new ArrayList<WeightedLatLng>();


        latLngData = new ArrayList<>();
        latLngData.add(new LatLng(30, 40));
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
        if (wifiReceiver.getScanResults().size() == 0) {
            dostepneSieciVector.add("<puste>");
            //dodajemy opcje "puste". Zapytacie po co? Bo jak nie ma żaanego elementu przy tworzeniu to nie
        }    // można wybrać żadnej opcji (wyświetla się, ale się nie wybiera)
        else {
            for (ScanResult sr : wifiReceiver.getScanResults()) {
                dostepneSieciVector.add(sr.SSID);
            }
        }
        //spiner
        //ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dostepneSieciVector); //stare śmieci
        ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, dostepneSieciVector);    //dzięki temu możemy modyfikować wygląd czcionki (plik: spinner_layout.xml z folderu layout)
        final Spinner dostepneSieciSpinner = (Spinner) findViewById(R.id.listaDostepnychSieci);
        dostepneSieciSpinner.setAdapter(dostepneSieciAdapter);

        dostepneSieciSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {           //co się stanie gdy wybierzemy...
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int numer, long l) {          //...element z listy

                //poniższe jest bez sensu bo nie wyświetla dla pierwszego elementu - nigdy?
                if (numer != 0) {        //po prostu by nie wyrzucało informacji na początku aplikacji o "wybraniu" jakiejś opcji
                    Toast.makeText(getApplicationContext(), "Wybrano " + "element numer: " + (numer - 1), Toast.LENGTH_LONG).show();
                    latLngData.addAll(networkAndCoordStore.getCoordsForESSID(dostepneSieciVector.get(numer)));
                    intensityData.addAll(networkAndCoordStore.getLevelForESSID(dostepneSieciVector.get(numer)));

                    //latLngData.add(new LatLng(50.297158, 18.686032));     //it is working so everything should work fine
                    //intensityData.add(new Float(0.5f));                  //it is working so everything should work fine

                    for (int i = 0; i < latLngData.size(); i++) {
                        addPointToData(latLngData.get(i).latitude, latLngData.get(i).longitude, intensityData.get(i).floatValue());
                    }
                    mProvider.setWeightedData(dataPoints);
                    if (mOverlay != null)
                        mOverlay.clearTileCache();


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
                List<ScanResult> listOfNetworks = wifiReceiver.getScanResults();
                for (ScanResult sr : listOfNetworks) {
                    dostepneSieciVector.add(sr.SSID);
                }

                LocationManager locationManager = (LocationManager) getSystemService(getBaseContext().LOCATION_SERVICE);


                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(v.getContext(), "No permission to GPS", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                        Double latitude = locationListener.getLatitude();
                        Double longitude = locationListener.getLongitude();
                        Toast.makeText(v.getContext(), "latitude" + latitude + " longitude " + longitude, Toast.LENGTH_SHORT).show();

                        if (latitude != null && longitude != null) {
                            networkAndCoordStore.addScanResults(listOfNetworks, latitude, longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12.0f));
                        }else{
                            networkAndCoordStore.addScanResults(listOfNetworks, lastPosition.latitude, lastPosition.longitude);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastPosition.latitude, lastPosition.longitude)));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastPosition.latitude, lastPosition.longitude), 20.0f));
                        }
                    } catch (Exception ex) {
                        Toast.makeText(v.getContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
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
        // Add a tile overlay to the map, using the heat map tile provider.s
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
    //możliwe że po debugu się nigdy więcej nie przyda, ale narazie używam ostatnich coordów i mam to w dupie. Alek
    void setLastPosition(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lastPosition = (new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    }
                });
    }



}

