package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class Algorithme extends AppCompatActivity {

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
    private int[] place;    //place assignée à chaque élève (indice: élève, valeur: place dans laMatrice)
    private String[] Rplace;   //place vide; indice: case de tampon, valeur: nom élève si occupée

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
        setContentView(R.layout.activity_algorithme);

        prefsAlgo = getBaseContext().getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        Log.d(TAG, "onCreate: preferences config "+config.getAll());
        Log.d(TAG, "onCreate: preferences indicess"+indices.getAll());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Log.d(TAG, "Infos: initialisation terminée");
        new CreationPlan().execute(0);
        findViewById(R.id.changer_plan).setOnClickListener(v-> laBoucle());
        findViewById(R.id.enregistrer_plan).setOnClickListener(v ->{
            if (ContextCompat.checkSelfPermission(Algorithme.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            AlertDialog.Builder cons = new AlertDialog.Builder(this);
            cons.setTitle("Nom de l'image");
            EditText edit = new EditText(this);
            edit.setHint("nom de l'image (sans extension)");
            cons.setView(edit);
            cons.setPositiveButton("Valider", (dialog1, which1) -> {
                try{
                    findViewById(R.id.ma_vue_qui_scroll).setDrawingCacheEnabled(true);
                    Bitmap bitmap =  findViewById(R.id.ma_vue_qui_scroll).getDrawingCache();
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
    }

    private class CreationPlan extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... integers) {
            laBoucle();
            Log.d(TAG, "doInBackground: OKOK");
            return "Finished!";
        }
        @Override
        protected void onPostExecute(String string) {
             affiche();
            Log.d(TAG, "onPostExecute: OK");
        }
    }

    private void laBoucle(){
        init();
        String[] clone = placeur();
        placement(2);
        if (prefsAlgo.getInt("ordre_alpha",0)>0)ordreAlpha(prefsAlgo.getInt("ordre_alpha",0));
        isolement();
        //taille(prefsAlgo.getInt("taille",0));
        int nbEleves=0;    //nb d'élèves à placer
        for (String e:clone){
            if(e != null) nbEleves++;
        }

        for (int wse = 0; wse< nbEleves; wse++){
            Log.d(TAG, "laBoucle: eleve: "+ Arrays.toString(laMatrice.get(wse)));
            int max=0;
            List<Integer> indicesE = new ArrayList<>();  //indices élèves
            List<Integer> indices2 = new ArrayList<>(); //indices place
            for(int i =0; i< eleves.length;i++){    //cherche le(s) coef max
                if (clone[i]!= null){
                    for (int element = 0; element<laMatrice.get(i).length;element++){
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
            Log.d(TAG, "laBoucle: "+choix);
            Log.d(TAG, "laBoucle: "+indicesE.get(choix)+" "+indices2.get(choix));
            place[indicesE.get(choix)] = indices2.get(choix);
            clone[indicesE.get(choix)] = null;
            Rplace[indices2.get(choix)] = eleves[indicesE.get(choix)];
            affinites(indicesE.get(choix),prefsAlgo.getInt("affinites_e",0),prefsAlgo.getInt("affinites_i",0),prefsAlgo.getInt("alternance_fg",0),prefsAlgo.getInt("alternance_ac",0),prefsAlgo.getInt("alternance_fd",0), prefsAlgo.getInt("associer_dm",0));
            Log.d(TAG, "res: matrix"+ Arrays.toString(laMatrice.get(indicesE.get(choix))));
            Log.d(TAG, "res: place"+ Arrays.toString(place));
            Log.d(TAG, "res: Rplace"+ Arrays.toString(Rplace));
        }
        Log.i(TAG, "Informations laBoucle: OK");
    }

    public void affiche(){
        TableLayout table = findViewById(R.id.ma_table);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams params = new TableRow.LayoutParams(50, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 10, 2, 10);
        int compte = 0;
        Log.d(TAG, "affiche: "+ Arrays.toString(Rplace));
        for (int rang = 0; rang<tampon.length/colonnes;rang++){
            TableRow ligne = new TableRow(this);
            ligne.setLayoutParams(rowParams);
            ligne.setMinimumWidth(30);
            table.addView(ligne);
            for (int x =0; x<colonnes;x++) {
                final Button nomEleve = new Button(this);
                nomEleve.setLayoutParams(params);
                nomEleve.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                nomEleve.setPadding(2, 1, 2, 1);
                ligne.addView(nomEleve);
                Log.d(TAG, "affiche: "+matrice[rang][0]+" "+compte);
                if (matrice[rang][x] == 1) {
                    nomEleve.setBackground(AppCompatResources.getDrawable(this, R.drawable.bords));
                    Log.d(TAG, "affiche:"+Rplace[compte]);
                    if (!Objects.equals(Rplace[compte], "")) {
                        nomEleve.setText(Rplace[compte]);
                        final String nom = Rplace[compte];

                        nomEleve.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                                .setTitle(nom)
                                .setMessage(prefListeEleve.getString(classe + nom, "[aucun commentaire]"))
                                .show());
                    } else {
                        nomEleve.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                                .setMessage("cette place est vide.")
                                .show());
                    }
                    compte++;
                }
            }
        }
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
        for(String[] i:dataList) {
            Log.d(TAG, "readCsvFile2: "+Arrays.toString(i));
        }
        return dataList;
    }

    public void obtienClasse3(String nomClasse){
        String savedString = prefListeEleve.getString(nomClasse, "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        eleves = new String[st.countTokens()];
        for (int i = 0; i < eleves.length; i++) {
            eleves[i] = st.nextToken();
        }
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
                laMatrice.get(i)[j] = val*2 - (Math.abs(Arrays.asList(elevesAlpha).indexOf(eleves[i])-j))*val;
            }
        }
        Log.i(TAG, "Informations ordreAlpha: OK"+ Arrays.toString(elevesAlpha));
    }

    public void taille (int val) {
        //TODO: bogue d'indice avec j: bonnes varaiable?
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
        //TODO:vérifier application uniquement sur les élèves  à isoler.
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
                laMatrice.get(i)[j] += 2*importance[i]*(4-isoVal[j]);
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
                laMatrice.get(i)[droite] -= 2 * importance[i];
                laMatrice.get(i)[gauche] -= 2 * importance[i];
                laMatrice.get(i)[devant] -= 2 * importance[i] * 0.9;
                laMatrice.get(i)[derriere] -= 2 * importance[i] * 0.9;
                if (indiceTampon%colonnes>0){ //gauche
                    if (indiceTampon/colonnes>0 && tampon[indiceTampon-colonnes -1]==1)laMatrice.get(i)[indiceTampon-colonnes -1] -= 2* importance[i]*0.8;//haut
                    if (indiceTampon<tampon.length-colonnes && tampon[indiceTampon+colonnes -1]==1)laMatrice.get(i)[indiceTampon+colonnes -1] -= 2* importance[i]*0.8;//bas
                }
                if(indiceTampon%colonnes<colonnes-2){//droite
                    if (indiceTampon/colonnes>0 && tampon[indiceTampon-colonnes -1]==1)laMatrice.get(i)[indiceTampon-colonnes +1] -= 2* importance[i]*0.8;//haut
                    if (indiceTampon<tampon.length-colonnes && tampon[indiceTampon+colonnes -1]==1)laMatrice.get(i)[indiceTampon+colonnes +1] -= 2* importance[i]*0.8;//bas
                }
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

    public String[] placeur(){  //place les élèves positionnés manuellement
        String[] clone = eleves.clone();
        StringTokenizer st3 = new StringTokenizer(prefsAlgo.getString(classe+"places", ""), ",");
        final String[] placeur = new String[Rplace.length];//config
        Arrays.fill(placeur, " ");
        int z = st3.countTokens();
        for (int i = 0; i < z; i++) {
            placeur[i] = st3.nextToken();
        }
        for (int indicePlace = 0; indicePlace<placeur.length; indicePlace++){
            String elv = placeur[indicePlace];
            if(!Objects.equals(elv, " ")){
                Log.d(TAG, "placeur: "+indicePlace+elv);
                Rplace[indicePlace] = elv;
                place[Arrays.asList(eleves).indexOf(elv)] = indicePlace;
                clone[Arrays.asList(eleves).indexOf(elv)] = null;
            }
        }
        return clone;
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
            default:
                return super.onOptionsItemSelected(item);
        }
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