package com.example.c.heatmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Ten komentarz mieć winieneś
        //Ten komentarz mieć winieneś2


        //Część Jurczyka

        //vektor elementów
        final Vector<String> dostepneSieciVector;
        dostepneSieciVector = new Vector<String>();
        dostepneSieciVector.add("<puste>");

        //spiner
        ArrayAdapter<String> dostepneSieciAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dostepneSieciVector);
        final Spinner dostepneSieciSpinner = (Spinner) findViewById(R.id.listaDostepnychSieci);
        dostepneSieciSpinner.setAdapter(dostepneSieciAdapter);

        dostepneSieciSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int numer, long l) {
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
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(getApplicationContext(), "Nie wybrano niczego.", Toast.LENGTH_SHORT).show();
            }
        });

        //button
        Button sprawdzSieci = (Button) findViewById(R.id.wyszukajSieciButton);
        sprawdzSieci.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0){
                Toast.makeText(getApplicationContext(), "Wyszukuję dostepnych sieci...", Toast.LENGTH_SHORT).show();
                //W tym miejscu otrzymujemy listę/tablicę/chuj-wie-co zawierające nazwy dostępnych sieci.
                int iloscSieci = 5;
                if(dostepneSieciVector.size() > 1) {
                    for (int i = dostepneSieciVector.size(); i > 1; i--) {
                        dostepneSieciVector.remove(i-1);
                    }
                }
                for(int i = 0; i < iloscSieci; i++){
                    dostepneSieciVector.add("Opcja numer: " + i);
                }
            }


        });


    }
}
