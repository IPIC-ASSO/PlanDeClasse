package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class NouvelleClasse extends AppCompatActivity {

    private SharedPreferences prefs;
    private SharedPreferences config;
    private SharedPreferences prefListeEleve;
    private List<Integer> configuration2;

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
        final EditText colonne = findViewById(R.id.colonnes);
        final EditText rang = findViewById(R.id.rangees);
        final EditText nomDeClasse = findViewById(R.id.nom_classe);
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
            /*if (!colonne.getText().toString().equals("") && !rang.getText().toString().equals("") &&!nomDeClasse.getText().toString().equals("")){
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
            }*/
            if (!colonne.getText().toString().equals("") && !rang.getText().toString().equals("") &&!nomDeClasse.getText().toString().equals("") ){
            AlertDialog.Builder constructeur= new AlertDialog.Builder(this);
            constructeur.setTitle("Configuration");
            ScrollView def = new ScrollView(this);
            HorizontalScrollView def2 = new HorizontalScrollView(this);
            def.setScrollContainer(true);
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            TableLayout table = new TableLayout(this);
            table.setScrollContainer(true);

            table.setLayoutParams(tableParams);
            table.setColumnStretchable(0,true);
            table.setColumnStretchable(Integer.parseInt(colonne.getText().toString())+1,true);
            table.setBackground(getDrawable(R.drawable.bords));
            def2.addView(table);
            def.addView(def2);
            constructeur.setView(def);
            configuration2 = new ArrayList<>();
            for (int y = 0; y<Integer.parseInt(rang.getText().toString());y++){
                final int yy = y;
                TableRow ligne = new TableRow(this);
                ligne.setLayoutParams(rowParams);
                ligne.setMinimumWidth(40);
                ligne.setPadding(1,1,1,1);
                table.addView(ligne);
                Space esp= new Space(this);
                ligne.addView(esp);
                for (int x =0; x<Integer.parseInt(colonne.getText().toString());x++){
                    final int xx = x;
                    ImageView posEleve = new ImageView(this);
                    ligne.addView(posEleve);
                    posEleve.setImageDrawable(getDrawable(R.drawable.ic_baseline_add_24));
                    configuration2.add(0);
                    posEleve.setOnClickListener(v ->{
                        if(configuration2.get(yy*Integer.parseInt(colonne.getText().toString())+xx)==0) {
                            posEleve.setBackgroundColor(Color.parseColor("#808080"));
                            configuration2.set(yy * Integer.parseInt(colonne.getText().toString()) + xx, 1);
                        }else{
                            posEleve.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            configuration2.set(yy * Integer.parseInt(colonne.getText().toString()) + xx, 0);
                        }
                    });
                }
                Space esp2= new Space(this);
                ligne.addView(esp2);
            }
            constructeur.setPositiveButton("Valider", (dialog, which) -> {

                StringBuilder str = new StringBuilder();
                for (int place : configuration2) {
                    str.append(place).append(",");
                }
                str.append(colonne.getText().toString()).append(",");
                prefs.edit().putString(nomDeClasse.getText().toString(),commentaires.getText().toString()).apply();
                config.edit().putString(nomDeClasse.getText().toString(), str.toString()).apply();
                ajouteClasse(nomDeClasse.getText().toString());
                dialog.dismiss();
                Intent intention = new Intent(this, ListeEleves.class);
                intention.putExtra("classe",nomDeClasse.getText().toString());
                startActivity(intention);
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
                reinitialiser();
                return true;
            case R.id.nous_soutenir:
                //soutient();
                return true;
            case R.id.infos:
                infos();
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

    public void reinitialiser(){
        SharedPreferences prefsAlgo = getBaseContext().getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        SharedPreferences prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        SharedPreferences config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        SharedPreferences indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        SharedPreferences prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        prefsAlgo.edit().clear().apply();
        prefListeEleve.edit().clear().apply();
        config.edit().clear().apply();
        indices.edit().clear().apply();
        prefs.edit().clear().apply();
        File fich = new File((getExternalFilesDir(null) + "/donnees.csv"));
        fich.delete();
        onExplose();
    }

    public void infos(){
        AlertDialog.Builder constr = new AlertDialog.Builder(this);
        constr.setTitle("Informations");
        constr.setMessage(String.format("Vous utilisez la %s de l'application. \nL'application a été développée par IPIC&cie.",getString(R.string.version)));
        constr.show();
    }
}