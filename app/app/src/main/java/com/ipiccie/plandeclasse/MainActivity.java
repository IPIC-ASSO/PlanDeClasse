package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private LayoutInflater inflater;
    private SharedPreferences prefs;
    private String[] classes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = this.getLayoutInflater();
        prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        obtienClasse0();
        inflation0(classes);

        com.google.android.material.floatingactionbutton.FloatingActionButton bouton1 = findViewById(R.id.nouv_classe);
        bouton1.setOnClickListener(v -> {
            Intent intention = new Intent(this, NouvelleClasse.class);
            intention.putExtra("classe","-1");
            startActivity(intention);
        });


    }

    public void obtienClasse0(){
        String savedString = prefs.getString("liste_classes", "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        classes = new String[st.countTokens()];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = st.nextToken();
        }
        Log.d(TAG, "obtienClasse"+classes.length);
    }

    public void inflation0(String[] classes){
        LinearLayout liste = findViewById(R.id.liste_classes);

        for (String classe: classes){
            View vue = inflater.inflate(R.layout.profil_classe, null);
            TextView nom = vue.findViewById(R.id.nom_classe);
            TextView sup = vue.findViewById(R.id.commentaires_classe);
            TextView nb = vue.findViewById(R.id.nombre_eleves);
            nom.setText("classe: "+classe);
            sup.setText(prefs.getString(classe,"inconnu au bataillon"));
            SharedPreferences listeEleves= getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);
            StringTokenizer st = new StringTokenizer(listeEleves.getString(classe,""), ",");
            nb.setText("élèves: "+st.countTokens());
            vue.setOnClickListener(v -> {
                Intent intention = new Intent(this, NouvelleClasse.class);
                intention.putExtra("classe",classe);
                startActivity(intention);
            });
            liste.addView(vue);
        }
    }
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: "+prefs.getAll());
        LinearLayout liste = findViewById(R.id.liste_classes);
        liste.removeAllViews();
        obtienClasse0();
        inflation0(classes);
        super.onResume();
    }
}