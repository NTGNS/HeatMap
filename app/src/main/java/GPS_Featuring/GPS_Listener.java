package GPS_Featuring;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * Created by c on 11/24/17.
 */

public class GPS_Listener implements LocationListener
{
    Context context;

    Double longitude;
    public Double getLongitude()
    {
        return longitude;
    }

    Double latitude;
    public Double getLatitude()
    {
        return latitude;
    }

    public void GetGPSPermissions(Activity activity)
    {
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

    public GPS_Listener(Context con)
    {
        this.context=con;
    }
    
    @Override
    public void onLocationChanged(Location loc)
    {
        this.longitude = loc.getLongitude();
        //Log.v(TAG, longitude);
        this.latitude =  loc.getLatitude();
        //Log.v(TAG, latitude);
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}


}
