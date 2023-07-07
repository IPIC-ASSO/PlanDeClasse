package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringTokenizer;

public class NouvelleClasse extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences config;
    private SharedPreferences prefListeEleve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generateur_classe);
        prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        Log.d(TAG, "onCreate: preferences"+prefs.getAll());
        Log.d(TAG, "onCreate: preferences"+config.getAll());
        Button appli = findViewById(R.id.appliquer);
        final com.google.android.material.textfield.TextInputEditText colonne = findViewById(R.id.colonnes);
        final com.google.android.material.textfield.TextInputEditText rang = findViewById(R.id.rangees);
        final com.google.android.material.textfield.TextInputEditText nomDeClasse = findViewById(R.id.nom_classe);
        final EditText commentaires = findViewById(R.id.commentaires_classe_txt);
        if (!Objects.equals(getIntent().getStringExtra("classe"), "-1")) {
            nomDeClasse.setText(getIntent().getStringExtra("classe"));
            commentaires.setText(prefs.getString(nomDeClasse.getText().toString(), ""));
            StringTokenizer st = new StringTokenizer(config.getString(getIntent().getStringExtra("classe"), ""), ",");
            int[] places = new int[st.countTokens() - 1];
            int x = st.countTokens() - 1;
            for (int i = 0; i < x; i++) {
                places[i] = Integer.parseInt(st.nextToken());
            }
            colonne.setText(st.nextToken());
            rang.setText(String.valueOf(places.length / Integer.parseInt(colonne.getText().toString())));
        }
        appli.setOnClickListener(w -> {
            if (!colonne.getText().toString().equals("") && !rang.getText().toString().equals("") &&!nomDeClasse.getText().toString().equals("") ){
                String nbColonne = colonne.getText().toString();
                LayoutInflater inflater = this.getLayoutInflater();
                View vue = inflater.inflate(R.layout.emulateur_classe, null);
                AlertDialog.Builder constructeur= new AlertDialog.Builder(this);
                constructeur.setTitle("Configuration");
                constructeur.setView(vue);
                TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                TableLayout table = vue.findViewById(R.id.la_table);
                table.setColumnStretchable(0,true);
                table.setColumnStretchable(Integer.parseInt(nbColonne)+1,true);
                table.setBackground(AppCompatResources.getDrawable(this,R.drawable.bords));
                final int[] configurationC = litConfig(nomDeClasse.getText().toString(),Integer.parseInt(nbColonne),Integer.parseInt(rang.getText().toString()));
                Log.d(TAG, "onCreate: +config"+ Arrays.toString(configurationC));
                for (int y = 0; y<Integer.parseInt(rang.getText().toString());y++){
                    final int yy = y;
                    TableRow ligne = new TableRow(this);
                    ligne.setLayoutParams(rowParams);
                    ligne.setMinimumWidth(40);
                    ligne.setPadding(1,1,1,1);
                    table.addView(ligne);
                    Space esp= new Space(this);
                    ligne.addView(esp);
                    for (int x =0; x<Integer.parseInt(nbColonne);x++){
                        final int xx = x;
                        ImageView posEleve = new ImageView(this);
                        ligne.addView(posEleve);
                        if(configurationC[yy*Integer.parseInt(nbColonne)+xx]==0) {
                            posEleve.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.place_vide));
                        }else{
                            posEleve.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.place_occup_e));
                        }
                        posEleve.setOnClickListener(v ->{
                            if(configurationC[yy*Integer.parseInt(nbColonne)+xx]==0) {
                                posEleve.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.place_occup_e));
                                configurationC[yy * Integer.parseInt(nbColonne) + xx] = 1;
                            }else{
                                posEleve.setImageDrawable(AppCompatResources.getDrawable(this,R.drawable.place_vide));
                                configurationC[yy * Integer.parseInt(nbColonne) + xx] = 0;
                            }
                        });
                    }
                    Space esp2= new Space(this);
                    ligne.addView(esp2);
                }
                constructeur.setPositiveButton("Valider", (dialog, which) -> {
                    int compte = 0;
                    StringBuilder str = new StringBuilder();
                    for (int place : configurationC) {
                        str.append(place).append(",");
                        if(place == 1)compte++;
                    }
                    if(compte>2){
                        str.append(nbColonne).append(",");
                        Log.d(TAG, "onCreate: +config"+str);
                        prefs.edit().putString(nomDeClasse.getText().toString(),commentaires.getText().toString()).apply();
                        config.edit().putString(nomDeClasse.getText().toString(), str.toString()).apply();
                        Log.d(TAG, "onCreate: +++");
                        ajouteClasse(nomDeClasse.getText().toString());
                        dialog.dismiss();
                        Intent intention = new Intent(this, ListeEleves.class);
                        intention.putExtra("classe",nomDeClasse.getText().toString());
                        intention.putExtra("debut",true);
                        startActivity(intention);
                    }else{
                        Toast.makeText(this,"Veillez placer au moins trois tables dans cette classe",Toast.LENGTH_SHORT).show();
                    }
                });
                constructeur.show();
            }else{
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
            }
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
            Log.d(TAG, "ajouteClasse: OKPASOK");
            String savedString2 = prefListeEleve.getString(nomClasse, "");
            StringTokenizer st2 = new StringTokenizer(savedString2, ",");
            int xyz = st2.countTokens();
            SharedPreferences indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
            for (int i = 0; i < xyz; i++) {
                indices.edit().remove(st.nextToken()+getIntent().getStringExtra("classe")).apply();
            }
            prefListeEleve.edit().remove(getIntent().getStringExtra("classe")).apply();
        }
        StringBuilder str = new StringBuilder();
        for (String classe : lsClasses) {
            if (classe != null){
                str.append(classe).append(",");
            }
        }
        prefs.edit().putString("liste_classes",str.toString()).apply();
    }

    public int[] litConfig(String nomDeClasse, int colonne, int rangee){
        String data = config.getString(nomDeClasse,"fantome");
        if (!data.equals("fantome")){
            StringTokenizer st = new StringTokenizer(data,",");
            int[] configuration = new int[st.countTokens()-1];
            for (int i = 0; i< configuration.length; i++){
                configuration[i] = Integer.parseInt(st.nextToken());
            }
            if(colonne == Integer.parseInt(st.nextToken()) && colonne*rangee == configuration.length) return configuration;
        }
        return new int[colonne*rangee];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.détruire:
                onExplose();
                return true;
            case R.id.reinit:
                new MesOutils(this).reinitialiser();
                onExplose();
                return true;
            case R.id.nous_soutenir:
                new MesOutils(this).soutient();
                return true;
            case R.id.infos:
                new MesOutils(this).infos();
                return true;
            case R.id.contact:
                startActivity( new Intent(this, NousContacter.class));
                return true;
            case R.id.aide:
                startActivity( new Intent(this, Aide.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onExplose(){this.finishAffinity();}
}