package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class ListeEleves extends AppCompatActivity {

    private String[] eleves;
    private SharedPreferences prefListeEleve;
    private SharedPreferences indices;
    private SharedPreferences config;
    private SharedPreferences prefsAlgo;
    private List<String[]> donnees;
    private String classe;
    private int[]tampon;
    private int nbEleves;
    private int colonnes;
    private boolean phaseFinale = false;
    private List<Button> boutonsPlacement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_eleves);
        prefListeEleve = getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);//élèves d'une classe  {"classe" -->"élève"}
        indices = getBaseContext().getSharedPreferences("eleves", Context.MODE_PRIVATE);//indice de l'élève dans la DB. {"eleve"+"classe" --> int}
        config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        prefsAlgo = getBaseContext().getSharedPreferences("algo", Context.MODE_PRIVATE);//préférences de l'algorithme.
        Log.d(TAG, "registre 1 "+prefListeEleve.getAll());
        Log.d(TAG, "registre 2"+indices.getAll());
        Log.d(TAG, "registre 3"+config.getAll());
        donnees = new MesOutils(this).litFichierCsv();
        classe = getIntent().getStringExtra("classe");
        obtienClasse2(classe);
        phaseFinale = indices.getBoolean("phasefinale"+classe,false);
        if(phaseFinale && getIntent().getBooleanExtra("debut",false)){
            Intent intention = new Intent(this, ParametresAlgorithme.class);
            intention.putExtra("classe",classe);
            startActivity(intention);
        }
        if (phaseFinale){
            Button creerPlan = findViewById(R.id.vers_realiser_plan);
            creerPlan.setText("Creer un plan de Classe");
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Parametrage des élèves")
                    .setMessage("Cliquez sur chaque élève et définissez ses carctéristiques, puis passez à la réalisation du plan de classe")
                    .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        }
        inflation();
        com.google.android.material.floatingactionbutton.FloatingActionButton plus = findViewById(R.id.nouv_eleve);
        plus.setOnClickListener(v->{
            if (phaseFinale)generateurEleve(-1, "","",true);
            else generateurNomEleve();
        });
        findViewById(R.id.vers_realiser_plan).setOnClickListener(v -> {
            if (phaseFinale){
                boolean drapeau = false;
                for (String eleve:eleves){
                    if (eleve!= null && indices.getInt(eleve+classe,-1)==-1){
                        drapeau = true;     //un élève n'est pas entièrement configuré
                        Log.d(TAG, "onCreate: "+ Arrays.toString(eleves));
                    }
                }
                if (drapeau){
                    new MaterialAlertDialogBuilder(this).setMessage("Liste incomplète")
                            .setMessage("Vous n'avez pas rempli les caractéristiques de certains élèves (fond coloré). \nSouhaitez vous quand même réaliser un plan de classe?")
                            .setPositiveButton("Réaliser",((dialogInterface, i) -> {
                                for (String eleve:eleves){
                                    if (eleve!= null && indices.getInt(eleve+classe,-1)==-1){
                                        String[] d = new String[]{classe,eleve,"","","2", "1","2","2","2","2","false","false","false","0"};
                                        indices.edit().putInt(eleve+classe,donnees.size()).apply();
                                        donnees.add(d);
                                    }
                                }
                                Log.d(TAG, "onCreate: donnees"+donnees);
                                writeToCSVFile(donnees);
                                Intent intention = new Intent(this, AlgorithmeContraingnant.class);
                                intention.putExtra("classe",classe);
                                startActivity(intention);
                            }))
                            .setNegativeButton(getString(R.string.txt_annuler),((dialogInterface, i) -> dialogInterface.dismiss()))
                            .show();
                }else{
                    Log.d(TAG, "onCreate: donnees"+ Arrays.toString(donnees.toArray()));
                    Intent intention = new Intent(this, AlgorithmeContraingnant.class);
                    intention.putExtra("classe",classe);
                    startActivity(intention);
                }
            }else{

                Intent intention = new Intent(this, ParametresAlgorithme.class);
                intention.putExtra("classe",classe);
                indices.edit().putBoolean("phasefinale"+classe,true).apply();
                startActivity(intention);
            }
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
        nbEleves = 0;
        int compte = 0;
        int x = st2.countTokens();
        int w = st.countTokens();
        tampon = new int[st2.countTokens()-1];
        for (int i = 0; i< x-1; i++){
            tampon[i] = Integer.parseInt(st2.nextToken());
            if (tampon[i] == 1)compte++;
        }
        colonnes = Integer.parseInt(st2.nextToken());
        eleves = new String[compte];
        try {
            for (int i = 0; i < w; i++) {
                eleves[i] = st.nextToken();
                nbEleves += 1;
            }
        }catch( Exception e){
               Toast.makeText(this,"Erreur critique, contactez les développeurs.",Toast.LENGTH_LONG).show();
        }
    }

    public void inflation(){
        LinearLayout liste = findViewById(R.id.liste_eleve);
        LayoutInflater inflater = this.getLayoutInflater();
        liste.removeAllViews();
        if (nbEleves !=0) {
            findViewById(R.id.bienvenue_eleve).setVisibility(View.GONE);
            for (int i=0; i<nbEleves;i++){
                String eleve = eleves[i];
                View vue = inflater.inflate(R.layout.profil_eleve, null);
                TextView nom = vue.findViewById(R.id.nom);
                nom.setText(eleve);
                TextView sup = vue.findViewById(R.id.suplement);
                sup.setText(prefListeEleve.getString(classe+eleve," "));
                if (phaseFinale)vue.setOnClickListener(v -> generateurEleve(indices.getInt(eleve+classe,-1),eleve, prefListeEleve.getString(classe+eleve,""),false));   //modifier l'élève
                ImageView image = vue.findViewById(R.id.couleur);
                if (indices.getInt(eleve+classe,-1) != -1){ //élève enregistré dans la DB
                    Log.d(TAG, "inflation: "+indices.getInt(eleve+classe,-1)+" "+donnees.size());
                    if (Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[10])&& Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==1){
                        image.setColorFilter(Color.argb(255, 255, 255, 0));
                    } else if (Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[10]) ||Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[11])||Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==0){
                        image.setColorFilter(Color.argb(255, 255, 0, 0));
                    }else if(Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[12])|| Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==1){
                        image.setColorFilter(Color.argb(255, 0, 255, 0));
                    }else{
                        image.setColorFilter(Color.argb(255, 255, 255, 0));
                    }
                }else {  //nouvel élève pas encore enregistré dans la DB
                    Log.d(TAG, "inflation: "+"OKOKOK");
                    vue.findViewById(R.id.fond_profil_eleve).setBackgroundColor(Color.argb(255,150,200,200));
                }
                liste.addView(vue);
            }
        }
        if (nbEleves == eleves.length) {
            findViewById(R.id.nouv_eleve).setVisibility(View.GONE);
        }
        if(nbEleves >2){
            Button realise = findViewById(R.id.vers_realiser_plan);
            realise.setVisibility(View.VISIBLE);
            if (!phaseFinale) realise.setText(R.string.btn_liste_eleve_suivant);
            Space space = new Space(this);
            space.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,100));
            liste.addView(space);
        }
        Log.d(TAG, "inflation: "+nbEleves);
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

    private void writeToCSVFile (List<String[]> dataList) {
        String csv = (getExternalFilesDir(null) + "/donnees.csv");
        try (CSVWriter writer = new CSVWriter(new FileWriter(csv), ';', ICSVWriter.NO_QUOTE_CHARACTER,
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.RFC4180_LINE_END)) {
            writer.writeAll(dataList);
        } catch (IOException e) {
            Toast.makeText(this,"erreur d'écriture des données",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void generateurEleve (int indice, String nomDeEleve, String commentaireDeEleve, boolean nouveau){
        Dialog constructeur = new Dialog(this);
        View vue = genereVue();
        constructeur.setContentView(vue,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
        Button placer = vue.findViewById(R.id.place);

        vue.findViewById(R.id.afficher_plus_de_parametre).setOnClickListener(v -> {
            if (vue.findViewById(R.id.plus_de_parametres).getVisibility()==View.VISIBLE)vue.findViewById(R.id.plus_de_parametres).setVisibility(View.GONE);
            else vue.findViewById(R.id.plus_de_parametres).setVisibility(View.VISIBLE);
        });

        int compte = 0; //nombre d'élèves enregistrés dans la classe
        for (String eleve: eleves){
            if (eleve != null)compte+=1;
        }
        boolean[] selection = new boolean[compte];
        boolean[] selection2 = new boolean[compte];
        Arrays.fill(selection2,false);
        Arrays.fill(selection,false);
        nom.setText(nomDeEleve);
        commentaire.setText(commentaireDeEleve);
        if (indice>=0){ //chargement des données enregistrées sur l'élève
            constructeur.setTitle("Modifier les informations");
            try{
            String[] d = donnees.get(indice);
            dys.setChecked(Boolean.parseBoolean(d[10]));
            iso.setChecked(Boolean.parseBoolean(d[11]));
            moteur.setChecked(Boolean.parseBoolean(d[12]));
            prio.setProgress(Integer.parseInt(d[6]));
            switch (Integer.parseInt(d[4])){    //taille
                case 0:
                    taille.check(R.id.taille_1);
                    break;
                case 2:
                    taille.check(R.id.taille_3);
                    break;
                default:
                    taille.check(R.id.taille_2);
                    break;
            }
            switch (Integer.parseInt(d[5])){    //vue
                case 0:
                    vision.check(R.id.vue_1);
                    break;
                case 2:
                    vision.check(R.id.vue_3);
                    break;
                default:
                    vision.check(R.id.vue_2);
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
                    difficultes.check(R.id.diff_1);
                    break;
                case 1:
                    difficultes.check(R.id.diff_2);
                    break;
                default:
                    difficultes.check(R.id.diff_3);
                    break;
            }
            switch (Integer.parseInt(d[8])){    //attitude
                case 0:
                    attitude.check(R.id.att_1);
                    break;
                case 1:
                    attitude.check(R.id.att_2);
                    break;
                default:
                    attitude.check(R.id.att_3);
                    break;
            }
                //genre
            if (d[9].equals("0"))genre.check(R.id.fille);
            else genre.check(R.id.garcon);

            StringTokenizer stEvite = new StringTokenizer(d[2], ",");
            StringTokenizer stCorrecte = new StringTokenizer(d[3], ",");
            int x= stEvite.countTokens();
            for (int i= 0; i< x; i++){
                selection[i]=Boolean.parseBoolean(stEvite.nextToken());
                selection2[i]=Boolean.parseBoolean(stCorrecte.nextToken());
            }
            }catch (Exception e){
                Log.e(TAG, "generateurEleve: ", e);
                Toast.makeText(this,"Incompatibilités de versions détectées.Veuillez réinitialiser l'application",Toast.LENGTH_LONG).show();
            }
        }else{
            constructeur.setTitle("Configurer l'élève");
        }

        evite.setOnClickListener(x ->{
            AlertDialog.Builder constr = new AlertDialog.Builder(this);
            constr.setTitle("Doit éviter...");
            ListView liste = new ListView(this);
            AutreAdaptateurAdapte customAdapter = new AutreAdaptateurAdapte(getApplicationContext(), eleves, selection, nom.getText().toString());
            liste.setAdapter(customAdapter);
            constr.setView(liste);
            constr.setPositiveButton(R.string.txt_valider, (dialog, which) -> dialog.dismiss());
            constr.show();
        });
        correcte.setOnClickListener(x ->{
            AlertDialog.Builder constr = new AlertDialog.Builder(this);
            constr.setTitle("Devrait être avec...");
            ListView liste = new ListView(this);
            AutreAdaptateurAdapte customAdapter = new AutreAdaptateurAdapte(getApplicationContext(), eleves, selection2, nom.getText().toString());
            liste.setAdapter(customAdapter);
            constr.setView(liste);
            constr.setPositiveButton(getString(R.string.txt_valider), (dialog, which) -> dialog.dismiss());
            constr.show();
        });
        enregistrer.setOnClickListener(w ->{
            Log.d(TAG, "generateurEleve: "+!(nbEleves>=eleves.length && nouveau)+ !nom.getText().toString().equals("") + !(genre.getCheckedRadioButtonId()!= R.id.fille && genre.getCheckedRadioButtonId()!=R.id.garcon) + !(Arrays.asList(eleves).contains(nom.getText().toString()) && indice<0));
            if (!(nbEleves>=eleves.length && nouveau) &&  !nom.getText().toString().equals("") && !((Arrays.asList(eleves).contains(nom.getText().toString())) && (nomDeEleve.equals("") || !Objects.equals(nomDeEleve, nom.getText().toString())))){
                if (!Objects.equals(nomDeEleve, "")){  //élève déjà enregistré (modifications)
                    eleves[Arrays.asList(eleves).indexOf(nomDeEleve)] = nom.getText().toString();
                }else{
                    eleves[nbEleves] = nom.getText().toString();    //ajoute le nom du nouvel élève
                    nbEleves+=1;
                }
                for(int i = 0; i<selection.length;i++){ //met à jour les affinités
                    if (selection[i] && selection2[i]){
                        Log.d(TAG, "generateurEleve: Incohérence");
                        selection[i] = false;
                        selection2[i] = false;
                        Toast.makeText(this,"Incohérences dans les choix: un élève ne peut être éloigné et rapproché d'un même élève.",Toast.LENGTH_LONG).show();
                    }
                }
                StringBuilder strEvite = new StringBuilder();   //convertit en chaine de cararctère les indices d'élèves à (!)éviter
                for (Boolean eviteI : selection) {
                    strEvite.append(eviteI).append(",");
                }
                StringBuilder strCorrecte = new StringBuilder();
                for (Boolean correcteI : selection2) {
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
                    case R.id.garcon:
                        genE = "1";
                        break;
                    default:
                        genE = "2";
                        break;
                }

                String[] d = new String[]{classe,nom.getText().toString(),strEvite.toString(),strCorrecte.toString(),tailleE, vueE,placement,difE,attE,genE,String.valueOf(dys.isChecked()),String.valueOf(iso.isChecked()),String.valueOf(moteur.isChecked()),String.valueOf(prio.getProgress())};
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
                constructeur.setContentView(barre);
                writeToCSVFile(donnees);
                inflation();
                constructeur.dismiss();
            }else if(nbEleves>=eleves.length && nouveau) {
                Toast.makeText(this, "Enregistrement impossible. Trop d'élèves pour cette classe. Veuillez d'abord modifier sa configuration.", Toast.LENGTH_LONG).show();
            }else if((Arrays.asList(eleves).contains(nom.getText().toString())) && (nouveau || Objects.equals(donnees.get(Math.max(indice,0))[1], nom.getText().toString()))){
                Toast.makeText(this,"Enregistrement impossible. Un élève du même nom existe déjà dans cette classe.",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Enregistrement impossible. Veuillez remplir tous les champs marqués d'une astérisque.",Toast.LENGTH_LONG).show();
            }
        });
        placer.setOnClickListener(zer -> placeManuel(nomDeEleve, constructeur));

        constructeur.show();
    }

    public void generateurNomEleve(){
        AlertDialog.Builder constr = new AlertDialog.Builder(this);
        constr.setTitle("Nouvel élève");
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        EditText nomEleve = new EditText(this);
        EditText commentaire = new EditText(this);
        nomEleve.setHint("Nom de l'élève");
        nomEleve.setAllCaps(true);
        commentaire.setHint("Commentaire");
        linearLayout.addView(nomEleve);
        linearLayout.addView(commentaire);
        constr.setView(linearLayout);
        constr.setPositiveButton("Valider", (dialogInterface, i) -> {
            if (!Arrays.asList(eleves).contains(nomEleve.getText().toString())){
                prefListeEleve.edit().putString(classe,prefListeEleve.getString(classe,"")+nomEleve.getText().toString()+",").apply();
                prefListeEleve.edit().putString(classe+nomEleve.getText().toString(), commentaire.getText().toString()).apply();
                eleves[nbEleves] = nomEleve.getText().toString();    //ajoute le nom du nouvel élève
                nbEleves+=1;
                dialogInterface.dismiss();
                inflation();
            }else{
                Toast.makeText(this, "Un élève du même nom existe déjà", Toast.LENGTH_SHORT).show();
            }
        });
        constr.setNeutralButton("Annuler",((dialogInterface, i) -> dialogInterface.dismiss()));
        constr.show();
    }

    public void placeManuel(String eleve, Dialog constr) {
        StringTokenizer st3 = new StringTokenizer(prefsAlgo.getString(classe+"places", ""), ",");
        final String[] placeur = new String[eleves.length];//config
        Arrays.fill(placeur, " ");
        Log.d(TAG, "placeur man: "+eleves.length);
        int z = st3.countTokens();
        for (int i = 0; i < z; i++) {
            placeur[i] = st3.nextToken();
        }

        AlertDialog.Builder constructeur2 = new AlertDialog.Builder(this);
        constructeur2.setTitle("Variante");
        boutonsPlacement = new ArrayList<>();
        ScrollView def = new ScrollView(this);
        HorizontalScrollView def2 = new HorizontalScrollView(this);
        def.setScrollContainer(true);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(ScrollView.LayoutParams.WRAP_CONTENT, ScrollView.LayoutParams.MATCH_PARENT);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        TableLayout table = new TableLayout(this);
        table.setScrollContainer(true);
        table.setLayoutParams(tableParams);
        table.setStretchAllColumns(true);
        def2.addView(table);
        def.addView(def2);
        constructeur2.setView(def);
        TableRow.LayoutParams params = new TableRow.LayoutParams(50, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(2, 10, 2, 10);
        int compte2 = -1;
        for (int rang = 0; rang < tampon.length / colonnes; rang++) {
            TableRow ligne = new TableRow(this);
            ligne.setLayoutParams(rowParams);
            ligne.setMinimumWidth(30);
            table.addView(ligne);
            for (int x = 0; x < colonnes; x++) {
                Button nomEleve = new Button(this);
                nomEleve.setLayoutParams(params);
                nomEleve.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                nomEleve.setPadding(2, 1, 2, 1);
                ligne.addView(nomEleve);
                if (tampon[rang * colonnes + x] == 1) {
                    boutonsPlacement.add(nomEleve);
                    nomEleve.setBackground(AppCompatResources.getDrawable(this,R.drawable.bords));
                    compte2++;
                    final int truc = compte2;
                    if (!Objects.equals(placeur[compte2], " ")) nomEleve.setText(placeur[compte2]);
                    else nomEleve.setOnClickListener(v -> {
                        if (Arrays.asList(placeur).contains(eleve)){
                            boutonsPlacement.get(Arrays.asList(placeur).indexOf(eleve)).setText(" ");
                            placeur[Arrays.asList(placeur).indexOf(eleve)]= " ";
                        }
                        placeur[truc] = eleve;
                        nomEleve.setText(eleve);
                    });
                }
            }
        }
        constructeur2.setPositiveButton("Enregistrer", (dialogInterface, i) -> {
            StringBuilder strPlacement = new StringBuilder();
            for (String place : placeur) {
                strPlacement.append(place).append(",");
            }
            prefsAlgo.edit().putString(classe+"places", strPlacement.toString()).apply();
            dialogInterface.dismiss();
            constr.show();
        });
        constructeur2.setNeutralButton("Annuler", ((dialogInterface, i) -> {
            dialogInterface.dismiss();
            constr.show();
        }));
        try {
            constructeur2.show();
        } catch (Exception e) {
            Log.e(TAG, "affiche: ", e);
            Toast.makeText(this, "Erreur critique, contactez les développeurs", Toast.LENGTH_LONG).show();
        }
    }

    public View genereVue(){
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.generateur_eleve, null);
        LinearLayout cache = vue.findViewById(R.id.plus_de_parametres);
        LinearLayout autreParams = vue.findViewById(R.id.autre_params);
        RadioGroup taille = vue.findViewById(R.id.taille);
        RadioGroup vision = vue.findViewById(R.id.vue);
        RadioGroup difficultes = vue.findViewById(R.id.difficultes);
        RadioGroup attitude = vue.findViewById(R.id.attitude);
        RadioGroup genre = vue.findViewById(R.id.genre);
        TextView txtVue = vue.findViewById(R.id.txt_vue);
        TextView txtTaille = vue.findViewById(R.id.txt_taille);
        TextView txtGenre = vue.findViewById(R.id.txt_genre);
        TextView txtSerieux = vue.findViewById(R.id.txt_serieux);
        TextView txtNiveau = vue.findViewById(R.id.txt_niveau);

        for (int i = 2; i>0; i--){
            if (prefsAlgo.getInt("vue",0)==i){
                cache.removeView(txtVue);
                autreParams.addView(txtVue);
                cache.removeView(vision);
                autreParams.addView(vision);
            }
            if (prefsAlgo.getInt("taille",0)==i){
                cache.removeView(txtTaille);
                autreParams.addView(txtTaille);
                cache.removeView(taille);
                autreParams.addView(taille);
            }

            if (prefsAlgo.getInt("alternance_fg",0)==i){
                cache.removeView(txtGenre);
                autreParams.addView(txtGenre);
                cache.removeView(genre);
                autreParams.addView(genre);
            }
            if (prefsAlgo.getInt("alternance_ac",0)==i){
                cache.removeView(txtSerieux);
                autreParams.addView(txtSerieux);
                cache.removeView(attitude);
                autreParams.addView(attitude);
            }
            if (prefsAlgo.getInt("alternance_fd",0)==i){
                cache.removeView(txtNiveau);
                autreParams.addView(txtNiveau);
                cache.removeView(difficultes);
                autreParams.addView(difficultes);
            }
        }
        return vue;
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
            default:
                return false;
        }
    }

    public void onExplose(){this.finishAffinity();}
}