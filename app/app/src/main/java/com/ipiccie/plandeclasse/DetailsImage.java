package com.ipiccie.plandeclasse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class DetailsImage extends AppCompatActivity {

    // creating a string variable, image view variable
    // and a variable for our scale gesture detector class.
    String imgPath;
    private ImageView imageView;
    private ScaleGestureDetector scaleGestureDetector;

    // on below line we are defining our scale factor.
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_image);

        // on below line getting data which we have passed from our adapter class.
        imgPath = getIntent().getStringExtra("imgPath");

        // initializing our image view.
        imageView = findViewById(R.id.idIVImage);

        // on below line we are initializing our scale gesture detector for zoom in and out for our image.
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        // on below line we are getting our image file from its path.
        File imgFile = new File(imgPath);

        // if the file exists then we are loading that image in our image view.
        if (imgFile.exists()) {
            Picasso.get().load(imgFile).placeholder(R.drawable.ic_launcher_background).into(imageView);
        }
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

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // inside on touch event method we are calling on
        // touch event method and passing our motion event to it.
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        // on below line we are creating a class for our scale
        // listener and  extending it with gesture listener.
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            // inside on scale method we are setting scale
            // for our image in our image view.
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // on below line we are setting
            // scale x and scale y to our image view.
            imageView.setScaleX(mScaleFactor);
            imageView.setScaleY(mScaleFactor);
            return true;
        }
    }
}