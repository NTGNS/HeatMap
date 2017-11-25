package com.example.c.heatmap;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleks on 25.11.2017.
 */

public class NetworksAndCoordStore {
    List<Storage> storageList = new ArrayList<>();

    void addScanResults(List<ScanResult> wifiScanList, Double latitude, Double longitude){
        storageList.add(new Storage(wifiScanList, latitude, longitude));
    }
    

    class Storage{

        List<ScanResult> wifiDataList;
        Double latitude;
        Double longitude;

        Storage(List<ScanResult> wifiScanList, Double latitude, Double longitude){
            this.wifiDataList = wifiScanList;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public List<ScanResult> wifiDataList() {
            return wifiDataList;
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }

    }

}
