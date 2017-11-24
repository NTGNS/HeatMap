package com.example.c.heatmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.DoubleToLongFunction;

public class MainActivity extends AppCompatActivity {

    ArrayList<LatLng> data = new ArrayList<LatLng>();
    ArrayList<Float> intensity = new ArrayList<Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data.add(new LatLng(30,40));
        intensity.add(new Float(0.5f));
        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent i = new Intent(getApplicationContext(),MapsActivity.class);
                i.putParcelableArrayListExtra("latLngData",data);
                i.putExtra("intensityData",intensity);
                startActivity(i);

            }
        });
    }
}
