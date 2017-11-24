package com.example.c.heatmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleks on 24.11.2017.
 */

public class WifiReceiver extends BroadcastReceiver {
    WifiManager wifiManager;
    List<ScanResult> wifiScanList = new ArrayList<>();


    public WifiReceiver(WifiManager wifiManager){
        super();
        this.wifiManager = wifiManager;
        wifiScanList.clear();
        wifiScanList.addAll(wifiManager.getScanResults());
    }

    public void onReceive(Context c, Intent intent) {
        wifiScanList.clear();
        wifiScanList.addAll(wifiManager.getScanResults());

    }

    List<ScanResult> getScanResults(){
        return wifiScanList;
    }

}