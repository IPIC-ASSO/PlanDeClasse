package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;


public class AlgorithmeContraingnant extends AppCompatActivity {

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

    private List<Boolean[]> affinitesE;     //pour chaque élève, la liste des élèves à éviter
    
    private List<Boolean[]> affinitesI;     //pour chaque élève, la liste des élèves à se rapprocher
    private int maxTolere = 3;  //niveau de correspondance
    private float contrainteAct = 0;


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
        Log.d(TAG, "onCreate: preferences algo"+prefsAlgo.getAll());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        new CreationPlan().execute(0);

        findViewById(R.id.enregistrer_plan).setOnClickListener(v ->{
            if (ContextCompat.checkSelfPermission(AlgorithmeContraingnant.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
            arbreQuiGrandit();
            Log.d(TAG, "doInBackground: "+maxTolere);
            Log.d(TAG, "doInBackground: "+ Arrays.toString(eleves));
            Log.d(TAG, "doInBackground: "+ Arrays.toString(place));
            Log.d(TAG, "doInBackground: "+ Arrays.toString(Rplace));
            return "Finished!";
        }
        @Override
        protected void onPostExecute(String string) {
            affiche();
            Log.d(TAG, "onPostExecute: OK");
        }
    }


    public void arbreQuiGrandit(){
        initialisation();
        String[] clone = placeur(); //élèves déjà placés
        boolean x = boucleRecursive(0);
        while (Boolean.FALSE.equals(x)){
            maxTolere+=1;
            x = boucleRecursive(0);
        }
    }

    public boolean boucleRecursive(int indiceEleve){
        Log.d(TAG, "boucleRecursive: "+indiceEleve);
        if (indiceEleve == eleves.length){
            Log.d(TAG, "boucleRecursive: fini!");
            return true;
        }
        if (place[indiceEleve]!=-1){
            return(boucleRecursive(indiceEleve+1));
        }
        for (int indicePlace = 0; indicePlace < Rplace.length ; indicePlace++){
            if (Objects.equals(Rplace[indicePlace], "")){  //place libre
                float contrainteCalc = contrainte(indicePlace,indiceEleve);
                if (contrainteAct+contrainteCalc<= maxTolere){
                    contrainteAct+=contrainteCalc;
                    Rplace[indicePlace]=eleves[indiceEleve];
                    Log.d(TAG, "lance_appel: "+ Arrays.toString(Rplace)+ contrainteAct);
                    place[indiceEleve] = indicePlace;
                    if(boucleRecursive(indiceEleve+1)) return true;
                    else{
                        Log.d(TAG, "boucleRecursive: appel_raté");
                        contrainteAct-=contrainteAct;
                        Rplace[indicePlace]="";
                        place[indiceEleve] = -1;
                    }
                }
            }
        }
        return false;
    }


    public float contrainte(int indicePlace, int indiceEleve){
        float maContrainte = 0;
        if (prefsAlgo.getInt("ordre_alpha",0)>0)
            maContrainte += verifOrdreAlpha(indicePlace,indiceEleve,prefsAlgo.getInt("ordre_alpha",0))*importance[indiceEleve];
        if (prefsAlgo.getInt("affinites_e",0)>0 || prefsAlgo.getInt("affinites_i",0)>0)
            maContrainte += verifAffinites(indicePlace, indiceEleve, prefsAlgo.getInt("affinites_e",0),prefsAlgo.getInt("affinites_i",0));
        if (prefsAlgo.getInt("taille",0)>0)
            maContrainte += verifTaille(indicePlace,indiceEleve,prefsAlgo.getInt("taille",0));
        if (prefsAlgo.getInt("alternance_fg",0)>0 || prefsAlgo.getInt("alternance_ac",0)>0 || prefsAlgo.getInt("alternance_fd",0)>0)
            maContrainte += verifalternances(indicePlace, indiceEleve, prefsAlgo.getInt("alternance_fg",0),prefsAlgo.getInt("alternance_ac",0), prefsAlgo.getInt("alternance_fd",0));
        Log.d(TAG, "contrainte: "+maContrainte);
        return maContrainte;
    }

    public float verifOrdreAlpha(int indicePlace, int indiceEleve, int importance){
        float x = 0;
        int petiIndice = indicePlace;
        while(petiIndice>0 && Objects.equals(Rplace[petiIndice], "")){
            petiIndice-=1;
        }
        if (indiceEleve!=0 && (eleves[indiceEleve]).toLowerCase().compareTo(Rplace[petiIndice].toLowerCase())<0){
            x+=importance/2F;
        }
        petiIndice = indicePlace;
        while(petiIndice<Rplace.length-1 && Objects.equals(Rplace[petiIndice], "")){
            petiIndice+=1;
        }
        if (petiIndice!= Rplace.length-1 && eleves[indiceEleve].toLowerCase().compareTo(Rplace[petiIndice].toLowerCase())>0) x+=importance/2F;
        return x;
    }

    public float verifAffinites(int indicePlace, int indiceEleve, int importanceE, int importanceI){
        float x = 0;
        if (indicePlace>0 && indicePlace%colonnes>0 && !Objects.equals(Rplace[indicePlace - 1], "")){   //gauche
            int indexElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace-1]);
            if (Boolean.TRUE.equals(affinitesE.get(indiceEleve)[indexElv]))x+=importanceE;
            if (Boolean.TRUE.equals(affinitesI.get(indiceEleve)[indexElv]))x-=importanceI;
        }
        if (indicePlace< Rplace.length-1 && (indicePlace+1)%colonnes<colonnes && !Objects.equals(Rplace[indicePlace + 1], "")){
            int indexElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace + 1]);
            if (Boolean.TRUE.equals(affinitesE.get(indiceEleve)[indexElv]))x+=importanceE;
            if (Boolean.TRUE.equals(affinitesI.get(indiceEleve)[indexElv]))x-=importanceI;
        }
        return x;
    }

    public float verifTaille(int indicePlace, int indiceEleve, int importance){
        float x = 0;
        if (indicePlace>= colonnes){
            int taille = Integer.parseInt(donnees.get(indiceEleve)[4]);
            if(!Objects.equals(Rplace[indicePlace - colonnes], "")){    //devant
                int indiceElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace -colonnes]);
                if (Integer.parseInt(donnees.get(indiceElv)[4])>taille) x+=importance/2F;
            }
            if (indicePlace%colonnes>0 && !Objects.equals(Rplace[indicePlace - colonnes - 1], "")){     //gauche
                int indiceElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace - colonnes -1]);
                if (Integer.parseInt(donnees.get(indiceElv)[4])>taille) x+=importance/2F;
            }
            if ((indicePlace+1)%colonnes<colonnes && !Objects.equals(Rplace[indicePlace + 1 - colonnes], "")){      //droite
                int indiceElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace - colonnes +1]);
                if (Integer.parseInt(donnees.get(indiceElv)[4])>taille) x+=importance/2F;
            }
        }
        return x;
    }

    public float verifalternances(int indicePlace, int indiceEleve, int importanceFG, int importanceAC, int importanceFD){
        float x = 0;
        String genre = donnees.get(indiceEleve)[9];
        Log.d(TAG, "verifalternances: "+donnees.get(indiceEleve)[7]);
        int attitude = Integer.parseInt(donnees.get(indiceEleve)[8]);   //0->agite; 1->calme; 2-> normal
        int niveau = Integer.parseInt(donnees.get(indiceEleve)[7]);     //0-> dif; 1->aise; 2->dans la moyenne
        if (indicePlace>0 && indicePlace%colonnes>0 && !Objects.equals(Rplace[indicePlace - 1], "")) {   //gauche
            int indiceElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace-1]);
            int attitudeElv = Integer.parseInt(donnees.get(indiceElv)[8]);
            int niveauElv = Integer.parseInt(donnees.get(indiceElv)[7]);
            Log.d(TAG, "verifalternances2: "+niveauElv);
            if (Objects.equals(donnees.get(indiceElv)[9], genre)) x+=importanceFG/2F;
            if (Math.abs(attitudeElv-attitude)==2 ) x+= importanceAC/2F;
            else if (Math.abs(attitudeElv-attitude)==0 && attitudeElv == 0) x+= importanceAC;
            if (Math.abs(niveauElv-niveau) == 2) x+= importanceFD/2F;
            else if (Math.abs(niveau-niveauElv)==0 && niveauElv == 0) x+= importanceFD;
        }
        if (indicePlace< Rplace.length-1 && (indicePlace+1)%colonnes<colonnes && !Objects.equals(Rplace[indicePlace + 1], "")){   //droite
            int indiceElv = Arrays.asList(eleves).indexOf(Rplace[indicePlace+1]);
            int attitudeElv = Integer.parseInt(donnees.get(indiceElv)[8]);
            int niveauElv = Integer.parseInt(donnees.get(indiceElv)[7]);
            if (Objects.equals(donnees.get(indiceElv)[9], genre)) x+=importanceFG/2F;
            if (Math.abs(attitudeElv-attitude)==1 && (attitudeElv == 2|| attitude ==2)) x+= importanceAC/2F;
            else if (Math.abs(attitudeElv-attitude)==0 && attitudeElv == 2) x+= importanceAC;
            if (Math.abs(niveauElv-niveau)==1 && (niveauElv == 0|| niveau ==0)) x+= importanceFD/2F;
            else if (Math.abs(niveau-niveauElv)==0 && niveauElv == 0) x+= importanceFD;
        }
        return x;
    }

    public void initialisation(){
        MesOutils monOutil = new MesOutils(this);
        classe = getIntent().getStringExtra("classe");  //nom classe
        eleves = monOutil.obtienClasse(classe);     //liste des élèves
        donnees = new ArrayList<>();
        laMatrice = new ArrayList<>();      //liste de liste, par indice, des places pour chaque élève
        List<String[]> datum = monOutil.litFichierCsv();
        for (String eleve: eleves){
            Log.d(TAG, "initialisation: "+ Arrays.toString(datum.get(indices.getInt(eleve + classe, 0))));
            donnees.add(datum.get(indices.getInt(eleve+classe,0)));
        }
        //donnees.remove(0);//supprime la ligne d'en-tête
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
            importance[i] = Integer.parseInt(donnees.get(i)[13])+1;
            laMatrice.add(new int[compte]);
        }
        place = new int[eleves.length];
        Rplace= new String[compte];
        Arrays.fill(Rplace,"");
        Arrays.fill(place,-1);
        Map<String, ?> tousParams = prefsAlgo.getAll();
        for (Map.Entry<String, ?> entry : tousParams.entrySet()) {
            if(!entry.getKey().contains("place")) maxTolere+=eleves.length * (int)entry.getValue();
        }
        //maxTolere = maxTolere/2;
        maxTolere = 0;
        reciproque();   //verif reciprocité affinités
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

    public void reciproque(){      //vérifie et applique la réciprocité des affinités
        affinitesE = new ArrayList<>();
        affinitesI = new ArrayList<>();
        for (int ind=0; ind<eleves.length;ind++){
            StringTokenizer st = new StringTokenizer(donnees.get(ind)[2],",");
            StringTokenizer st2 = new StringTokenizer(donnees.get(ind)[3],",");
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
            affinitesE.add(evite);
            affinitesI.add(evitePas);
        }
        for (int i=0;i< affinitesE.size();i++){      //applique la réciprocité de l'éloignement
            for(int j = 0; j< affinitesE.get(i).length;j++){
                if (Boolean.TRUE.equals(affinitesE.get(i)[j])){
                    affinitesE.get(j)[i] = true;
                    affinitesI.get(j)[i] = false;
                }else if(Boolean.TRUE.equals(affinitesI.get(i)[j])){
                    affinitesI.get(j)[i] = true;
                }
            }
        }
        for (Boolean[] aff:affinitesE) Log.d(TAG, "reciproque: "+ Arrays.toString(aff));
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
                if (matrice[rang][x] == 1) {
                    nomEleve.setBackground(AppCompatResources.getDrawable(this, R.drawable.bords));
                    if (!Objects.equals(Rplace[compte], "")) {
                        nomEleve.setText(Rplace[compte]);
                        final String nom = Rplace[compte];

                        nomEleve.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
                                .setTitle("Commentaire")
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