package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.zip.Inflater;

public class ListeEleves extends AppCompatActivity {

    private String[] eleves;
    private SharedPreferences prefListeEleve;
    private SharedPreferences noms;
    private List<String[]> donnees;
    int nbEleves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_eleves);
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe
        noms = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//commentaire associé à chaque élève
        Log.d(TAG, "onCreate: "+prefListeEleve.getAll());
        Log.d(TAG, "onCreate: "+noms.getAll());
        donnees = readCsvFile();
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
            RadioGroup plac = vue.findViewById(R.id.groupe_radio);
            RadioButton iso = vue.findViewById(R.id.isoler);
            SeekBar prio = vue.findViewById(R.id.priorite);
            enregistrer.setOnClickListener(w ->{
                if (nbEleves<40 && !nom.getText().toString().equals("")){
                    eleves[nbEleves] = nom.getText().toString();
                    nbEleves+=1;
                    int placement;
                    switch (plac.getCheckedRadioButtonId()){
                        case R.id.radioButton:
                            placement = 0;
                            break;
                        case R.id.radioButton2:
                            placement = 1;
                            break;
                        case R.id.radioButton3:
                        default:
                            placement = 2;
                            break;
                    }

                    String[] d = new String[]{getIntent().getStringExtra("classe"),nom.getText().toString(),String.valueOf(placement),String.valueOf(iso.isChecked()),String.valueOf(prio.getProgress()),commentaire.getText().toString()};
                    noms.edit().putInt(nom.getText().toString(),donnees.size()).apply();
                    donnees.add(d);
                    inflation(new String[]{nom.getText().toString()},nbEleves-1);
                }else{
                    Toast.makeText(this,"Enregistrement impossible. Remplissez tous les champs et ne dépassez pas la limite de 40 élèves par classe.",Toast.LENGTH_LONG).show();
                }
                show.dismiss();
                
            });
            show.show();
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
            sup.setText(donnees.get(noms.getInt(eleve,0))[7]);
            liste.addView(vue);
        }
    }
    @Override
    protected void onStop() {
        StringBuilder str = new StringBuilder();
        for (String eleve : eleves) {
            if (eleve != null){
                str.append(eleve).append(",");
            }
        }
        prefListeEleve.edit().putString(getIntent().getStringExtra("classe"),str.toString()).apply();
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void writeToCSVFile (List<String[]> dataList,String classe, String eleve, String evite, String evitePas, int placement,boolean isoler, int priorite,String commentaire) {
        String[] data = String.format("%s,%s,%s,%s,%s,%s,%s,%s", classe, eleve, evite, evitePas, placement, isoler, priorite, commentaire).split(",");
        String csv = (getExternalFilesDir(null) + "/donnees.csv");
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();

        try (CSVWriter writer = new CSVWriter(new FileWriter(csv), ';', ICSVWriter.NO_QUOTE_CHARACTER,
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.RFC4180_LINE_END)) {
            dataList.add(data);
            writer.writeAll(dataList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String[]> readCsvFile(){
        List<String[]> dataList = new ArrayList<>();
        String csv = (getExternalFilesDir(null) + "/donnees.csv");
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (Reader br = Files.newBufferedReader(Paths.get(csv)); CSVReader reader =
                new CSVReaderBuilder(br).withCSVParser(parser).build()) {
            List<String[]> rows = reader.readAll();
            dataList.addAll(rows);

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        if (dataList.isEmpty()) {
            dataList.add(new String[]{"classe", "eleve", "evite", "n_evite_pas", "placement", "isoler", "priorite", "commentaire"});
        }
        return dataList;
    }
}