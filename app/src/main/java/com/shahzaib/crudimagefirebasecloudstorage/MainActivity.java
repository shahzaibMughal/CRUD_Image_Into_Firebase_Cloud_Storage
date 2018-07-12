package com.shahzaib.crudimagefirebasecloudstorage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private int IMAGE_PICKER_REQUEST_CODE = 5;
    ImageView imageView;
    boolean isImageSelected = false;
    boolean isImageUploaded = false;
    Uri downloadImageUri = null;

    Uri imageUri;
    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);

        firebaseStorage = FirebaseStorage.getInstance();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);
        if (requestCode == IMAGE_PICKER_REQUEST_CODE) {
            if (imageData != null) {
                imageUri = imageData.getData();

                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    isImageSelected = true;
                    imageView.setImageBitmap(image);
                    imageView.setVisibility(View.VISIBLE);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(this, "Image Not Received", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void picKImage(View view) {
        // first check for permission
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            boolean isHavePermission = checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!isHavePermission) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
                return;
            }
        }


        Intent chooseImageIntent = new Intent(Intent.ACTION_PICK);
        chooseImageIntent.setType("image/*");
        /*sendIntent.resolveActivity(getPackageManager()) != null*/
        Intent chooser = Intent.createChooser(chooseImageIntent, "Complete action using");
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, IMAGE_PICKER_REQUEST_CODE);
        } else {
            Toast.makeText(this, "There is no app to perform the action", Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadImage(View view) {
        if (isImageSelected) {
            // create a directory under root (named "Images") & save all your images in Images directory
            StorageReference storageReference = firebaseStorage.getReference().child("Images");
            final StorageReference referenceToImage = storageReference.child("myImage");

            Log.i("123456","Image Uri: "+imageUri);

            referenceToImage.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(MainActivity.this, "Uploading Failed", Toast.LENGTH_SHORT).show();
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                    Toast.makeText(MainActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    imageView.setVisibility(View.GONE);
                    isImageSelected = false;
                    isImageUploaded = true;

                }
            });

        } else {
            Toast.makeText(this, "First Select a images", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadImage(View view) {
        if(isImageUploaded)
        {
            StorageReference storageReference = firebaseStorage.getReference().child("Images");
            final StorageReference referenceToImage = storageReference.child("myImage");

            try {

                final File fileAddressInLocalStorage = File.createTempFile("image", "jpg");

                referenceToImage.getFile(fileAddressInLocalStorage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(MainActivity.this, "Image Downloaded successfully", Toast.LENGTH_SHORT).show();

                        Bitmap image = getImageBitmapFromFile(fileAddressInLocalStorage);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(image);
                        isImageSelected = false;
                        isImageUploaded = false;

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                    }
                });


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(this, "First Upload Image", Toast.LENGTH_SHORT).show();
        }


    }

    private Bitmap getImageBitmapFromFile(File fileAddressInLocalStorage) {
        try {

            FileInputStream fin = new FileInputStream(fileAddressInLocalStorage);
            return  BitmapFactory.decodeStream(fin);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public void deleteImage(View view) {

        StorageReference storageReference = firebaseStorage.getReference().child("Images");
        StorageReference referenceToImage = storageReference.child("myImage");

        referenceToImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Image Deleted successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Image already deleted, Or not exist", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
