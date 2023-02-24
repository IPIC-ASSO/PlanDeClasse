package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ParametresAlgorithme extends AppCompatActivity {
    private SharedPreferences prefsAlgo;
    private SharedPreferences config;
    private SharedPreferences prefListeEleve;
    private SharedPreferences indices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres_algorithme);
        prefsAlgo = getBaseContext().getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        Log.d(TAG, "onCreate: preferences config "+config.getAll());
        Log.d(TAG, "onCreate: preferences indicess"+indices.getAll());
        Log.d(TAG, "onCreate: preferences algo"+prefsAlgo.getAll());

        Button suivant = findViewById(R.id.realiser_plan);
        initBtn();
        RadioGroup affinitesE = findViewById(R.id.affinites_e);
        RadioGroup affinitesI = findViewById(R.id.affinites_i);
        RadioGroup vue = findViewById(R.id.vision);
        RadioGroup taille = findViewById(R.id.haut);
        RadioGroup alternanceFG = findViewById(R.id.alternance_fg);
        RadioGroup alternanceAC = findViewById(R.id.alternance_ac);
        RadioGroup alternanceFD = findViewById(R.id.alternance_fd);
        RadioGroup associerDM = findViewById(R.id.associer_dm);
        RadioGroup ordreAlpha = findViewById(R.id.ordre_alpha);

        //initBouton
        Log.d(TAG, "onCreate: initBouton"+prefsAlgo.getInt("vue",0));
        switch (prefsAlgo.getInt("affinites_x",2)){
            case 0:
                affinitesE.check(R.id.radioButton2);
                break;
            case 1:
                affinitesE.check(R.id.radioButton3);
                break;
        }
        switch (prefsAlgo.getInt("affinites_i",2)){
            case 0:
                affinitesI.check(R.id.radioButton5);
                break;
            case 1:
                affinitesI.check(R.id.radioButton6);
                break;
        }
        switch (prefsAlgo.getInt("vue",0)){
            case 1:
                vue.check(R.id.radioButton8);
                break;
            case 2:
                vue.check(R.id.radioButton9);
                break;
        }
        switch (prefsAlgo.getInt("taille",0)){
            case 1:
                taille.check(R.id.radioButton11);
                break;
            case 2:
                taille.check(R.id.radioButton12);
                break;
        }
        switch (prefsAlgo.getInt("alternance_fg",0)){
            case 1:
                alternanceFG.check(R.id.radioButton14);
                break;
            case 2:
                alternanceFG.check(R.id.radioButton15);
                break;
        }
        switch (prefsAlgo.getInt("alternance_ac",1)){
            case 0:
                alternanceAC.check(R.id.radioButton16);
                break;
            case 2:
                alternanceAC.check(R.id.radioButton18);
                break;
        }
        switch (prefsAlgo.getInt("alternance_fd",0)){
            case 1:
                alternanceFD.check(R.id.radioButton20);
                break;
            case 2:
                alternanceFD.check(R.id.radioButton21);
                break;
        }
        switch (prefsAlgo.getInt("associer_dm",0)){
            case 1:
                associerDM.check(R.id.radioButton23);
                break;
            case 2:
                associerDM.check(R.id.radioButton24);
                break;
        }
        switch (prefsAlgo.getInt("ordre_alpha",1)){
            case 0:
                affinitesE.check(R.id.radioButton25);
                break;
            case 2:
                affinitesE.check(R.id.radioButton27);
                break;
        }

        suivant.setOnClickListener(v -> {
            int x;
            switch (affinitesE.getCheckedRadioButtonId()){
                case R.id.radioButton2:
                    x=1;
                    break;
                case R.id.radioButton3:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("affinites_e",x).apply();

            switch (affinitesI.getCheckedRadioButtonId()){
                case R.id.radioButton5:
                    x=1;
                    break;
                case R.id.radioButton6:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("affinites_i",x).apply();

            switch (vue.getCheckedRadioButtonId()){
                case R.id.radioButton8:
                    x=1;
                    break;
                case R.id.radioButton9:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("vue",x).apply();

            switch (taille.getCheckedRadioButtonId()){
                case R.id.radioButton11:
                    x=1;
                    break;
                case R.id.radioButton12:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("taille",x).apply();
            Log.d(TAG, "onCreate: bouton"+prefsAlgo.getInt("taille",0));

            switch (alternanceFG.getCheckedRadioButtonId()){
                case R.id.radioButton14:
                    x=1;
                    break;
                case R.id.radioButton15:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("alternance_fg",x).apply();

            switch (alternanceAC.getCheckedRadioButtonId()){
                case R.id.radioButton17:
                    x=1;
                    break;
                case R.id.radioButton18:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("alternance_ac",x).apply();

            switch (alternanceFD.getCheckedRadioButtonId()){
                case R.id.radioButton20:
                    x=1;
                    break;
                case R.id.radioButton21:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("alternance_fd",x).apply();

            switch (associerDM.getCheckedRadioButtonId()){
                case R.id.radioButton23:
                    x=1;
                    break;
                case R.id.radioButton24:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("associer_dm",x).apply();

            switch (ordreAlpha.getCheckedRadioButtonId()){
                case R.id.radioButton26:
                    x=1;
                    break;
                case R.id.radioButton27:
                    x=2;
                    break;
                default:
                    x=0;
                    break;
            }
            prefsAlgo.edit().putInt("ordre_alpha",x).apply();
            Intent intention = new Intent(this, ListeEleves.class);
            intention.putExtra("classe",getIntent().getStringExtra("classe"));
            startActivity(intention);
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Log.d(TAG, "Infos: initialisation terminée");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int orientation = this.getResources().getConfiguration().orientation;
        Log.d(TAG, "onCreate: orientation"+width);
        if (width < 1100 && orientation == Configuration.ORIENTATION_PORTRAIT){
            new MaterialAlertDialogBuilder(this).setTitle("Recommandation").setMessage("Nous vous recommandons d'utiliser votre appareil en mode paysage sur cet écran, pour bénéficier d'une expérience optimale").show();
        }
    }

    public void initBtn(){
        findViewById(R.id.btn_affinite_e).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_aff_e)).setTitle(R.string.btn_inf_aff_e).show());
        findViewById(R.id.btn_affinite_i).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_aff_i)).setTitle(R.string.btn_infos_aff_i).show());
        findViewById(R.id.btn_vue).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_vue)).setTitle(R.string.btn_infos_vue).show());
        findViewById(R.id.btn_taille).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_taille)).setTitle(R.string.btn_infos_taille).show());
        findViewById(R.id.btn_alt_fg).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_alt_fg)).setTitle(R.string.btn_infos_alt_fg).show());
        findViewById(R.id.btn_alt_ac).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_alt_ac)).setTitle(R.string.btn_infos_alt_ac).show());
        findViewById(R.id.btn_alt_fd).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_alt_df)).setTitle(R.string.btn_infos_alt_fd).show());
        findViewById(R.id.btn_associer_dm).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_asso_dm)).setTitle(R.string.btn_infos_rep_moteurs).show());
        findViewById(R.id.btn_ordre_alpha).setOnClickListener(w-> new MaterialAlertDialogBuilder(this).setMessage(getString(R.string.aide_ordre_alpha)).setTitle(R.string.btn_infos_ordre_alpha).show());
    }

    public void initBouton(){

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