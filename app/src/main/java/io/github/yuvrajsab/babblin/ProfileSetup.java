package io.github.yuvrajsab.babblin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ProfileSetup extends AppCompatActivity {

    private EditText profileNameEt;
    private Button saveprofileBtn;
    private String uid = null;
    private String profileName;
    private ImageView profileImg;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private StorageReference thumbnailImagePath;

    private FloatingActionButton setupProfileImgBtn;

    private ConstraintLayout profile_setup_layout;

    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        profile_setup_layout = findViewById(R.id.profile_setup_layout);

        //get profile image on btn click and send to database
        profileImg = findViewById(R.id.profileImg);

        profileNameEt = findViewById(R.id.profileNameEt);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(uid + ".jpg");
        thumbnailImagePath = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbnail_images").child(uid + ".jpg");

        final String deviceToken = FirebaseInstanceId.getInstance().getToken();

        saveprofileBtn = findViewById(R.id.saveProfileBtn);
        saveprofileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                profileName = profileNameEt.getText().toString();

                if (TextUtils.isEmpty(profileName)) {
                    profileNameEt.setError("Profile Name is required");
                    profileNameEt.requestFocus();
                } else {
                    HashMap<String, String> usermap = new HashMap<>();
                    usermap.put("device_token", deviceToken);
                    usermap.put("name", profileName);
                    usermap.put("status", "what's up folks");
                    usermap.put("image", "default");
                    usermap.put("thumbnail", "default");

                    databaseReference.setValue(usermap);
                    databaseReference.child("online").setValue(true);

                    if (resultUri != null) {
                        storageReference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            databaseReference.child("image").setValue(uri.toString());
                                        }
                                    });
                                } else {
                                    Snackbar.make(profile_setup_layout, "Error in uploading profile image", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });

                        File actualImageFile = new File(Objects.requireNonNull(resultUri.getPath()));
                        byte[] data = new byte[0];
                        try {
                            Bitmap thumbnailImage = new Compressor(ProfileSetup.this)
                                    .setMaxHeight(100)
                                    .setMaxWidth(100)
                                    .setQuality(60)
                                    .compressToBitmap(actualImageFile);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumbnailImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            data = baos.toByteArray();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        UploadTask uploadTask = thumbnailImagePath.putBytes(data);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                thumbnailImagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        databaseReference.child("thumbnail").setValue(uri.toString());
                                    }
                                });
                            }
                        });
                    }

                    Intent mainIntent = new Intent(ProfileSetup.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                }
            }
        });

        setupProfileImgBtn = findViewById(R.id.setupProfileImgBtn);
        setupProfileImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileSetup.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profileImg.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
                Snackbar.make(profile_setup_layout, "Error in cropping image", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
