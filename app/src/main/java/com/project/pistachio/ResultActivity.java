package com.project.pistachio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    ImageView picture;
    private Button btn_hesapla;
    private ResultView mResultView;
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY;
    private Bitmap mBitmap = null;
    private Module mModule = null;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        picture = (ImageView) findViewById(R.id.imageView);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if (intent.getParcelableExtra(MainActivity.TAKE_PICTURE_INTENT) != null) {
            mBitmap= (Bitmap) intent.getParcelableExtra(MainActivity.TAKE_PICTURE_INTENT);

            picture.setImageBitmap(mBitmap);
        }

        if (intent.getStringExtra(MainActivity.LOAD_PICTURE_INTENT) != null) {
            String data = intent.getStringExtra(MainActivity.LOAD_PICTURE_INTENT);
            Uri selectedImage = Uri.parse(data);
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            if (selectedImage != null) {
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    mBitmap = BitmapFactory.decodeFile(picturePath);
                    Matrix matrix = new Matrix();
                    //matrix.postRotate(90.0f);
                    mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);

                    picture.setImageBitmap(mBitmap);
                    cursor.close();
                }
            }
        }



        /*try {
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[0]));
            picture.setImageBitmap(mBitmap);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }*/

        btn_hesapla = findViewById(R.id.btn_hesapla);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_hesapla.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btn_hesapla.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);

                mImgScaleX = (float)mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                mImgScaleY = (float)mBitmap.getHeight() / PrePostProcessor.mInputHeight;

                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float)picture.getWidth() / mBitmap.getWidth() : (float)picture.getHeight() / mBitmap.getHeight());
                mIvScaleY  = (mBitmap.getHeight() > mBitmap.getWidth() ? (float)picture.getHeight() / mBitmap.getHeight() : (float)picture.getWidth() / mBitmap.getWidth());

                mStartX = (picture.getWidth() - mIvScaleX * mBitmap.getWidth())/2;
                mStartY = (picture.getHeight() -  mIvScaleY * mBitmap.getHeight())/2;

                Thread thread = new Thread(ResultActivity.this::run);
                thread.start();
            }

        });
        try {
            mModule = PyTorchAndroid.loadModuleFromAsset(getAssets(), "best.torchscript");
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        mBitmap = (Bitmap) data.getExtras().get("data");
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90.0f);
                        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                        picture.setImageBitmap(mBitmap);
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90.0f);
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                picture.setImageBitmap(mBitmap);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }

    public void run(){
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue inputs = IValue.from(inputTensor);
        IValue[] outputTuple = mModule.forward(inputs).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results =  PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);

        runOnUiThread(() -> {
            btn_hesapla.setEnabled(true);
            btn_hesapla.setText("Hesapla");
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        });
    }
}