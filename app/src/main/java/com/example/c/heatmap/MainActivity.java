package com.example.c.heatmap;

import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    WifiReceiver wifiReceiver;
    WifiFinder wifiFinder;
    private int mInterval = 500; // 5 seconds by default, can be changed later
    private Handler mHandler;

    Button but;//remove it when merge

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Ten komentarz mieć winieneś
        //Ten komentarz mieć winieneś2

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);

        wifiReceiver = new WifiReceiver(wifiManager);
        registerReceiver(wifiReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //WifiManager.WIFI_STATE_DISABLING

        mHandler = new Handler();
        wifiFinder = new WifiFinder(wifiManager);
        startRepeatingNetworkScan();

        but  = findViewById(R.id.button);;
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String networksListSummarized = "";
                    for(ScanResult sr : wifiReceiver.getScanResults()){
                        networksListSummarized  += sr.SSID+"\n";
                    }
                    Toast.makeText(getApplicationContext(), networksListSummarized, Toast.LENGTH_SHORT).show();
                }
                catch(IndexOutOfBoundsException ex){
                    Toast.makeText(getApplicationContext(), "pusta lista", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingNetworkScan();
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                wifiFinder.scanForNetworks();
            } finally {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingNetworkScan() {
        mStatusChecker.run();
    }

    void stopRepeatingNetworkScan() {
        mHandler.removeCallbacks(mStatusChecker);
    }


}
