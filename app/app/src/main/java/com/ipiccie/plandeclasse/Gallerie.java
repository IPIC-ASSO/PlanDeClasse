package com.ipiccie.plandeclasse;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class Gallerie extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 200;
    private ArrayList<String> imagePaths;
    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallerie);


        // creating a new array list and
        // initializing our recycler view.
        imagePaths = new ArrayList<>();
        imagesRV = findViewById(R.id.idRVImages);

        // we are calling a method to request
        // the permissions to read external storage.
        requestPermissions();
        // calling a method to
        // prepare our recycler view.
        prepareRecyclerView();

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();
        // showing the back button in action bar
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private boolean checkPermission() {
        // in this method we are checking if the permissions are granted or not and returning the result.
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (checkPermission()) {
            // if the permissions are already granted we are calling
            // a method to get all images from our external storage.
            getImagePath2();
        } else {
            // if the permissions are not granted we are
            // calling a method to request permissions.
            requestPermission();
        }
    }

    private void requestPermission() {
        //on below line we are requesting the read external storage permissions.
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    private void prepareRecyclerView() {

        if(imagePaths.size() == 0){
            findViewById(R.id.bienvenue_galerie).setVisibility(View.VISIBLE);
        }else{
            // in this method we are preparing our recycler view.
            // on below line we are initializing our adapter class.
            imageRVAdapter = new RecyclerViewAdapter(Gallerie.this, imagePaths);

            // on below line we are creating a new grid layout manager.
            GridLayoutManager manager = new GridLayoutManager(Gallerie.this, 4);

            // on below line we are setting layout
            // manager and adapter to our recycler view.
            imagesRV.setLayoutManager(manager);
            imagesRV.setAdapter(imageRVAdapter);
        }
    }

    private void getImagePath2(){
        File directory = new File(Environment.getExternalStorageDirectory() + "/Download/Plans_de_classe/");
        File[] files = directory.listFiles();
        if (files != null) {
            Log.d("Files", "Size: "+ files.length);
        }
        for (int i = 0; i < Objects.requireNonNull(files).length; i++)
        {
            imagePaths.add(files[i].getPath());
            Log.d("Files", "FileName:" + files[i].getName());
        }
    }

    private void getImagePath() {
        // in this method we are adding all our image paths
        // in our arraylist which we have created.
        // on below line we are checking if the device is having an sd card or not.
        boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);

        if (isSDPresent) {

            // if the sd card is present we are creating a new list in
            // which we are getting our images data with their ids.
            final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

            // on below line we are creating a new
            // string to order our images by string.
            final String orderBy = MediaStore.Images.Media._ID;

            // this method will stores all the images
            // from the gallery in Cursor
            Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);

            // below line is to get total number of images
            int count = cursor.getCount();

            // on below line we are running a loop to add
            // the image file path in our array list.
            for (int i = 0; i < count; i++) {

                // on below line we are moving our cursor position
                cursor.moveToPosition(i);

                // on below line we are getting image file path
                int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                // after that we are getting the image file path
                // and adding that path in our array list.
                Log.d(TAG, "getImagePath: "+(imagePaths == null));
                imagePaths.add(cursor.getString(dataColumnIndex));
            }
            //imageRVAdapter.notifyDataSetChanged();
            // after adding the data to our
            // array list we are closing our cursor.
            cursor.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // this method is called after permissions has been granted.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // we are checking the permission code.
            case PERMISSION_REQUEST_CODE:
                // in this case we are checking if the permissions are accepted or not.
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // if the permissions are accepted we are displaying a toast message
                        // and calling a method to get image path.
                        getImagePath2();
                    } else {
                        // if permissions are denied we are closing the app and displaying the toast message.
                        Toast.makeText(this, "Permission refusée. Impossible de réaliser cette action.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}
