package com.example.jacie.functions;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.jacie.utils.utils.setCustomActionBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jacie.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

public class GoogleLensActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_PERMISSION=200;
    private static final int STORAGE_REQUEST_PERMISSION=400;
    private static final int IMAGE_PICK_GALLERY_REQUEST_PERMISSION=1000;
    private static final int IMAGE_PICK_CAMERA_REQUEST_PERMISSION=1001;

    String[] cameraPermissions;
    String[] storagePermissions;
    EditText mResultEt;
    ImageView mPreviewIV;
    Uri image_uri;
    Button search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lens);
        setCustomActionBar(Objects.requireNonNull(getSupportActionBar()),this);

        mResultEt=findViewById(R.id.resultEd);
        mPreviewIV=findViewById(R.id.imageViewPre);
        search=findViewById(R.id.searchBtn);
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textToSearch=mResultEt.getText().toString();
                if (!textToSearch.isEmpty()){
                    Uri uri=Uri.parse("https://www.google.com/search?q="+textToSearch);
                    Intent gSearchIntent=new Intent(Intent.ACTION_VIEW,uri);
                    startActivity(gSearchIntent);
                }
                else {
                    Toast.makeText(GoogleLensActivity.this,"Add Text",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.addImageBtn){
            showImageInputDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageInputDialog() {
        String[] items={"Camera","Gallery"};
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Select Image");
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which==0){
                    if (!checkCameraPermissions()){
                        requestCameraPermissions();
                    }
                    else{
                        pickCamera();
                    }
                }

                if (which==1){
                    if (!checkStoragePermissions()){
                        requestStoragePermissions();
                    }
                    else{
                        pickGallery();
                    }
                }

            }
        });
        dialog.create().show();
    }


    private void pickGallery() {
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_REQUEST_PERMISSION);
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_PERMISSION);
    }

    private boolean checkStoragePermissions() {
        return ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PERMISSION_GRANTED);
    }



    private void pickCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"New Image");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Text");
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraImage=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImage.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraImage,IMAGE_PICK_CAMERA_REQUEST_PERMISSION);
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this,cameraPermissions,CAMERA_REQUEST_PERMISSION);
    }

    private boolean checkCameraPermissions() {
        boolean resultCamera= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)==(PERMISSION_GRANTED);
        boolean resultStorage= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PERMISSION_GRANTED);
        return (resultCamera && resultStorage) ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_PERMISSION:
                if (grantResults.length>0)
                {
                    boolean cameraAccepted=grantResults[0]== PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[0]== PERMISSION_GRANTED;
                    if (cameraAccepted && writeStorageAccepted){
                        pickCamera();
                    }
                    else {
                        Toast.makeText(this,"PERMISSION DENIED",Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case STORAGE_REQUEST_PERMISSION:
                if (grantResults.length>0)
                {
                    boolean writeStorageAccepted=grantResults[0]== PERMISSION_GRANTED;
                    if (writeStorageAccepted){
                        pickGallery();
                    }
                    else {
                        Toast.makeText(this,"PERMISSION DENIED",Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_REQUEST_PERMISSION ) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);


            }
            if (requestCode == IMAGE_PICK_CAMERA_REQUEST_PERMISSION) {
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);


            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mPreviewIV.setImageURI(resultUri);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) mPreviewIV.getDrawable();
                Bitmap bitmap = bitmapDrawable.getBitmap();

                TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if (!recognizer.isOperational()) {
                    Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();

                } else {
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = recognizer.detect(frame);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < items.size(); i++) {
                        TextBlock myitems = items.valueAt(i);
                        sb.append(myitems.getValue());
                        sb.append("\n");

                    }
                    mResultEt.setText(sb.toString());
                }


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}