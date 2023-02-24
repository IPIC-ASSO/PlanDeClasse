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
                new MesOutils(this).reinitialiser();
                onExplose();
                return true;
            case R.id.nous_soutenir:
                new MesOutils(this).soutient();
                return true;
            case R.id.infos:
                new MesOutils(this).infos();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onExplose(){this.finishAffinity();}

}
