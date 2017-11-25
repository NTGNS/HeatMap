package com.example.c.heatmap;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import GpsFeaturing.GPS_Listener;
import WifiNetworksFeaturing.WifiReceiver;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    ArrayList<WeightedLatLng> dataPoints;
    ArrayList<LatLng> latLngData;
    ArrayList<Float> intensityData;
    GPS_Listener locationListener;

    WifiReceiver wifiReceiver;
    WifiManager wifiManager;
    final private int wifiNetworksScanInterval = 500;
    NetworksAndCoordStore networkAndCoordStore = new NetworksAndCoordStore();

    private Handler mHandler;
    private FusedLocationProviderClient mFusedLocationClient;

    final Button scanWifiNetworksButton = findViewById(R.id.wyszukajSieciButton);
    final Button debugButton = findViewById(R.id.debugButton);
    final Spinner networksListSpinner = findViewById(R.id.listaDostepnychSieci);
    final Vector<String> scannedWifiNetworksVector = new Vector<String>();


    public MapsActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        locationListener = new GPS_Listener(getApplicationContext());
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        wifiConfigure();                    //configuration of turning wifi on, scanning networks etc.
        mHandler = new Handler();
        startRepeatingNetworkScan();
        networksListSpinnerConfigure();     //configuration of spinner list with wifi networks names: its adapter, layout etc.
        buttonsConfigure();                 //configuration of buttons on layout
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

        if (latLngData.size() > 0) {
            LatLng start = new LatLng(latLngData.get(latLngData.size() - 1).latitude, latLngData.get(latLngData.size() - 1).longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(start));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start, 12.0f));
            createHeatMap();
        }

    }

    public void zoomOnPoint(double latitude, double longitude, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom));
    }

    public void addPointToData(double latitude, double longitude, double intensity) {
        dataPoints.add(new WeightedLatLng(new LatLng(latitude, longitude), intensity));
    }

    public void createHeatMap() {
        for (int i = 0; i < latLngData.size(); i++) {
            addPointToData(latLngData.get(i).latitude, latLngData.get(i).longitude, intensityData.get(i).floatValue());
        }
        mProvider = new HeatmapTileProvider.Builder().weightedData(dataPoints).build();
        // Add a tile overlay to the map, using the heat map tile provider.s
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void removeHeatMap() {
        mOverlay.remove();
    }


    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(wifiReceiver);
        } catch (IllegalArgumentException ex) {

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
                wifiManager.startScan();
            } finally {
                mHandler.postDelayed(mStatusChecker, wifiNetworksScanInterval);
            }
        }
    };

    void startRepeatingNetworkScan() {
        mStatusChecker.run();
    }

    void stopRepeatingNetworkScan() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    void wifiConfigure(){
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifiManager.getWifiState() == WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
        }
        wifiReceiver = new WifiReceiver(wifiManager);
        registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //WifiManager.WIFI_STATE_DISABLING
    }

    void networksListSpinnerConfigure(){

        if (wifiReceiver.getScanResults().size() == 0) {
            //dodajemy opcje "puste". Zapytacie po co? Bo jak nie ma żadnego elementu przy tworzeniu to nie
            // można wybrać żadnej opcji (wyświetla się, ale się nie wybiera)
            //UPDATE: i tak nie można wybrać pierwszej opcji po wyskanowaniu sieci
            //TODO
            //i think its possible i just dunno how to do it now
            scannedWifiNetworksVector.add("<puste>");
        } else {
            for (ScanResult sr : wifiReceiver.getScanResults()) {
                scannedWifiNetworksVector.add(sr.SSID);
            }
        }
        //spiner
        networksListSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_layout, scannedWifiNetworksVector));

        networksListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int numer, long l) {
                latLngData.addAll(networkAndCoordStore.getCoordsForESSID(scannedWifiNetworksVector.get(numer)));
                intensityData.addAll(networkAndCoordStore.getLevelForESSID(scannedWifiNetworksVector.get(numer)));
                for (int i = 0; i < latLngData.size(); i++) {
                    addPointToData(latLngData.get(i).latitude, latLngData.get(i).longitude, intensityData.get(i).floatValue());
                }
                mProvider.setWeightedData(dataPoints);
                if (mOverlay != null)
                    mOverlay.clearTileCache();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

    }

    void buttonsConfigure(){
        //button
        scanWifiNetworksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannedWifiNetworksVector.clear();
                List<ScanResult> listOfNetworks = wifiReceiver.getScanResults();
                for (ScanResult sr : listOfNetworks) {
                    scannedWifiNetworksVector.add(sr.SSID);
                }
                Double latitude = null;
                Double longitude = null;

                try {
                    latitude = locationListener.getLatitude();
                    longitude = locationListener.getLongitude();
                } catch (Exception ex) {
                    Toast.makeText(v.getContext(), "Problem: " + ex.toString(), Toast.LENGTH_SHORT).show();
                }

                networkAndCoordStore.addScanResults(listOfNetworks, latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 21.0f));

                Toast.makeText(v.getContext(), "latitude: " + latitude + "\nlongitude: " + longitude, Toast.LENGTH_SHORT).show();

                //networkAndCoordStore here should be stored location and networks


            }
            //example ends here

        });

        debugButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), locationListener.getStatusLog(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

