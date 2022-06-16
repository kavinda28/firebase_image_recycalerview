package com.example.firebase_image_recycalerview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Button ButtonChooseImage, ButtonUpload, ShowUpload;
    private EditText filename;
    private ImageView imageView;
    private ProgressBar progressBar, Image_loading;

    private Uri ImageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    String myUri = "";
    private StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButtonChooseImage = findViewById(R.id.choice_image);
        ButtonUpload = findViewById(R.id.upload);
        ShowUpload = findViewById(R.id.show_upload);
        filename = findViewById(R.id.file_name);
        imageView = findViewById(R.id.image_View);
        progressBar = findViewById(R.id.progressBar);
        Image_loading = findViewById(R.id.imageloading);

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("Uploads");

        ButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Image_loading.setVisibility(View.VISIBLE);
                OpenFileChooser();
            }
        });

        ButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Upload in progress", Toast.LENGTH_SHORT).show();
                } else {
                    UploadFile();
                }
            }
        });

        ShowUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenImageActivity();
            }
        });


    }




    //open image and show image_view
    private void OpenFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            ImageUri = data.getData();

            Picasso.get().load(ImageUri).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    if (Image_loading != null) {
                        Image_loading.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(Exception e) {

                }
            });
        }
    }


    //store file in firebase

    private String getFile_Extension(Uri uri) {
        ContentResolver CR = getContentResolver();
        MimeTypeMap mimeTypeInfo = MimeTypeMap.getSingleton();
        return mimeTypeInfo.getExtensionFromMimeType(CR.getType(uri));
    }


    private void UploadFile() {
        if (ImageUri != null) {
            StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFile_Extension(ImageUri));

            uploadTask = fileReference.putFile(ImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 500);

                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    update_model update = new update_model(filename.getText().toString().trim(), url);
                                    String uploadId = databaseReference.push().getKey();
                                    databaseReference.child(uploadId).setValue(update);

                                }
                            });
                            Toast.makeText(MainActivity.this, "upload Successful", Toast.LENGTH_SHORT).show();




                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

//show images in Recycler_View
    private void OpenImageActivity() {
        startActivity(new Intent(this,show_images.class));
    }
}
