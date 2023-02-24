package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.content.ContextWrapper.*;

import androidx.appcompat.app.AlertDialog;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MesOutils {

    private Context contexte;

    public MesOutils(Context contexte){
        this.contexte = contexte;
    }

    public List<String[]> litFichierCsv(){
        List<String[]> dataList = new ArrayList<>();
        String csv = (contexte.getExternalFilesDir(null) + "/donnees.csv");
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
        for(String[] i:dataList) {
            Log.d(TAG, "litFichierCsv: "+ Arrays.toString(i));
        }
        return dataList;
    }

    public String [] obtienClasse(String nomClasse){
        SharedPreferences prefListeEleve = contexte.getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);
        String savedString = prefListeEleve.getString(nomClasse, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");

        String[] eleves = new String[st.countTokens()];
        for (int i = 0; i < eleves.length; i++) {
            eleves[i] = st.nextToken();
        }
        return eleves;
    }

    public void reinitialiser(){
        SharedPreferences prefsAlgo = contexte.getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        SharedPreferences prefListeEleve = contexte.getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        SharedPreferences config = contexte.getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        SharedPreferences indices = contexte.getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        SharedPreferences prefs = contexte.getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        prefsAlgo.edit().clear().apply();
        prefListeEleve.edit().clear().apply();
        config.edit().clear().apply();
        indices.edit().clear().apply();
        prefs.edit().clear().apply();
        File fich = new File((contexte.getExternalFilesDir(null) + "/donnees.csv"));
        fich.delete();
    }
    public void soutient(){
        AlertDialog.Builder construit = new AlertDialog.Builder(contexte);
        construit.setTitle("Merci de votre soutient");
        construit.setMessage("Quelle générosité :.)");
        construit.show();
    }

    public void infos(){
        AlertDialog.Builder constr = new AlertDialog.Builder(contexte);
        constr.setTitle("Informations");
        constr.setMessage(String.format("Vous utilisez la %s de l'application.\n%s \nL'application a été développée par IPIC&cie.",contexte.getString(R.string.version),contexte.getString(R.string.notes_version)));
        constr.show();
    }
}
