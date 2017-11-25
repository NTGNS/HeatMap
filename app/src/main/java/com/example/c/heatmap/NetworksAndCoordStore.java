package com.example.c.heatmap;

import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;

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

    List<LatLng> getCoordsForESSID(String ESSID){
        List<LatLng> list = new ArrayList<>();
        for(Storage s : storageList){
            for(ScanResult sr : s.getWifiDataList()){
                if(sr.SSID.equals(ESSID)){
                    list.add(new LatLng(s.getLatitude(), s.getLongitude()));
                }
            }
        }
        return list;
    }

    List<Float> getLevelForESSID(String ESSID){
        List<Float> list = new ArrayList<>();
        for(Storage s : storageList){
          //  Integer maxLevel=-500;//this is returned as negative number!!! probably... and max -100
            for(ScanResult sr : s.getWifiDataList()){
                if(sr.SSID.equals(ESSID)){
                    list.add((new Integer(sr.level).floatValue()));
                  //  if(maxLevel>sr.level)
                        //maxLevel = sr.level;
                }
            }
           // if(maxLevel>-500)
           //     list.add((maxLevel.floatValue()));
        }
        return list;
    }

    class Three{

    }

    class Storage{

        private List<ScanResult> wifiDataList;
        private Double latitude;
        private Double longitude;

        Storage(List<ScanResult> wifiScanList, Double latitude, Double longitude){
            this.wifiDataList = wifiScanList;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public List<ScanResult> getWifiDataList() {
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
