package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

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
import android.widget.RadioButton;
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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
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
                sup.setText(donnees.get(indices.getInt(eleve+classe,0))[14]);
                vue.setOnClickListener(v -> generateurEleve(indices.getInt(eleve+classe,0)));
                ImageView image = vue.findViewById(R.id.couleur);
                if (Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[10])&& Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==1){
                    image.setColorFilter(Color.argb(255, 255, 255, 0));
                } else if (Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[10]) ||Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[11])||Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==0){
                    image.setColorFilter(Color.argb(255, 255, 0, 0));
                }else if(Boolean.parseBoolean(donnees.get(indices.getInt(eleve+classe,0))[12])|| Integer.parseInt(donnees.get(indices.getInt(eleve+classe,0))[7])==1){
                    image.setColorFilter(Color.argb(255, 0, 255, 0));
                }else{
                    image.setColorFilter(Color.argb(255, 255, 255, 0));
                }
                liste.addView(vue);
            }
        }
        if (nbEleves == eleves.length) {
            findViewById(R.id.nouv_eleve).setVisibility(View.GONE);
        }
        if(nbEleves >2){
            findViewById(R.id.vers_realiser_plan).setVisibility(View.VISIBLE);
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
        LayoutInflater inflater = this.getLayoutInflater();
        View vue = inflater.inflate(R.layout.generateur_eleve, null);
        constructeur.setView(vue);
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
        StringTokenizer st3 = new StringTokenizer(prefsAlgo.getString("places",""),",");
        final int[] placeur = new int[eleves.length];//config
        Arrays.fill(placeur,0);
        int z = st3.countTokens();
        for(int i= 0; i<z;i++){
            placeur[i] = Integer.parseInt(st3.nextToken());
        }
        int compte = 0; //nombre d'élèves enregistrés dans la classe
        for (String eleve: eleves){
            if (eleve != null)compte+=1;
        }
        boolean[] selection = new boolean[compte];
        boolean[] selection2 = new boolean[compte];
        Arrays.fill(selection2,false);
        Arrays.fill(selection,false);
        if (indice>=0){ //chargement des données enregistrées sur l'élève
            constructeur.setTitle("Modifier les informations");
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
            switch (d[9]){    //genre
                case "0":
                    genre.check(R.id.fille);
                    break;
                case "1":
                    genre.check(R.id.garcon);
                    break;
            }
            StringTokenizer stEvite = new StringTokenizer(d[2], ",");
            StringTokenizer stCorrecte = new StringTokenizer(d[3], ",");
            int x= stEvite.countTokens();
            for (int i= 0; i< x; i++){
                selection[i]=Boolean.parseBoolean(stEvite.nextToken());
                selection2[i]=Boolean.parseBoolean(stCorrecte.nextToken());
            }
            Log.d(TAG, "generateurEleve: "+Arrays.toString(selection));
            Log.d(TAG, "generateurEleve: "+Arrays.toString(selection2));
            }catch (Exception e){
                Log.e(TAG, "generateurEleve: ", e);
                Toast.makeText(this,"Incompatibilités de versions détectées.Veuillez réinitialiser l'application",Toast.LENGTH_LONG).show();
            }
        }else{
            constructeur.setTitle("Nouvel élève");
        }
        AlertDialog show = constructeur.show();
        evite.setOnClickListener(x ->{

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
            Log.d(TAG, "generateurEleve: "+!(nbEleves>=eleves.length && indice<0)+ !nom.getText().toString().equals("") + !(genre.getCheckedRadioButtonId()!= R.id.fille && genre.getCheckedRadioButtonId()!=R.id.garcon) + !(Arrays.asList(eleves).contains(nom.getText().toString()) && indice<0));
            if (!(nbEleves>=eleves.length && indice<0) &&  !nom.getText().toString().equals("") && !(genre.getCheckedRadioButtonId()!= R.id.fille && genre.getCheckedRadioButtonId()!=R.id.garcon) && !((Arrays.asList(eleves).contains(nom.getText().toString())) && (indice<0|| !Objects.equals(donnees.get(indice)[1], nom.getText().toString())))){
                if (indice>=0){ //élève déjà enregistré (modifications)
                    eleves[Arrays.asList(eleves).indexOf(donnees.get(indice)[1])] = nom.getText().toString();
                }else{
                    eleves[nbEleves] = nom.getText().toString();    //ajoute le nom du nouvel élève
                    nbEleves+=1;
                }
                for(int i = 0; i<selection.length;i++){ //met à jour les affinités
                    if (selection[i] && selection2[i]){
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
            }else if(nbEleves>=eleves.length && indice<0) {
                Toast.makeText(this, "Enregistrement impossible. Trop d'élèves pour cette classe. Veuillez d'abord modifier sa configuration.", Toast.LENGTH_LONG).show();
            }else if((Arrays.asList(eleves).contains(nom.getText().toString())) && (indice<0|| !Objects.equals(donnees.get(indice)[1], nom.getText().toString()))){
                Toast.makeText(this,"Enregistrement impossible. Un élève du même nom existe déjà dans cette classe.",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this,"Enregistrement impossible. Veuillez remplir tous les champs marqués d'une astérisque.",Toast.LENGTH_LONG).show();
            }
            show.dismiss();
        });
        placer.setOnClickListener(zer ->{
            AlertDialog.Builder constructeur2= new AlertDialog.Builder(this);
            constructeur2.setTitle("Variante");
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
            constructeur2.setView(def);
            TableRow.LayoutParams params = new TableRow.LayoutParams(50, TableRow.LayoutParams.WRAP_CONTENT);
            params.setMargins(2,10,2,10);
            int compte2 = 0;
            for (int rang = 0; rang<tampon.length/colonnes;rang++){
                TableRow ligne = new TableRow(this);
                ligne.setLayoutParams(rowParams);
                ligne.setMinimumWidth(30);
                table.addView(ligne);
                for (int x =0; x<colonnes;x++){
                    Button nomEleve = new Button(this);
                    nomEleve.setLayoutParams(params);
                    nomEleve.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    nomEleve.setPadding(2,1,2,1);
                    ligne.addView(nomEleve);
                    if (tampon[rang*colonnes +x] == 1){
                        nomEleve.setBackground(getDrawable(R.drawable.bords));
                        compte2++;
                        final int truc = compte2;
                        nomEleve.setOnClickListener(v -> placeur[truc] =1);
                    }
                }
            }
            try {
                constructeur2.show();
            }catch (Exception e){
                Log.e(TAG, "affiche: ",e );
                Toast.makeText(this,"Erreur critique, contactez les développeurs",Toast.LENGTH_LONG).show();
            }
        });
        show.show();
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
        fich.delete();
        onExplose();
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
        construit.setMessage("Que vous êtes bon");
        construit.show();
    }
}