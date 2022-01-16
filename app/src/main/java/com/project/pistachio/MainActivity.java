package com.project.pistachio;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button load_picture, take_picture;
    public static Boolean access_permission = false;
    public static final String SELECT_IMAGE = "com.project.pistachio.SELECT_IMAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init_UI();
    }

    private void init_UI() {
        load_picture = (Button) findViewById(R.id.load_pic);
        take_picture = (Button) findViewById(R.id.take_pic);

        load_picture.setOnClickListener(new View.OnClickListener() {

            ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                                // There are no request codes
                                Intent data = new Intent(MainActivity.this, ResultActivity.class);
                                data.putExtra(SELECT_IMAGE, result.getData().toString());

                                startActivity(data);
                                // doSomeOperations();
                            }
                        }
                    });

            @Override
            public void onClick(View view) {
                if (access_permission == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Erişim izni");
                    builder.setMessage("Kamera ve galeri erişimine izin ver");
                    builder.setPositiveButton("İzin ver", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            access_permission = true;

                            dialog.dismiss();
                            Intent i = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);

                            someActivityResultLauncher.launch(i);
                        }
                    });

                    builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, MainActivity.this, ResultActivity.class);
                    startActivity(i);
                }
            }
        });

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (access_permission == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Erişim izni");
                    builder.setMessage("Kamera ve galeri erişimine izin ver");
                    builder.setPositiveButton("İzin ver", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            access_permission = true;

                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

    }

}