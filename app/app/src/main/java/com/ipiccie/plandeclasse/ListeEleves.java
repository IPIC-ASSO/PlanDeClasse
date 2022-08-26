package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class ListeEleves extends AppCompatActivity {

    private String[] eleves;
    private SharedPreferences prefListeEleve;
    private SharedPreferences indices;
    private SharedPreferences config;
    private List<String[]> donnees;
    private String classe;
    int nbEleves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_eleves);
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        Log.d(TAG, "registre 1 "+prefListeEleve.getAll());
        Log.d(TAG, "registre 2"+indices.getAll());
        Log.d(TAG, "registre 3"+config.getAll());
        donnees = readCsvFile();
        classe = getIntent().getStringExtra("classe");
        obtienClasse2(classe);
        inflation();
        com.google.android.material.floatingactionbutton.FloatingActionButton plus = findViewById(R.id.nouv_eleve);
        plus.setOnClickListener(v->generateurEleve(-1));
        findViewById(R.id.vers_realiser_plan).setOnClickListener(v -> {
            Intent intention = new Intent(this, ParametresAlgorithme.class);
            intention.putExtra("classe",classe);
            startActivity(intention);
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void obtienClasse2(String nomClasse){
        String savedString = prefListeEleve.getString(nomClasse, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        StringTokenizer st2 = new StringTokenizer(config.getString(getIntent().getStringExtra("classe"),""), ",");
        eleves = new String[st2.countTokens()-1];
        nbEleves = 0;
        int w =st.countTokens();
        for (int i = 0; i < w; i++) {
            eleves[i] = st.nextToken();
            nbEleves += 1;
        }
    }

    public void inflation(){
        LinearLayout liste = findViewById(R.id.liste_eleve);
        LayoutInflater inflater = this.getLayoutInflater();
        liste.removeAllViews();

        for (int i=0; i<nbEleves;i++){
            String eleve = eleves[i];
            View vue = inflater.inflate(R.layout.profil_eleve, null);
            TextView nom = vue.findViewById(R.id.nom);
            nom.setText(eleve);
            TextView sup = vue.findViewById(R.id.suplement);
            sup.setText(donnees.get(indices.getInt(eleve+classe,0))[14]);
            vue.setOnClickListener(v -> generateurEleve(indices.getInt(eleve+classe,0)));
            liste.addView(vue);
        }
        if (nbEleves == eleves.length) {
            findViewById(R.id.vers_realiser_plan).setVisibility(View.VISIBLE);
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
        prefListeEleve.edit().putString(classe,str.toString()).apply();
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

    private void writeToCSVFile (List<String[]> dataList) {
        String csv = (getExternalFilesDir(null) + "/donnees.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(csv), ';', ICSVWriter.NO_QUOTE_CHARACTER,
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.RFC4180_LINE_END)) {
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
            dataList.add(new String[]{"classe", "eleve", "evite", "n_evite_pas","taille","vue", "placement", "difficultés","attitude","genre","dyslexique","isoler","moteur", "priorite", "commentaire"});
        }
        return dataList;
    }

    public void generateurEleve (int indice){
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
        RadioGroup taille = vue.findViewById(R.id.taille);
        RadioGroup vision = vue.findViewById(R.id.vue);
        RadioGroup plac = vue.findViewById(R.id.placement);
        RadioGroup difficultes = vue.findViewById(R.id.difficultes);
        RadioGroup attitude = vue.findViewById(R.id.attitude);
        RadioGroup genre = vue.findViewById(R.id.genre);
        CheckBox dys = vue.findViewById(R.id.dys);
        CheckBox iso = vue.findViewById(R.id.isoler);
        CheckBox moteur = vue.findViewById(R.id.moteur);
        SeekBar prio = vue.findViewById(R.id.priorite);
        int compte = 0; //nombre d'élèves enregistrés dans la classe
        for (String eleve: eleves){
            if (eleve != null)compte+=1;
        }
        boolean[] selection = new boolean[compte];
        boolean[] selection2 = new boolean[compte];
        Arrays.fill(selection2,false);
        Arrays.fill(selection,false);
        if (indice>=0){ //chargement des données enregistrées sur l'élève
            try{


            String[] d = donnees.get(indice);
            nom.setText(d[1]);
            commentaire.setText(d[14]);
            dys.setChecked(Boolean.parseBoolean(d[10]));
            iso.setChecked(Boolean.parseBoolean(d[11]));
            moteur.setChecked(Boolean.parseBoolean(d[12]));
            prio.setProgress(Integer.parseInt(d[6]));
            switch (Integer.parseInt(d[4])){    //taille
                case 0:
                    plac.check(R.id.taille_1);
                    break;
                case 2:
                    plac.check(R.id.taille_3);
                    break;
                default:
                    plac.check(R.id.taille_2);
                    break;
            }
            switch (Integer.parseInt(d[5])){    //vue
                case 0:
                    plac.check(R.id.vue_1);
                    break;
                case 2:
                    plac.check(R.id.vue_3);
                    break;
                default:
                    plac.check(R.id.vue_2);
                    break;
            }
            switch (Integer.parseInt(d[6])){    //placement
                case 0:
                    plac.check(R.id.plac_1);
                    break;
                case 1:
                    plac.check(R.id.plac_2);
                    break;
                default:
                    plac.check(R.id.plac_3);
                    break;
            }
            switch (Integer.parseInt(d[7])){    //difficultés
                case 0:
                    plac.check(R.id.diff_1);
                    break;
                case 1:
                    plac.check(R.id.diff_2);
                    break;
                default:
                    plac.check(R.id.diff_3);
                    break;
            }
            switch (Integer.parseInt(d[8])){    //attitude
                case 0:
                    plac.check(R.id.att_1);
                    break;
                case 1:
                    plac.check(R.id.att_2);
                    break;
                default:
                    plac.check(R.id.att_3);
                    break;
            }
            switch (Integer.parseInt(d[9])){    //genre
                case 0:
                    plac.check(R.id.fille);
                    break;
                case 1:
                    plac.check(R.id.garcon);
                    break;
            }
            StringTokenizer stEvite = new StringTokenizer(d[2], ",");
            StringTokenizer stCorrecte = new StringTokenizer(d[3], ",");
            int x= stEvite.countTokens();
            Log.d(TAG, "generateurEleve: "+x);
            for (int i= 0; i< x; i++){
                String nom1 = donnees.get(Integer.parseInt(stEvite.nextToken()))[1];
                String nom2 = donnees.get(Integer.parseInt(stCorrecte.nextToken()))[1];
                if (Arrays.asList(eleves).contains(nom1))selection[Arrays.asList(eleves).indexOf(nom1)]=true;
                if (Arrays.asList(eleves).contains(nom2))selection2[Arrays.asList(eleves).indexOf(nom2)]=true;
            }
            Log.d(TAG, "generateurEleve: "+Arrays.toString(selection));
            Log.d(TAG, "generateurEleve: "+Arrays.toString(selection2));
            }catch (Exception e){
                Log.e(TAG, "generateurEleve: ", e);
                Toast.makeText(this,"incompatibilité de version. Désinstallez puis réinstallez l'application",Toast.LENGTH_LONG).show();
            }
        }
        int[] eviteIndices = new int[compte];
        int[] correcteIndices = new int[compte];
        evite.setOnClickListener(x ->{
            Log.d(TAG, "generateur eleve"+ Arrays.toString(selection));
            AlertDialog.Builder constr = new AlertDialog.Builder(this);
            constr.setTitle("Doit éviter...");
            ListView liste = new ListView(this);
            AutreAdaptateurAdapte customAdapter = new AutreAdaptateurAdapte(getApplicationContext(), eleves, selection, nom.getText().toString());
            liste.setAdapter(customAdapter);
            constr.setView(liste);
            constr.setPositiveButton("Valider", (dialog, which) -> dialog.dismiss());
            constr.show();
        });
        correcte.setOnClickListener(x ->{
            AlertDialog.Builder constr = new AlertDialog.Builder(this);
            constr.setTitle("Devrait être avec...");
            ListView liste = new ListView(this);
            AutreAdaptateurAdapte customAdapter = new AutreAdaptateurAdapte(getApplicationContext(), eleves, selection2, nom.getText().toString());
            liste.setAdapter(customAdapter);
            constr.setView(liste);
            constr.setPositiveButton("Valider", (dialog, which) -> dialog.dismiss());
            constr.show();
        });
        enregistrer.setOnClickListener(w ->{
            if (nbEleves<eleves.length && !nom.getText().toString().equals("") && !(genre.getCheckedRadioButtonId()!= R.id.fille && genre.getCheckedRadioButtonId()!=R.id.garcon) &&! (Arrays.asList(eleves).contains(nom.getText().toString()) && indice<0)){
                if (indice>=0){ //élève déjà enregistré (modifications)
                    eleves[Arrays.asList(eleves).indexOf(donnees.get(indice)[1])] = nom.getText().toString();
                }else{
                    eleves[nbEleves] = nom.getText().toString();    //ajoute le nom du nouvel élève
                    nbEleves+=1;
                }
                Log.d(TAG, "generateurEleve: "+Arrays.toString(selection));
                for(int i = 0; i<selection.length;i++){ //met à jour les affinités
                    Log.d(TAG, "generateurEleve: "+prefListeEleve.getInt(eleves[i]+classe,10));
                    if (selection[i])eviteIndices[i]=indices.getInt(eleves[i]+classe,0);
                    if (selection2[i])correcteIndices[i] = indices.getInt(eleves[i] + classe, 0);
                }
                StringBuilder strEvite = new StringBuilder();   //convertit en chaine de cararctère les indices d'élèves à (!)éviter
                for (int eviteI : eviteIndices) {
                    strEvite.append(eviteI).append(",");
                }
                StringBuilder strCorrecte = new StringBuilder();
                for (int correcteI : correcteIndices) {
                    strCorrecte.append(correcteI).append(",");
                }
                String tailleE; //enregiste le bouton sélectionné
                String vueE;
                String placement;
                String difE;
                String attE;
                String genE;
                switch (taille.getCheckedRadioButtonId()){
                    case R.id.taille_1:
                        tailleE = "0";
                        break;
                    case R.id.taille_3:
                        tailleE = "2";
                        break;
                    case R.id.taille_2:
                    default:
                        tailleE = "1";
                        break;
                }
                switch (vision.getCheckedRadioButtonId()){
                    case R.id.vue_1:
                        vueE = "0";
                        break;
                    case R.id.vue_3:
                        vueE = "2";
                        break;
                    case R.id.vue_2:
                    default:
                        vueE = "1";
                        break;
                }
                switch (plac.getCheckedRadioButtonId()){
                    case R.id.plac_1:
                        placement = "0";
                        break;
                    case R.id.plac_2:
                        placement = "1";
                        break;
                    case R.id.plac_3:
                    default:
                        placement = "2";
                        break;
                }
                switch (difficultes.getCheckedRadioButtonId()){
                    case R.id.diff_1:
                        difE = "0";
                        break;
                    case R.id.diff_2:
                        difE = "1";
                        break;
                    case R.id.diff_3:
                    default:
                        difE = "2";
                        break;
                }
                switch (attitude.getCheckedRadioButtonId()){
                    case R.id.att_1:
                        attE = "0";
                        break;
                    case R.id.att_2:
                        attE = "1";
                        break;
                    case R.id.att_3:
                    default:
                        attE = "2";
                        break;
                }
                switch (genre.getCheckedRadioButtonId()){
                    case R.id.fille:
                        genE = "0";
                        break;
                    default:
                        genE = "1";
                        break;
                }

                String[] d = new String[]{classe,nom.getText().toString(),strEvite.toString(),strCorrecte.toString(),tailleE, vueE,placement,difE,attE,genE,String.valueOf(dys.isChecked()),String.valueOf(iso.isChecked()),String.valueOf(moteur.isChecked()),String.valueOf(prio.getProgress()),commentaire.getText().toString()};
                Log.d(TAG, "generateurEleve: nouvelle ligne "+Arrays.toString(d));
                if (indice>=0){
                    indices.edit().remove(donnees.get(indice)[1]+classe).apply();
                    indices.edit().putInt(nom.getText().toString()+classe,indice).apply();
                    donnees.set(indice,d);
                }else{
                    indices.edit().putInt(nom.getText().toString()+classe,donnees.size()).apply();
                    donnees.add(d);
                }
                ProgressBar barre = new ProgressBar(this);
                barre.setIndeterminate(true);
                show.setView(barre);
                writeToCSVFile(donnees);
                inflation();
            }else if(nbEleves>=eleves.length) {
                Toast.makeText(this, "Enregistrement impossible. Trop d'élèves pour cette classe. Veuillez d'abord modifier sa configuration.", Toast.LENGTH_LONG).show();
            }else if(Arrays.asList(eleves).contains(nom.getText().toString()) && indice <0){
                Toast.makeText(this,"Enregistrement impossible. Un élève du même nom existe déjà dans cette classe.",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Enregistrement impossible. Veuillez remplir tous les champs marqués d'une astérisque.",Toast.LENGTH_LONG).show();
            }
            show.dismiss();
        });
        show.show();
    }
}