package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.StringTokenizer;
import java.util.zip.Inflater;

public class ListeEleves extends AppCompatActivity {

    private String[] eleves;
    private SharedPreferences prefListeEleve;
    private SharedPreferences noms;
    int nbEleves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_eleves);
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe
        noms = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//commentaire associé à chaque élève
        Log.d(TAG, "onCreate: "+prefListeEleve.getAll());
        Log.d(TAG, "onCreate: "+noms.getAll());
        obtienClasse2(getIntent().getStringExtra("classe"));
        inflation(eleves,0);
        com.google.android.material.floatingactionbutton.FloatingActionButton plus = findViewById(R.id.nouv_eleve);
        plus.setOnClickListener(v->{
            AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
            constructeur.setTitle("Nouvel élève");
            LayoutInflater inflater = this.getLayoutInflater();
            View vue = inflater.inflate(R.layout.generateur_eleve, null);
            constructeur.setView(vue);
            AlertDialog show = constructeur.show();
            Button enregistrer = vue.findViewById(R.id.enregistrer_eleve);
            Button evite = vue.findViewById(R.id.liste_evite);
            Button correcte = vue.findViewById(R.id.liste_correcte);
            EditText nom = vue.findViewById(R.id.nom_eleve);
            EditText commentaire = vue.findViewById(R.id.txt_commentaires);
            enregistrer.setOnClickListener(w ->{
                if (nbEleves<40 && !nom.getText().toString().equals("")){
                    eleves[nbEleves] = nom.getText().toString();
                    nbEleves+=1;
                    noms.edit().putString(nom.getText().toString(),commentaire.getText().toString()).apply();
                    inflation(new String[]{nom.getText().toString()},nbEleves-1);
                }else{
                    Toast.makeText(this,"Enregistrement impossible. Remplissez tous les champs et ne dépassez pas la limite de 40 élèves par classe.",Toast.LENGTH_LONG).show();
                }
                show.dismiss();
                
            });
            show.show();
        });

    }

    public void obtienClasse2(String nomClasse){
        String savedString = prefListeEleve.getString(nomClasse, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        eleves = new String[40];
        nbEleves = 0;
        int w =st.countTokens();
        for (int i = 0; i < w; i++) {
            eleves[i] = st.nextToken();
            nbEleves += 1;
        }
    }

    public void inflation(String[] listeEleves, int debut){
        LinearLayout liste = findViewById(R.id.liste_eleve);
        LayoutInflater inflater = this.getLayoutInflater();

        for (int i=debut; i<nbEleves;i++){
            String eleve = eleves[i];
            View vue = inflater.inflate(R.layout.profil_eleve, null);
            TextView nom = vue.findViewById(R.id.nom);
            nom.setText(eleve);
            TextView sup = vue.findViewById(R.id.suplement);
            sup.setText(noms.getString(eleve,"inconnu au bataillon"));
            liste.addView(vue);
        }
    }
}