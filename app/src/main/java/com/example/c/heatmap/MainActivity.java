package com.example.c.heatmap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import GPS_Featuring.GPS_Listener;

public class MainActivity extends AppCompatActivity {

    GPS_Listener locationListener = new GPS_Listener(getBaseContext());
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///Very important. This part of code requires addition of 'com.karumi:dexter:4.2.0 to build.gradle doc
        ///it asks user for GPS usage permission
        locationListener.GetGPSPermissions(this);


        final Button button = findViewById(R.id.button);



        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //this code is a sample of obtaining the GPS coords
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
}