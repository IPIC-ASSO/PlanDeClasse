package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class ParametresAlgorithme extends AppCompatActivity {
    private SharedPreferences prefsAlgo;
    private SharedPreferences config;
    private SharedPreferences prefListeEleve;
    private SharedPreferences indices;
    private int[][] matrice;    //salle de classe
    private int colonnes; //nombre de colonnes
    private String[] eleves;    //liste des élèves
    private String classe;      //nom de la classe
    private List<int[]> laMatrice;    //liste de liste par indice des places, pour chaque élève
    private List<String[]> donnees;     //données
    private int[] importance;   //niveau d'importance de chaque élève
    private int[] tampon;   //matrice mise sur une seule ligne
    private int[] place;    //place assigné à chaque élève (indice: élève, valeur: place dans laMatrice)
    private String[] Rplace;   //place vide; indice: case de tampon, valeur: 1 si occupée

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (Boolean.FALSE.equals(isGranted)) {
                    AlertDialog.Builder constructeur = new AlertDialog.Builder(this);
                    constructeur.setTitle("Avertissement");
                    constructeur.setMessage("Certaines fonctionnalités pourraient ne pas être disponible \nPour plus d'informations, contactez le service d'assistance IPIC&cie");
                }
            });


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
        prefsAlgo.edit().clear().apply();

        Button suivant = findViewById(R.id.realiser_plan);
        CheckBox affinitesE = findViewById(R.id.affinites_e);
        CheckBox affinitesI = findViewById(R.id.affinites_i);
        CheckBox vue = findViewById(R.id.vision);
        CheckBox taille = findViewById(R.id.haut);
        CheckBox alternanceFG = findViewById(R.id.alternance_fg);
        CheckBox alternanceAC = findViewById(R.id.alternance_ac);
        CheckBox alternanceFD = findViewById(R.id.alternance_fd);
        CheckBox associerDM = findViewById(R.id.associer_dm);
        CheckBox ordreAlpha = findViewById(R.id.ordre_alpha);
        SeekBar affiniteEniv = findViewById(R.id.affinites_e_niv);
        SeekBar affiniteIniv = findViewById(R.id.affinites_i_niv);
        SeekBar vueNiv = findViewById(R.id.vision_niv);
        SeekBar tailleNiv = findViewById(R.id.haut_niv);
        SeekBar alternanceFGNiv = findViewById(R.id.alternance_fg_niv);
        SeekBar alternanceACNiv = findViewById(R.id.alternance_ac_niv);
        SeekBar alternanceFDNiv = findViewById(R.id.alternance_fd_niv);
        SeekBar associerDMNiv = findViewById(R.id.associer_dm_niv);
        SeekBar ordreAlphaNiv = findViewById(R.id.ordre_alpha_niv);


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
            if (ordreAlpha.isChecked()){
                prefsAlgo.edit().putInt("ordre_alpha",ordreAlphaNiv.getProgress()).apply();
            }else{
                prefsAlgo.edit().putInt("ordre_alpha",-1).apply();
            }
            laBoucle();
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Log.d(TAG, "Infos: initialisation terminée");
    }

    private List<String[]> readCsvFile2(){
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

    public void init(){
        laMatrice = new ArrayList<>();
        donnees=readCsvFile2();
        classe = getIntent().getStringExtra("classe");
        obtienClasse3(classe);
        StringTokenizer st = new StringTokenizer(config.getString(classe,"fantome"), ",");
        int compte = 0;
        tampon = new int[st.countTokens()-1];
        for (int i = 0; i< tampon.length; i++){
            tampon[i] = Integer.parseInt(st.nextToken());
            if (tampon[i] == 1)compte++;
        }
        colonnes = Integer.parseInt(st.nextToken());
        matrice = new int[tampon.length/colonnes][colonnes];
        for (int y = 0; y< tampon.length/colonnes; y++){
            if (colonnes >= 0)
                System.arraycopy(tampon, colonnes * (y), matrice[y], 0, colonnes);
        }
        importance = new int[eleves.length];
        for(int i =0;i<eleves.length;i++){
            importance[i] = Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[13])+1;
            laMatrice.add(new int[compte]);
        }
        place = new int[eleves.length];
        Rplace= new String[compte];
        Arrays.fill(Rplace,"");
    }

    public void placement(int val){
        List<Integer> compteRang1 = new ArrayList<>();
        List<Integer> compteRang2 = new ArrayList<>();
        for (int i = 0; i< eleves.length;i++){      //compte le nombre d'élève à placer
            if (Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[6])==0){   //placer davant
                compteRang1.add(i);
            }else if (Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[6])==1){    //placer derrière
                compteRang2.add(i);
            }
        }
        int compteXrang1 = 0;       //détermine les places qui seront occupées
        int ligne = 1;
        while(compteXrang1<compteRang1.size()){
            for (int i = 0; i< colonnes;i++){
                if(tampon[i*ligne]==1)compteXrang1++;
            }
            ligne+=1;
        }
        int compteXrang2 = 0;
        ligne = tampon.length/colonnes;
        while(compteXrang2<compteRang2.size()){
            for (int i = 0; i< colonnes;i++){
                if(tampon[i*ligne]==1)compteXrang2++;
            }
            ligne -=1;
        }
        for (int x:compteRang1){        //remplit rangs de devant
            Log.d(TAG, "placement: "+x);
            for (int i = 0; i<compteXrang1;i++){
                laMatrice.get(x)[i] += val * importance[x];
            }
        }
        for (int x:compteRang2){    //remplit rangs de derriere
            for (int i = 0; i<compteXrang2;i++){
                laMatrice.get(x)[laMatrice.get(x).length-i-1] += val * importance[x];
            }
        }
        Log.i(TAG, "Informations placement: OK");
    }

    public void ordreAlpha(int val){
        String[] elevesAlpha = eleves.clone();
        Arrays.sort(elevesAlpha);
        for(int i = 0; i < eleves.length; i++){
            for(int j = 0; j<laMatrice.get(i).length; j++){
                laMatrice.get(i)[j] = val*5 - (Math.abs(Arrays.asList(elevesAlpha).indexOf(eleves[i])-j))*val;
            }
        }
        Log.i(TAG, "Informations ordreAlpha: OK"+ Arrays.toString(elevesAlpha));
    }

    public void taille (int val) {
        List<Integer> compteTaille1 = new ArrayList<>();
        List<Integer> compteTaille2 = new ArrayList<>();
        for (int i = 0; i < eleves.length; i++) {      //compte le nombre d'élève à placer
            if (Integer.parseInt(donnees.get(indices.getInt(eleves[i] + classe, 0))[4]) == 2) {
                compteTaille1.add(i);
            } else if (Integer.parseInt(donnees.get(indices.getInt(eleves[i] + classe, 0))[4]) == 0) {
                compteTaille2.add(i);
            }
        }
        int compteXrang1 = 0;       //détermine les places qui seront occupées
        int ligne = 1;
        while (compteXrang1 < compteTaille1.size()) {
            for (int i = 0; i < colonnes; i++) {
                if (tampon[i * ligne] == 1) compteXrang1++;
            }
            ligne += 1;
        }
        int compteXrang2 = 0;
        ligne = tampon.length / colonnes;
        while (compteXrang2 < compteTaille2.size()) {
            for (int i = 0; i < colonnes; i++) {
                if (tampon[i * ligne] == 1) compteXrang2++;
            }
            ligne -= 1;
        }

        for (int i = 0; i < eleves.length; i++) {
            if (compteTaille1.contains(i)) {
                for (int j = 0; j < compteXrang1; j++) {//petit
                    laMatrice.get(i)[j] += val * importance[j];
                }
            } else if (compteTaille2.contains(i)) {    //grand
                for (int j = 0; j < compteXrang2; j++) {
                    laMatrice.get(i)[laMatrice.get(i).length - j - 1] += val * importance[j];
                }
            } else {        //moyen
                for (int j = compteXrang1; j < compteXrang2; j++) {
                    laMatrice.get(i)[j] += val * importance[j];
                }
            }
        }
        Log.i(TAG, "Informations taille: OK");
    }

    public void isolement(){
        int[] isoVal = new int[laMatrice.get(0).length];
        int compte = 0;
        for (int indiceTampon = 0; indiceTampon<tampon.length; indiceTampon++){    //détermine le nombre de places adjacentes
            if(tampon[indiceTampon] == 1){
                int x = 0;
                if (indiceTampon/colonnes>0 && tampon[indiceTampon-colonnes]==1)x++;
                if (indiceTampon<tampon.length-colonnes && tampon[indiceTampon+colonnes]==1)x++;
                if (indiceTampon%colonnes>0 && tampon[indiceTampon-1]==1)x++;
                if (indiceTampon%colonnes<colonnes-2 && tampon[indiceTampon+1]==1)x++;
                isoVal[compte] = x;
                compte++;
            }
        }
        for(int i = 0; i < eleves.length; i++){
            for(int j=0; j<isoVal.length;j++){
                laMatrice.get(i)[j] += 5*importance[i]*(4-isoVal[j]);
            }
        }
        Log.i(TAG, "Informations isolement: OK");

    }

    public void affinites(int indice, int affE, int affI, int fg, int ac, int fd, int dm){
        int indexAct =  place[indice];
        int compte = 0;
        int indiceTampon =-1;
        int[] correspondance = new int[tampon.length];     // associe chaque élément de tampon qui est remplit à son indice dans Matrix
        while (compte < indexAct+1) {
            indiceTampon++;
            if (tampon[indiceTampon] == 1){
                correspondance[indiceTampon] = compte;
                compte++;
            }
        }
        Log.d(TAG, "affinites: ind "+Arrays.asList(donnees.get(indices.getInt(eleves[indice]+classe,0))));
        String genre = donnees.get(indices.getInt(eleves[indice]+classe,0))[9];
        boolean dys= Boolean.parseBoolean(donnees.get(indices.getInt(eleves[indice]+classe,0))[10]);
        boolean isoler = Boolean.parseBoolean(donnees.get(indices.getInt(eleves[indice]+classe,0))[11]);
        boolean moteur= Boolean.parseBoolean(donnees.get(indices.getInt(eleves[indice]+classe,0))[12]);
        int calme = Integer.parseInt(donnees.get(indices.getInt(eleves[indice]+classe,0))[8]);
        int fort = Integer.parseInt(donnees.get(indices.getInt(eleves[indice]+classe,0))[7]);
        int droite = indexAct;
        int gauche = indexAct;
        int devant = indexAct;
        int derriere = indexAct;
        if (indiceTampon/colonnes>0 && tampon[indiceTampon-colonnes]==1)devant = correspondance[indiceTampon-colonnes];
        if (indiceTampon<tampon.length-colonnes && tampon[indiceTampon+colonnes]==1)derriere = correspondance[indiceTampon+colonnes];
        if (indiceTampon%colonnes>0 && tampon[indiceTampon-1]==1)gauche= indexAct-1;
        if (indiceTampon%colonnes<colonnes-2 && tampon[indiceTampon+1]==1)droite= indexAct+1;

        List<Boolean[]> listeB = reciproque(indice);
        Boolean[] evite = listeB.get(0);
        Boolean[] evitePas = listeB.get(1);
        for (int i= 0; i< eleves.length; i++) {
            Log.d(TAG, "affinites: "+droite+" "+gauche+" "+devant+" "+derriere);
            if (isoler){
                laMatrice.get(i)[droite] -= 5* importance[i];
                laMatrice.get(i)[gauche] -= 5* importance[i];
                laMatrice.get(i)[devant] -= 5* importance[i]*0.75F;
                laMatrice.get(i)[derriere] -= 5* importance[i]*0.75F;
            }
            if (Boolean.TRUE.equals(evite[i])) {//à éviter
                laMatrice.get(i)[droite] -= affE* importance[i];
                laMatrice.get(i)[gauche] -= affE* importance[i];
                laMatrice.get(i)[devant] -= affE* importance[i]*0.75F;
                laMatrice.get(i)[derriere] -= affE* importance[i]*0.75F;
            }else if (Boolean.TRUE.equals(evitePas[i])) {// à !éviter
                laMatrice.get(i)[droite] += affI* importance[i];
                laMatrice.get(i)[gauche] += affI* importance[i];
            }
            if (!Objects.equals(donnees.get(indices.getInt(eleves[i] + classe, 0))[9], genre)){//alternance FG
                laMatrice.get(i)[droite] += fg* importance[i];
                laMatrice.get(i)[gauche] += fg* importance[i];
            }else{
                laMatrice.get(i)[droite] -= fg* importance[i];
                laMatrice.get(i)[gauche] -= fg* importance[i];
            }
            if((moteur && Boolean.parseBoolean(donnees.get(indices.getInt(eleves[i]+classe,0))[10])) ||(dys &&Boolean.parseBoolean(donnees.get(indices.getInt(eleves[i]+classe,0))[12]))){ //alternance DM
                laMatrice.get(i)[droite] += dm* importance[i];
                laMatrice.get(i)[gauche] += dm* importance[i];
            }
            if (fort<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[7])<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[7]) != fort){ //alternance FD
                laMatrice.get(i)[droite] += fd* importance[i];
                laMatrice.get(i)[gauche] += fd* importance[i];
            }
            if (calme<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[8])<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[8]) != calme){ //alternance AC
                laMatrice.get(i)[droite] += ac* importance[i];
                laMatrice.get(i)[gauche] += ac* importance[i];
            }
        }
        Log.i(TAG, "Informations affinités: OK");
    }

    public void obtienClasse3(String nomClasse){
        String savedString = prefListeEleve.getString(nomClasse, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        eleves = new String[st.countTokens()];
        for (int i = 0; i < eleves.length; i++) {
            eleves[i] = st.nextToken();
        }
        Log.d(TAG, "obtienClasse3: "+eleves.length);
    }

    public List<Boolean[]> reciproque(int indice){
        List<Boolean[]> liste = new ArrayList<>();
        List<Boolean[]> liste2 = new ArrayList<>();
        for (String eleve : eleves){
            StringTokenizer st = new StringTokenizer(donnees.get(indices.getInt(eleve+classe,0))[2],",");
            StringTokenizer st2 = new StringTokenizer(donnees.get(indices.getInt(eleve+classe,0))[3],",");
            int w = st.countTokens();
            int x = st2.countTokens();
            Boolean[] evite = new Boolean[eleves.length];
            Boolean[] evitePas = new Boolean[eleves.length];
            Arrays.fill(evite,false);
            Arrays.fill(evitePas,false);
            for (int i =0; i<w; i++){
                evite[i] = Boolean.parseBoolean(st.nextToken());
            }
            for (int i =0; i<x; i++){
                evitePas[i] = Boolean.parseBoolean(st2.nextToken());
            }
            liste.add(evite);
            liste2.add(evitePas);
        }
        for (int i=0;i< liste.size();i++){      //applique la réciprocité de l'éloignement
            for(int j = 0; j< liste.get(i).length;j++){
                if (Boolean.TRUE.equals(liste.get(i)[j])){
                    liste.get(j)[i] = true;
                    liste2.get(j)[i] = false;
                }else if(Boolean.TRUE.equals(liste2.get(i)[j])){
                    liste2.get(j)[i] = true;
                }
            }
        }
        List<Boolean[]> list3 = new ArrayList<>();
        list3.add(liste.get(indice));
        list3.add(liste2.get(indice));
        return list3;
    }

    private void laBoucle(){
        init();
        placement(5);
        if (prefsAlgo.getInt("ordre_alpha",0)+1 >0)ordreAlpha(prefsAlgo.getInt("ordre_alpha",0)+1);
        Log.d(TAG, "laBoucle: eleve4: "+ Arrays.toString(laMatrice.get(3)));
        isolement();
        Log.d(TAG, "laBoucle: eleve4: "+ Arrays.toString(laMatrice.get(3)));
        taille(prefsAlgo.getInt("taille",0)+1);
        String[] clone;
        clone = eleves.clone();
        for (int wse = 0; wse< eleves.length; wse++){
            Log.d(TAG, "laBoucle: eleve: "+ Arrays.toString(laMatrice.get(wse)));
            int max=0;
            List<Integer> indicesE = new ArrayList<>();  //indices élèves
            List<Integer> indices2 = new ArrayList<>(); //indices place
            for(int i =0; i< eleves.length;i++){    //cherche le(s) coef max
                if (clone[i]!= null){
                    for (int element = 0;element<laMatrice.get(i).length;element++){
                        if(Objects.equals(Rplace[element], "")) {  //element: indice de la place
                            if (laMatrice.get(i)[element] > max) {
                                max = laMatrice.get(i)[element];
                                indicesE.clear();
                                indices2.clear();
                                indices2.add(element);
                                indicesE.add(i);
                            } else if (laMatrice.get(i)[element] == max) {
                                indicesE.add(i);
                                indices2.add(element);
                            }
                        }
                    }
                }
                Log.d(TAG, "laBoucle: "+ Arrays.toString(laMatrice.get(i)));
            }

            int choix = (int) Math.floor(Math.random()*(indicesE.size()));
            Log.d(TAG, "laBoucle: "+indicesE.get(choix)+" "+indices2.get(choix));
            place[indicesE.get(choix)] =indices2.get(choix);
            clone[indicesE.get(choix)] = null;
            Rplace[indices2.get(choix)] = eleves[indicesE.get(choix)];
            affinites(indicesE.get(choix),prefsAlgo.getInt("affinites_e",0)+1,prefsAlgo.getInt("affinites_i",0)+1,prefsAlgo.getInt("alternance_fg",0)+1,prefsAlgo.getInt("alternance_ac",0)+1,prefsAlgo.getInt("alternance_fd",0)+1, prefsAlgo.getInt("associer_dm",0)+1);
            Log.d(TAG, "res: matrix"+ Arrays.toString(laMatrice.get(indicesE.get(choix))));
            Log.d(TAG, "res: place"+ Arrays.toString(place));
            Log.d(TAG, "res: Rplace"+ Arrays.toString(Rplace));
        }
        Log.i(TAG, "Informations laBoucle: OK");
        affiche();
        Log.i(TAG, "Informations affiche: OK");
    }

    public void affiche(){
        AlertDialog.Builder constructeur= new AlertDialog.Builder(this);
        constructeur.setTitle("Variante");
        ScrollView def = new ScrollView(this);
        HorizontalScrollView def2 = new HorizontalScrollView(this);
        def.setScrollContainer(true);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

        TableLayout table = new TableLayout(this);
        table.setScrollContainer(true);
        table.setLayoutParams(tableParams);
        table.setStretchAllColumns(true);
        table.setBackground(getDrawable(R.drawable.bords));
        def2.addView(table);
        def.addView(def2);
        constructeur.setView(def);
        int compte = 0;
        TableRow.LayoutParams params = new TableRow.LayoutParams(50, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2,10,2,10);
        for (int rang = 0; rang<tampon.length/colonnes;rang++){
            TableRow ligne = new TableRow(this);
            ligne.setLayoutParams(rowParams);
            ligne.setMinimumWidth(30);
            table.addView(ligne);
            for (int x =0; x<colonnes;x++){
                TextView nomEleve = new TextView(this);
                nomEleve.setLayoutParams(params);
                nomEleve.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                nomEleve.setPadding(2,1,2,1);
                ligne.addView(nomEleve);
                if (matrice[rang][x] == 1){
                    nomEleve.setBackground(getDrawable(R.drawable.bords));
                    nomEleve.setText(Rplace[compte]);
                    compte++;
                }
            }
        }
        constructeur.setPositiveButton("changer", (dialog, which) -> {
            dialog.dismiss();
            laBoucle();
        });
        constructeur.setNeutralButton("Enregistrer",(dialog,which) ->{
            if (ContextCompat.checkSelfPermission(ParametresAlgorithme.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
            }
            AlertDialog.Builder cons = new AlertDialog.Builder(this);
            cons.setTitle("Nom de l'image");
            EditText edit = new EditText(this);
            edit.setHint("nom de l'image (sans extension)");
            cons.setView(edit);
            cons.setPositiveButton("Valider", (dialog1, which1) -> {
                try{
                    def.setDrawingCacheEnabled(true);
                    Bitmap bitmap = def.getDrawingCache();
                    File file,f;
                    if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
                    {
                        file =new File(Environment.getExternalStorageDirectory() + "/Download/Plans_de_classe/");
                        if(!file.exists())
                        {
                            if(!file.mkdir()){
                                Toast.makeText(this,"Erreur, veuillez réessayer.",Toast.LENGTH_SHORT).show();
                            }
                        }
                        f = new File(file.getAbsolutePath()+File.separator+edit.getText().toString()+".png");
                        FileOutputStream ostream = new FileOutputStream(f);
                        if(bitmap.compress(Bitmap.CompressFormat.PNG, 10, ostream))Toast.makeText(this,"Image enregistrée dans les téléchargements!",Toast.LENGTH_SHORT).show();
                        else {Toast.makeText(this,"Erreur lors de l'enregistrement, réessayez plus tard",Toast.LENGTH_SHORT).show();}
                        ostream.close();
                    }else{
                        Toast.makeText(this,"Impossible d'enregistrer l'images. Configuration système invalide",Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "enregistrement: ",e );
                    Toast.makeText(this,"Impossible d'enregistrer l'image",Toast.LENGTH_LONG).show();
                }
            });
            cons.show();
        });
        try {
           constructeur.show();
        }catch (Exception e){
            Log.e(TAG, "affiche: ",e );
            Toast.makeText(this,"Erreur critique, contactez les développeurs",Toast.LENGTH_LONG).show();
        }
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
                soutient();
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
        SharedPreferences prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        prefsAlgo.edit().clear().apply();
        prefListeEleve.edit().clear().apply();
        config.edit().clear().apply();
        indices.edit().clear().apply();
        prefs.edit().clear().apply();
        File fich = new File((getExternalFilesDir(null) + "/donnees.csv"));
        if (!fich.delete())Toast.makeText(this,"impossible d'effacer toutes les données",Toast.LENGTH_LONG).show();
        else{
            onExplose();
        }
    }

    public void infos(){
        AlertDialog.Builder constr = new AlertDialog.Builder(this);
        constr.setTitle("Informations");
        constr.setMessage(String.format("Vous utilisez la %s de l'application.\n%s \nL'application a été développée par IPIC&cie.",getString(R.string.version),getString(R.string.notes_version)));
        constr.show();
    }
    public void soutient(){
        AlertDialog.Builder construit = new AlertDialog.Builder(this);
        construit.setTitle("Merci de votre soutient");
        construit.setMessage(":)");
        construit.show();
    }
}