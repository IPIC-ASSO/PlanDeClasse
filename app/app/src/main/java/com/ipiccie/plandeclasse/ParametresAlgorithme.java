package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;

import java.util.Arrays;
import java.util.StringTokenizer;

public class ParametresAlgorithme extends AppCompatActivity {
    private SharedPreferences prefsAlgo;
    private SharedPreferences config;
    private int[][] matrice;
    private int colonnes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres_algorithme);
        prefsAlgo = getBaseContext().getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        Log.d(TAG, "onCreate: preferences"+config.getAll());
        prefsAlgo.edit().clear().apply();

        Button suivant = findViewById(R.id.realiser_plan);
        RadioButton affinitesE = findViewById(R.id.affinites_e);
        RadioButton affinitesI = findViewById(R.id.affinites_i);
        RadioButton vue = findViewById(R.id.vision);
        RadioButton taille = findViewById(R.id.haut);
        RadioButton alternanceFG = findViewById(R.id.alternance_fg);
        RadioButton alternanceAC = findViewById(R.id.alternance_ac);
        RadioButton alternanceFD = findViewById(R.id.alternance_fd);
        RadioButton associerDM = findViewById(R.id.associer_dm);
        RadioButton ordre_alpha = findViewById(R.id.ordre_alpha);
        SeekBar affiniteEniv = findViewById(R.id.affinites_e_niv);
        SeekBar affiniteIniv = findViewById(R.id.affinites_i_niv);
        SeekBar vueNiv = findViewById(R.id.vision_niv);
        SeekBar tailleNiv = findViewById(R.id.haut_niv);
        SeekBar alternanceFGNiv = findViewById(R.id.alternance_fg_niv);
        SeekBar alternanceACNiv = findViewById(R.id.alternance_ac_niv);
        SeekBar alternanceFDNiv = findViewById(R.id.alternance_fd_niv);
        SeekBar associerDMNiv = findViewById(R.id.associer_dm_niv);
        SeekBar ordre_alphaNiv = findViewById(R.id.ordre_alpha_niv);

        StringTokenizer st = new StringTokenizer(config.getString(getIntent().getStringExtra("classe"),""), ",");
        int[] tampon = new int[st.countTokens()-1];
        for (int i = 0; i< tampon.length; i++){
            tampon[i] = Integer.parseInt(st.nextToken());
        }
        colonnes = Integer.parseInt(st.nextToken());
        matrice = new int[tampon.length/colonnes][colonnes];
        for (int y = 0; y< tampon.length/colonnes; y++){
            for (int i = 0; i< colonnes; i++) {
                matrice[y][i] = tampon[i*(y+1)];
            }
        }
        Log.d(TAG, "onCreate: "+ Arrays.deepToString(matrice));

        suivant.setOnClickListener(v -> {
            if (affinitesE.isChecked()){
                prefsAlgo.edit().putInt("affinites_e",affiniteEniv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("affinites_e",-1).apply();
            }
            if (affinitesI.isChecked()){
                prefsAlgo.edit().putInt("affinites_i",affiniteIniv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("affinites_i",-1).apply();
            }
            if (vue.isChecked()){
                prefsAlgo.edit().putInt("vue",vueNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("vue",-1).apply();
            }
            if (taille.isChecked()){
                prefsAlgo.edit().putInt("taille",tailleNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("taille",-1).apply();
            }
            if (alternanceFG.isChecked()){
                prefsAlgo.edit().putInt("alternance_fg",alternanceFGNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("alternance_fg",-1).apply();
            }
            if (alternanceAC.isChecked()){
                prefsAlgo.edit().putInt("alternance_ac",alternanceACNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("alternance_ac",-1).apply();
            }
            if (alternanceFD.isChecked()){
                prefsAlgo.edit().putInt("alternance_fd",alternanceFDNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("alternance_fd",-1).apply();
            }
            if (associerDM.isChecked()){
                prefsAlgo.edit().putInt("associer_dm",associerDMNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("associer_dm",-1).apply();
            }
            if (ordre_alpha.isChecked()){
                prefsAlgo.edit().putInt("ordre_alpha",ordre_alphaNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("ordre_alpha",-1).apply();
            }

        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return false;
    }


}