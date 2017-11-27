package com.example.c.heatmap;

import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by aleks on 25.11.2017.
 */

public class NetworksAndCoordStore {
    private Map<LatLng, List<ScanResult>> storageMap = new HashMap<>();

    public void addScanResults(List<ScanResult> wifiScanList, Double latitude, Double longitude) {
        //it's replacing wifiScanList, not updating as it probably should
        storageMap.put(new LatLng(latitude, longitude), wifiScanList);
        /*
        TODO
            - before "put" we should check if something is at given LatLng,
            add networks if they aren't on ScanResult list and update levels
            (average from wifi levels - average from level and minimal value if network isn't on one list)
         */
    }

    //I don't think it's necessary anymore or used anywhere
    @Deprecated
    public List<LatLng> getCoordsForSSID(String SSID) {
        List<LatLng> list = new ArrayList<>();
        for (Map.Entry<LatLng, List<ScanResult>> entry : storageMap.entrySet()) {
            if (getIndexOfSSIDInNetworksList(entry.getValue(), SSID) != -1)
                list.add(entry.getKey());
        }
        return list;
    }

    //I don't think it's necessary anymore or used anywhere
    @Deprecated
    public List<Double> getLevelForSSID(String SSID) {
        List<Double> list = new ArrayList<>();
        for (Map.Entry<LatLng, List<ScanResult>> entry : storageMap.entrySet()) {
            int index = getIndexOfSSIDInNetworksList(entry.getValue(), SSID);
            if (index != -1)
                list.add((double)entry.getValue().get(index).level);
        }
        return list;
    }

    public List<WeightedLatLng> getWeightedLatLngForSSID(String SSID) {
        List<WeightedLatLng> list = new ArrayList<>();
        if(!SSID.isEmpty()) {
            for (Map.Entry<LatLng, List<ScanResult>> entry : storageMap.entrySet()) {
                int index = getIndexOfSSIDInNetworksList(entry.getValue(), SSID);
                if (index != -1)
                    list.add(new WeightedLatLng(entry.getKey(), Math.abs(entry.getValue().get(index).level)));
            }
        }else{
            list.add(new WeightedLatLng(new LatLng(30, 40), 100));
        }
        return list;
    }



    public Map<LatLng, List<ScanResult>> getStorageMap(){
        return storageMap;
    }


    private int getIndexOfSSIDInNetworksList(List<ScanResult> wifiNetworksList, String SSID) {
        for (int i = 0; i < wifiNetworksList.size(); i++) {
            if (wifiNetworksList.get(i).SSID.equals(SSID))
                return i;
        }
        return -1;
    }

}
