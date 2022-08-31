package com.ipiccie.plandeclasse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class NousContacter extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nous_contacter);

        EditText message = findViewById(R.id.message);
        Button envoyer = findViewById(R.id.envoyer);
        envoyer.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"ipic.assistance@protonmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,String.format("version %s",getString(R.string.version)));
            email.putExtra(Intent.EXTRA_TEXT, message.getText().toString());

            //need this to prompts email client only
            email.setType("message/rfc822");

            startActivity(Intent.createChooser(email, "Choose an Email client :"));
        });

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem info = menu.findItem(R.id.infos);
        MenuItem contact = menu.findItem(R.id.contact);
        MenuItem aide = menu.findItem(R.id.aide);
        info.setVisible(false);
        contact.setVisible(false);
        aide.setVisible(false);
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

    public void soutient(){
        AlertDialog.Builder construit = new AlertDialog.Builder(this);
        construit.setTitle("Merci de votre soutient");
        construit.setMessage("Votre soutient nous rend plus fort");
        construit.show();
    }

}
