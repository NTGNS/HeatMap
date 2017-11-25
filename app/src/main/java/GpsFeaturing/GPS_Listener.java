package GpsFeaturing;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * Created by c on 11/24/17.
 */

public class GPS_Listener implements LocationListener {
    Context context;
    Double longitude = null; //maybe should be set to last known location?
    Double latitude = null;  //maybe should be set to last know location?
    String statusLog = "";

    public GPS_Listener(Context context) {
        this.context = context;
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            GetGPSPermissions((Activity) context);
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, this);

    }


    public void GetGPSPermissions(Activity activity) {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            PermissionListener dialogPermissionListener =
                    DialogOnDeniedPermissionListener.Builder
                            .withContext(context)
                            .withTitle("GPS permission")
                            .withMessage("It's necessary for correct heatmap generation.")
                            .withButtonText(android.R.string.ok)
                            .build();

            Dexter.withActivity(activity)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(dialogPermissionListener)
                    .check();
        }
    }


    public Double getLongitude() throws Exception {
        if (this.longitude == null || this.latitude == null)
            throw new Exception("No GPS and GSM positioning available");
        return longitude;
    }

    public Double getLatitude() throws Exception {
        if (this.longitude == null || this.latitude == null)
            throw new Exception("No GPS and GSM positioning available");
        return latitude;
    }

    public String getStatusLog() {
        return statusLog;
    }


    @Override
    public void onLocationChanged(Location loc) {
        statusLog += "location changed from: "+loc.getProvider()+"\n";
        this.latitude = loc.getLatitude();
        this.longitude = loc.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        statusLog += provider + ": disabled\n";
    }

    @Override
    public void onProviderEnabled(String provider) {
        statusLog += provider + ": enabled\n";
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        statusLog += provider + ": " + status + "\n";
        /*
            int:
            OUT_OF_SERVICE 0
            TEMPORARILY_UNAVAILABLE 1
            AVAILABLE 2
         */
    }


}
