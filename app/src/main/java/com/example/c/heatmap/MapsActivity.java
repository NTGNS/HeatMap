package com.example.c.heatmap;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    HeatmapTileProvider mProvider;
    TileOverlay mOverlay;
    ArrayList<WeightedLatLng> dataPoints;
    ArrayList<LatLng> latLngData;
    ArrayList<Float> intensityData;
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
        latLngData = getIntent().getParcelableArrayListExtra("latLngData");

        intensityData = new ArrayList<Float>();
        intensityData = (ArrayList<Float>)getIntent().getSerializableExtra("intensityData");



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
            LatLng start = new LatLng(latLngData.get(0).latitude,latLngData.get(0).longitude);
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

}
