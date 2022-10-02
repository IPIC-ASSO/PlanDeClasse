package com.ipiccie.plandeclasse;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {
    private LayoutInflater inflater;
    private SharedPreferences prefs;
    private String[] classes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inflater = this.getLayoutInflater();
        prefs = getBaseContext().getSharedPreferences("classes", Context.MODE_PRIVATE);//liste des classes et commentaires pour chaque classe
        obtienClasse0();
        inflation0(classes);

        com.google.android.material.floatingactionbutton.FloatingActionButton bouton1 = findViewById(R.id.nouv_classe);
        com.google.android.material.floatingactionbutton.FloatingActionButton bouton2 = findViewById(R.id.vers_gallerie);
        bouton1.setOnClickListener(v -> {
            Intent intention = new Intent(this, NouvelleClasse.class);
            intention.putExtra("classe","-1");
            startActivity(intention);
        });
        bouton2.setOnClickListener(v -> {
            startActivity(new Intent(this, Gallerie.class));
        });
    }

    public void obtienClasse0(){
        String savedString = prefs.getString("liste_classes", "");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        classes = new String[st.countTokens()];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = st.nextToken();
        }
        Log.d(TAG, "obtienClasse"+classes.length);
    }

    public void inflation0(String[] classes){
        LinearLayout liste = findViewById(R.id.liste_classes);
        if (classes.length !=0){
            findViewById(R.id.bienvenue).setVisibility(View.GONE);
            for (String classe: classes){
                View vue = inflater.inflate(R.layout.profil_classe, null);
                TextView nom = vue.findViewById(R.id.nom_classe);
                TextView sup = vue.findViewById(R.id.commentaires_classe);
                TextView nb = vue.findViewById(R.id.nombre_eleves);
                ImageView apercu = vue.findViewById(R.id.apercu);
                List<Integer> conf1 = obtienConfig(classe);
                Log.d(TAG, "inflation0: "+conf1.size());
                dessine(conf1,apercu);

                nom.setText("classe: "+classe);
                sup.setText(prefs.getString(classe,"inconnu au bataillon"));
                SharedPreferences listeEleves= getBaseContext().getSharedPreferences("liste_eleves", Context.MODE_PRIVATE);
                StringTokenizer st = new StringTokenizer(listeEleves.getString(classe,""), ",");
                nb.setText("élèves: "+st.countTokens());
                vue.setOnClickListener(v -> {
                    AlertDialog.Builder constr = new AlertDialog.Builder(this);
                    constr.setTitle("Aller vers...");
                    Button versClasse = new Button(this);
                    Button versEleves = new Button(this);
                    Button versPlan = new Button(this);
                    versClasse.setText("Modifier la configuration");
                    versEleves.setText("Gérer les élèves");
                    versPlan.setText("Créer un plan de classe");
                    if(st.countTokens()<3){
                        versPlan.setVisibility(View.GONE);
                    }
                    versClasse.setOnClickListener(w->{
                        Intent intention = new Intent(this, NouvelleClasse.class);
                        intention.putExtra("classe",classe);
                        startActivity(intention);
                    });
                    versEleves.setOnClickListener(w->{
                        Intent intention = new Intent(this, ListeEleves.class);
                        intention.putExtra("classe",classe);
                        startActivity(intention);
                    });
                    versPlan.setOnClickListener(w ->{
                        Intent intention = new Intent(this, ParametresAlgorithme.class);
                        intention.putExtra("classe",classe);
                        startActivity(intention);
                    });
                    LinearLayout lay = new LinearLayout(this);
                    lay.setOrientation(LinearLayout.VERTICAL);
                    lay.setPadding(10,10,10,10);
                    lay.addView(versClasse);
                    lay.addView(versEleves);
                    lay.addView(versPlan);
                    constr.setView(lay);
                    constr.show();
                });
                liste.addView(vue);
            }
        }

    }
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: "+prefs.getAll());
        LinearLayout liste = findViewById(R.id.liste_classes);
        liste.removeAllViews();
        obtienClasse0();
        inflation0(classes);
        super.onResume();
    }

    public List<Integer> obtienConfig(String classe){
        SharedPreferences config = getBaseContext().getSharedPreferences("configuration", Context.MODE_PRIVATE);//config de la classe
        StringTokenizer st = new StringTokenizer(config.getString(classe,""), ",");
        int w = st.countTokens();
        List<Integer> conf = new ArrayList<>();
        for (int i =0; i<w; i++){
            conf.add(Integer.parseInt(st.nextToken()));
        }
        return conf;
    }

    public void dessine(List<Integer> conf, ImageView vue){
        if( conf != null && conf.size()>1){
            Paint color = new Paint();
            color.setARGB(255,0,0,0);
            Bitmap bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(getResources().getColor(R.color.white));
            int colonnes = conf.get(conf.size()-1);
            if (conf.size()-1>0){
                int dimension = Math.min(108/colonnes,108/((conf.size()-1)/colonnes));
                for (int x = 0; x<colonnes; x++){
                    for (int y = 0; y<(conf.size()-1)/colonnes; y++){
                        if (conf.get(x+y*colonnes) == 1){
                            canvas.drawRect(x*dimension+1F,y*dimension+1F,(x+1F)*dimension,(y+1F)*dimension,color);
                        }
                    }
                }
            }
            vue.setImageBitmap(bitmap);
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
        constr.setMessage(String.format("Vous utilisez la %s de l'application.\n%s \nL'application a été développée par IPIC&cie.",getString(R.string.version),getString(R.string.notes_version)));
        constr.show();
    }

    public void soutient(){
        AlertDialog.Builder construit = new AlertDialog.Builder(this);
        construit.setTitle("Merci de votre soutient");
        construit.setMessage("Quelle générosité :.)");
        construit.show();
    }

}