package com.example.c.heatmap;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.net.wifi.WifiManager.*;

/**
 * Created by phaun on 24.11.2017.
 */

class WifiFinder {

    private WifiManager wifiManager;

    WifiFinder(WifiManager wifiManager){
        this.wifiManager = wifiManager;
        if(wifiManager.getWifiState() == WIFI_STATE_DISABLED){
            wifiManager.setWifiEnabled(true);
        }
    }

    public void scanForNetworks(){
        wifiManager.startScan();
    }





}

