package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;

public class NouvelleClasse extends AppCompatActivity {

    private GridView grille;
    private int[] places;
    private SharedPreferences prefs;
    private SharedPreferences config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generateur_classe);
        prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        Log.d(TAG, "onCreate: preferences"+prefs.getAll());
        Log.d(TAG, "onCreate: preferences"+config.getAll());
        Button appli = findViewById(R.id.appliquer);
        Button enregistre = findViewById(R.id.enregistrer_salle);
        final EditText colonne = findViewById(R.id.colonnes);
        final EditText rang = findViewById(R.id.rangees);
        final EditText nomDeClasse = findViewById(R.id.nom_classe);
        final EditText commentaires = findViewById(R.id.commentaires_classe_txt);
        grille = findViewById(R.id.grille);
        if (!Objects.equals(getIntent().getStringExtra("classe"), "-1")){
            nomDeClasse.setText(getIntent().getStringExtra("classe"));
            commentaires.setText(prefs.getString(nomDeClasse.getText().toString(),""));
            StringTokenizer st = new StringTokenizer(config.getString(getIntent().getStringExtra("classe"),""), ",");
            places = new int[st.countTokens() -1];
            int x = st.countTokens() -1;
            for (int i=0; i<x; i++){
                places[i] = Integer.parseInt(st.nextToken());
            }
            colonne.setText(st.nextToken());
            rang.setText(String.valueOf(places.length/Integer.parseInt(colonne.getText().toString())));
            grille.setNumColumns(Integer.parseInt(colonne.getText().toString()));
            grille.setLayoutParams(new LinearLayout.LayoutParams(Integer.parseInt(colonne.getText().toString())*40, Integer.parseInt(rang.getText().toString())*40));
            AdaptateurAdapte customAdapter = new AdaptateurAdapte(getApplicationContext(), places);
            grille.setAdapter(customAdapter);
            grille.setVisibility(View.VISIBLE);
            findViewById(R.id.enregistrer_salle).setVisibility(View.VISIBLE);
            findViewById(R.id.txt_tableau).setVisibility(View.VISIBLE);

        }
        appli.setOnClickListener(w -> {
            if (!colonne.getText().toString().equals("") && !rang.getText().toString().equals("") &&!nomDeClasse.getText().toString().equals("")){
                places = new int[Integer.parseInt(colonne.getText().toString())*Integer.parseInt(rang.getText().toString())];
                Arrays.fill(places, 0);
                grille.setNumColumns(Integer.parseInt(colonne.getText().toString()));
                grille.setLayoutParams(new LinearLayout.LayoutParams(Integer.parseInt(colonne.getText().toString())*40, Integer.parseInt(rang.getText().toString())*40));
                AdaptateurAdapte customAdapter = new AdaptateurAdapte(getApplicationContext(), places);
                grille.setAdapter(customAdapter);
                grille.setVisibility(View.VISIBLE);
                findViewById(R.id.enregistrer_salle).setVisibility(View.VISIBLE);
                findViewById(R.id.txt_tableau).setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            }
        });
        enregistre.setOnClickListener(w -> {

            StringBuilder str = new StringBuilder();
            for (int place : places) {
                str.append(place).append(",");
            }
            str.append(colonne.getText().toString()).append(",");
            prefs.edit().putString(nomDeClasse.getText().toString(),commentaires.getText().toString()).apply();
            config.edit().putString(nomDeClasse.getText().toString(), str.toString()).apply();
            ajouteClasse(nomDeClasse.getText().toString());
            Intent intention = new Intent(this, ListeEleves.class);
            intention.putExtra("classe",nomDeClasse.getText().toString());
            startActivity(intention);
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    public void ajouteClasse(String nomClasse){
        String savedString = prefs.getString("liste_classes", "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        String[] lsClasses = new String[st.countTokens()+1];
        int x = st.countTokens();
        for (int i = 0; i < x; i++) {
            lsClasses[i] = st.nextToken();
        }
        if (!Arrays.asList(lsClasses).contains(nomClasse)){
            lsClasses[lsClasses.length -1] = nomClasse;
        }
        if (!Objects.equals(nomClasse, getIntent().getStringExtra("classe")) && !Objects.equals("-1", getIntent().getStringExtra("classe"))){
            lsClasses[Arrays.asList(lsClasses).indexOf(getIntent().getStringExtra("classe"))] = null;
            prefs.edit().remove(getIntent().getStringExtra("classe")).apply();
            config.edit().remove(getIntent().getStringExtra("classe")).apply();
        }
        StringBuilder str = new StringBuilder();
        for (String classe : lsClasses) {
            if (classe != null){
                str.append(classe).append(",");
            }
        }
        prefs.edit().putString("liste_classes",str.toString()).apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}