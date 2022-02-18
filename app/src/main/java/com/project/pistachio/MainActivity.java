package com.project.pistachio;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    Button load_picture, take_picture;
    public static final String LOAD_PICTURE_INTENT = "com.project.pistachio.LOAD_PICTURE_INTENT";
    public static final String TAKE_PICTURE_INTENT = "com.project.pistachio.TAKE_PICTURE_INTENT";
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    File output;
    String TYPE_OF_ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                    }
                });

        init_UI();
    }

    private void init_UI() {
        load_picture = (Button) findViewById(R.id.load_pic);
        take_picture = (Button) findViewById(R.id.take_pic);

        load_picture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                    TYPE_OF_ACTION = "LOAD_PICTURE";
                    someActivityResultLauncher.launch(i);
                } else {
                    requestStoragePermission();
                }

            }
        });

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                TYPE_OF_ACTION = "TAKE_PICTURE";
                someActivityResultLauncher.launch(cameraIntent);
            }
        });

    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Erişim izni")
                    .setMessage("Kamera ve galeri erişimine izin ver")
                    .setPositiveButton("İzin Ver", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            Intent intent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                            someActivityResultLauncher.launch(intent);

                        }
                    })
                    .setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
         }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "İzin alındı", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "İzin alınmadı", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap mBitmap;
        Intent data1 = new Intent(MainActivity.this, ResultActivity.class);

        if (TYPE_OF_ACTION == "TAKE_PICTURE"){
            mBitmap = (Bitmap) data.getExtras().get("data");
            Matrix matrix = new Matrix();
            //matrix.postRotate(90.0f);
            mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
            data1.putExtra(TAKE_PICTURE_INTENT, mBitmap);
        } else if (TYPE_OF_ACTION == "LOAD_PICTURE") {
            String selectedImage= data.getData().toString();
            data1.putExtra(LOAD_PICTURE_INTENT, selectedImage);
        }

        startActivity(data1);

    }
}