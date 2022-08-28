package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        CheckBox x = findViewById(R.id.affinites_e);
        CheckBox affinitesE = findViewById(R.id.affinites_e);
        CheckBox affinitesI = findViewById(R.id.affinites_i);
        CheckBox vue = findViewById(R.id.vision);
        CheckBox taille = findViewById(R.id.haut);
        CheckBox alternanceFG = findViewById(R.id.alternance_fg);
        CheckBox alternanceAC = findViewById(R.id.alternance_ac);
        CheckBox alternanceFD = findViewById(R.id.alternance_fd);
        CheckBox associerDM = findViewById(R.id.associer_dm);
        CheckBox ordre_alpha = findViewById(R.id.ordre_alpha);
        SeekBar affiniteEniv = findViewById(R.id.affinites_e_niv);
        SeekBar affiniteIniv = findViewById(R.id.affinites_i_niv);
        SeekBar vueNiv = findViewById(R.id.vision_niv);
        SeekBar tailleNiv = findViewById(R.id.haut_niv);
        SeekBar alternanceFGNiv = findViewById(R.id.alternance_fg_niv);
        SeekBar alternanceACNiv = findViewById(R.id.alternance_ac_niv);
        SeekBar alternanceFDNiv = findViewById(R.id.alternance_fd_niv);
        SeekBar associerDMNiv = findViewById(R.id.associer_dm_niv);
        SeekBar ordre_alphaNiv = findViewById(R.id.ordre_alpha_niv);


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
            for (int i = 0; i< colonnes; i++) {
                matrice[y][i] = tampon[i+colonnes*(y)];
            }
        }
        int[] matrix = new int[compte];
        importance = new int[eleves.length];
        for(int i =0;i<eleves.length;i++){
            importance[i] = Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[13])+1;
            laMatrice.add(matrix.clone());
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
                //TODO: prendre en compte dyslexie
            }
        }
        for (int x:compteRang2){    //remplit rangs de derriere
            for (int i = 0; i<compteXrang2;i++){
                laMatrice.get(x)[laMatrice.get(x).length-i-1] += val * importance[x];
            }
        }
        Log.d(TAG, "Informations placement: OK");
    }

    public void ordreAlpha(int val){
        String[] elevesAlpha = eleves.clone();
        Arrays.sort(elevesAlpha);
        for(int i = 0; i < eleves.length; i++){
            laMatrice.get(i)[Arrays.asList(elevesAlpha).indexOf(eleves[i])] += val*5;
            Log.d(TAG, "ordreAlpha: "+ Arrays.toString(laMatrice.get(i))+ val*3);
        }
        Log.d(TAG, "Informations ordreAlpha: OK"+ Arrays.toString(elevesAlpha));
    }

    public void taille (int val) {
        List<Integer> compteTaille1 = new ArrayList<>();
        List<Integer> compteTaille2 = new ArrayList<>();
        List<Integer> compteTaille3 = new ArrayList<>();
        for (int i = 0; i < eleves.length; i++) {      //compte le nombre d'élève à placer
            if (Integer.parseInt(donnees.get(indices.getInt(eleves[i] + classe, 0))[4]) == 2) {
                compteTaille1.add(i);
            } else if (Integer.parseInt(donnees.get(indices.getInt(eleves[i] + classe, 0))[4]) == 0) {
                compteTaille2.add(i);
            } else {
                compteTaille3.add(i);
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
                    Log.d(TAG, "taille: "+i+" "+j+" "+ compteXrang2);
                    laMatrice.get(i)[j] += val * importance[j];
                }

            }
        }
    }

    public void affinites(int indice, int affE, int affI, int FG, int AC, int FD, int DM){
        int indexAct =  place[indice];
        int compte = 0;
        int indiceTampon =-1;
        while (compte < indexAct+1) {
            indiceTampon++;
            if (tampon[indiceTampon] == 1) compte++;
        }
        Log.d(TAG, "affinites: ind "+Arrays.asList(donnees.get(indices.getInt(eleves[indice]+classe,0))));
        String genre = donnees.get(indices.getInt(eleves[indice]+classe,0))[9];
        Boolean dys= Boolean.parseBoolean(donnees.get(indices.getInt(eleves[indice]+classe,0))[10]);
        Boolean moteur= Boolean.parseBoolean(donnees.get(indices.getInt(eleves[indice]+classe,0))[12]);
        int calme = Integer.parseInt(donnees.get(indices.getInt(eleves[indice]+classe,0))[8]);
        int fort = Integer.parseInt(donnees.get(indices.getInt(eleves[indice]+classe,0))[7]);
        int droite = indexAct;
        int gauche = indexAct;
        //TODO: devant derrière
        int devant = indexAct;
        int derriere = indexAct;
        //if (indiceTampon/colonnes>0 && tampon[indiceTampon-colonnes]==1)devant = indice-colonnes;
        //if (indiceTampon>tampon.length-colonnes && tampon[indiceTampon+colonnes]==1)derriere = indice+colonnes;
        if (indiceTampon%colonnes>0 && tampon[indiceTampon-1]==1)gauche= indexAct-1;
        if (indiceTampon%colonnes<colonnes-2 && tampon[indiceTampon+1]==1)droite= indexAct+1;

        StringTokenizer st = new StringTokenizer(donnees.get(indices.getInt(eleves[indice]+classe,0))[2], ",");
        StringTokenizer st2 = new StringTokenizer(donnees.get(indices.getInt(eleves[indice]+classe,0))[3], ",");
        int x= st.countTokens();
        for (int i= 0; i< x; i++) {
            String nom1 = st.nextToken();
            String nom2 = st2.nextToken();
            Log.d(TAG, "affinites: "+droite+" "+gauche+" "+devant+" "+derriere+" "+nom1);
            if (Boolean.parseBoolean(nom1)) {//à éviter
                laMatrice.get(i)[droite] -= affE* importance[i];
                laMatrice.get(i)[gauche] -= affE* importance[i];
                //laMatrice.get(Integer.parseInt(nom1))[devant] -= affE* importance[Integer.parseInt(nom1)]*0.75F;
                //laMatrice.get(Integer.parseInt(nom1))[derriere] -= affE* importance[Integer.parseInt(nom1)]*0.75F;
            }
            if (Boolean.parseBoolean(nom2)) {// à !éviter
                laMatrice.get(i)[droite] += affI* importance[i];
                laMatrice.get(i)[gauche] += affI* importance[i];
            }
            if (!Objects.equals(donnees.get(indices.getInt(eleves[i] + classe, 0))[9], genre)){//alternance FG
                laMatrice.get(i)[droite] += FG* importance[i];
                laMatrice.get(i)[gauche] += FG* importance[i];
            }else{
                laMatrice.get(i)[droite] -= FG* importance[i];
                laMatrice.get(i)[gauche] -= FG* importance[i];
            }
            if((moteur && Boolean.parseBoolean(donnees.get(indices.getInt(eleves[i]+classe,0))[10])) ||(dys &&Boolean.parseBoolean(donnees.get(indices.getInt(eleves[i]+classe,0))[12]))){ //alternance DM
                laMatrice.get(i)[droite] += DM* importance[i];
                laMatrice.get(i)[gauche] += DM* importance[i];
            }
            if (fort<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[7])<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[7]) != fort){ //alternance FD
                laMatrice.get(i)[droite] += FD* importance[i];
                laMatrice.get(i)[gauche] += FD* importance[i];
            }
            if (calme<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[8])<2 && Integer.parseInt(donnees.get(indices.getInt(eleves[i]+classe,0))[8]) != calme){ //alternance AC
                laMatrice.get(i)[droite] += AC* importance[i];
                laMatrice.get(i)[gauche] += AC* importance[i];
            }
        }
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

    private void laBoucle(){
        init();
        placement(5);
        ordreAlpha(prefsAlgo.getInt("ordre_alpha",0)+1);
        taille(prefsAlgo.getInt("taille",0)+1);
        String[] clone;
        clone = eleves.clone();
        for (int wse = 0; wse< eleves.length; wse++){
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
            Log.d(TAG, "res: "+ Arrays.toString(laMatrice.get(indicesE.get(choix))));
            Log.d(TAG, "res: "+ Arrays.toString(place));
            Log.d(TAG, "res: "+ Arrays.toString(Rplace));
        }
        affiche();
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