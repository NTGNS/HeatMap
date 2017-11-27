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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import GpsFeaturing.GPS_Listener;
import WifiNetworksFeaturing.WifiReceiver;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    //ArrayList<WeightedLatLng> dataPoints;
    //ArrayList<LatLng> latLngData = new ArrayList<>();
    //ArrayList<Double> intensityData = new ArrayList<Double>();
    GPS_Listener locationListener;

    WifiReceiver wifiReceiver;
    WifiManager wifiManager;
    final private int wifiNetworksScanInterval = 500;
    NetworksAndCoordStore networkAndCoordStore = new NetworksAndCoordStore();

    private Handler mHandler;
    private FusedLocationProviderClient mFusedLocationClient;

    Button scanWifiNetworksButton;
    Button debugButton;
    Spinner networksListSpinner;
    Vector<String> scannedWifiNetworksVector = new Vector<String>();


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

//        dataPoints = new ArrayList<WeightedLatLng>();
//        latLngData.add(new LatLng(30, 40));
//        intensityData.add(new Double(0.5f));
//        latLngData.add(new LatLng(30.001, 40));
//        intensityData.add(new Double(0.5f));

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
        mProvider = new HeatmapTileProvider.Builder().weightedData(networkAndCoordStore.getWeightedLatLngForSSID("")).build();
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

//        List<WeightedLatLng> listOfNetworks = networkAndCoordStore.getWeightedLatLngForSSID(scannedWifiNetworksVector.get(0));
//        if (listOfNetworks.size() > 0) {
//            LatLng startPoint = new LatLng(listOfNetworks.get(0).getPoint().x, listOfNetworks.get(0).getPoint().y);
//            zoomOnPoint(startPoint, 18.0f);
//        }


    }

    public void zoomOnPoint(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }


    public void createHeatMapForESSID(String SSID) {

        if (!networkAndCoordStore.getWeightedLatLngForSSID(SSID).isEmpty()) {
            try {
                mProvider.setWeightedData(networkAndCoordStore.getWeightedLatLngForSSID(SSID));
                mOverlay.clearTileCache();
            } catch (NullPointerException ex) {
                //mProvider and mOverlay could be nulls if its before OnMapReady()
            }
        }
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

    void wifiConfigure() {
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager.getWifiState() == WIFI_STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        }
        wifiReceiver = new WifiReceiver(wifiManager);
        registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //WifiManager.WIFI_STATE_DISABLING
    }

    void networksListSpinnerConfigure() {
        //find spinner
        networksListSpinner = findViewById(R.id.listaDostepnychSieci);

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
                createHeatMapForESSID(scannedWifiNetworksVector.get(numer));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //do nothing
            }
        });

    }

    void buttonsConfigure() {
        //find buttons
        scanWifiNetworksButton = findViewById(R.id.wyszukajSieciButton);
        debugButton = findViewById(R.id.debugButton);
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

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 21.0f));
                //here coordinates and networks are stored:
                networkAndCoordStore.addScanResults(listOfNetworks, latitude, longitude);

                createHeatMapForESSID(networksListSpinner.getSelectedItem().toString());

                Toast.makeText(v.getContext(), "latitude: " + latitude + "\nlongitude: " + longitude, Toast.LENGTH_SHORT).show();
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

