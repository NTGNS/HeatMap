package com.example.c.heatmap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Vector;

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
        //locationListener.GetGPSPermissions(this);

        //vektor elementów
        final Vector<String> dostepneSieciVector;
        dostepneSieciVector = new Vector<String>();
        dostepneSieciVector.add("<puste>");     //dodajemy opcje "puste". Zapytacie po co? Bo jak nie ma żaanego elementu przy tworzeniu to nie
                                                //można wybrać żadnej opcji (wyświetla się, ale się nie wybiera)

        //spiner
        //ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dostepneSieciVector); //stare śmieci
        ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout,dostepneSieciVector);    //dzięki temu możemy modyfikować wygląd czcionki (plik: spinner_layout.xml z folderu layout)
        final Spinner dostepneSieciSpinner = (Spinner) findViewById(R.id.listaDostepnychSieci);
        dostepneSieciSpinner.setAdapter(dostepneSieciAdapter);

        dostepneSieciSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {           //co się stanie gdy wybierzemy...
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int numer, long l) {          //...element z listy


                if(numer != 0) {        //po prostu by nie wyrzucało informacji na początku aplikacji o "wybraniu" jakiejś opcji
                    Toast.makeText(getApplicationContext(), "Wybrano " + "element numer: " + (numer - 1), Toast.LENGTH_LONG).show();
                    //W tym miejscu jest wywoływana metoda (Intencja) Kondzia... trzeba jej wcisnąć jakoś numer wi-fi z listy
                    //Przekazywanie nazwy wi-fi do nowej aktywności (kondziu-part)
                    Intent kondziuJakimsCudemMaIntencje = new Intent();
                    //kondziuJakimsCudemMaIntencje.setClass(MainActivity.this, OtherActivity.class);
                    kondziuJakimsCudemMaIntencje.putExtra("nazwaSieci", dostepneSieciVector.elementAt(numer));
                    //startActivity(kondziuJakimsCudemMaIntencje);
                    //tutaj jest juz nowa aktywność
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {                                     //...nic (nie wiem jak to zrobić, ale podobno jest i taka opcja)
                Toast.makeText(getApplicationContext(), "Nie wybrano niczego.", Toast.LENGTH_SHORT).show();
            }
        });
        //button
        Button sprawdzSieci = (Button) findViewById(R.id.wyszukajSieciButton);
        sprawdzSieci.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                 //co się stanie gdy naciśniemy przycisk
               /* Toast.makeText(getApplicationContext(), "Wyszukuję dostepnych sieci...", Toast.LENGTH_SHORT).show();
                //W tym miejscu otrzymujemy listę/tablicę/chuj-wie-co zawierające nazwy dostępnych sieci.
                int iloscSieci = 5;
                if (dostepneSieciVector.size() > 1) {                        //jeśli w vectorze istnieją już jakies elementy (naciskamy po raz drugi+ przycisk)
                    for (int i = dostepneSieciVector.size(); i > 1; i--) {  //to trzeba "odświeżyć" listę dostepnych sieci -> usunąć poprzednie i dodać wszystkie raz jeszcze.
                        dostepneSieciVector.remove(i - 1);              //ALE pozostawiamy element "zerowy", aby była opcja "puste" -> "nie dokonaj wyboru"
                    }
                }
                    for (int i = 0; i < iloscSieci; i++) {                        //dodajemy spis wszystkich wykrytych sieci wi-fi (numery elementów w vectorze: 1-(n+1),
                        dostepneSieciVector.add("Opcja numer: " + i);           //gdzie "n" to liczba elementów (element zerowy to element "<puste>"
                    }
               */
                LocationManager locationManager = (LocationManager)getSystemService(getBaseContext().LOCATION_SERVICE);


                if (ActivityCompat.checkSelfPermission(v.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(v.getContext(),"No permission to GPS", Toast.LENGTH_SHORT).show() ;
                }
                else
                {
                    try
                    {
                        locationListener.measure(locationManager);
                        Toast.makeText(v.getContext(),"latitude"+locationListener.getLatitude()+" longitude " + locationListener.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                    catch(SecurityException ex)
                    {
                        Toast.makeText(v.getContext(),"Acces to GPS unauthorized", Toast.LENGTH_SHORT).show();
                    }catch(Exception ex)
                    {
                        Toast.makeText(v.getContext(),"No GPS or GSM", Toast.LENGTH_SHORT).show();
                    }

                }
                //example ends here
            }
        });
    }
}

/*
* //this code is a sample of obtaining the GPS coords
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
* */