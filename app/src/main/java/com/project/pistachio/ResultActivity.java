package com.project.pistachio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;

public class ResultActivity extends AppCompatActivity {

    ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        Uri uri =  Uri.parse(intent.getStringExtra(MainActivity.SELECT_IMAGE));
        String filestring = uri.getPath();

        Bitmap thumbnail = BitmapFactory.decodeFile(filestring);
        Bitmap loadedImage = intent.getParcelableExtra(MainActivity.SELECT_IMAGE);

        picture = (ImageView) findViewById(R.id.imageView);
        picture.setImageBitmap(thumbnail);

    }
}