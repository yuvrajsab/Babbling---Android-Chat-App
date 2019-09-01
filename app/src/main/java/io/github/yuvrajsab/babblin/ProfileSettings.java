package io.github.yuvrajsab.babblin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import id.zelory.compressor.Compressor;

public class ProfileSettings extends AppCompatActivity {

    ImageView profileImage;
    EditText profileName, profileStatus;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    StorageReference thumbnailImagePath;
    FirebaseUser firebaseUser;
    String uid;

    Button profileSaveBtn;
    FloatingActionButton profileImageBtn;

    Uri resultUri = null;

    String image;

    //save button validation
    private boolean task_successfull = false;
    ProgressBar profileProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        profileImage = findViewById(R.id.profileImage);
        profileStatus = findViewById(R.id.profileStatus);
        profileName = findViewById(R.id.profileName);
        profileSaveBtn = findViewById(R.id.profileSaveBtn);
        profileImageBtn = findViewById(R.id.profileImageBtn);
        profileProgressBar = findViewById(R.id.profileProgressBar);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(uid + ".jpg");
        thumbnailImagePath = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbnail_images").child(uid + ".jpg");

        String name = getIntent().getExtras().getString("profile_name");
        profileName.setText(name);
        String status = getIntent().getExtras().getString("profile_status");
        profileStatus.setText(status);

//        image = getIntent().getExtras().getString("profile_image");
        databaseReference.child("image").keepSynced(true);
        databaseReference.child("image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                image = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                if (!image.equals("default")) {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(profileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profileImage);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profileImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(ProfileSettings.this);
            }
        });


        profileSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileProgressBar.setVisibility(View.VISIBLE);

                String newDisplayName = profileName.getText().toString();
                databaseReference.child("name").setValue(newDisplayName).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        task_successfull = true;
                    }
                });

                String newStatus = profileStatus.getText().toString();
                databaseReference.child("status").setValue(newStatus).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        task_successfull = true;
                    }
                });

                if (resultUri != null) {
                    File actualImageFile = new File(resultUri.getPath());
                    Uri compressUri = null;

                    try {
                        File compressedImageFile = new Compressor(ProfileSettings.this)
                                .setQuality(85)
                                .compressToFile(actualImageFile);
                        compressUri = Uri.fromFile(compressedImageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    byte[] data = new byte[0];
                    try {
                        Bitmap thumbnailImage = new Compressor(ProfileSettings.this)
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

                    if (compressUri != null) {
                        storageReference.putFile(compressUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            databaseReference.child("image").setValue(uri.toString());
                                            Toast.makeText(ProfileSettings.this, "Done", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ProfileSettings.this, "Failed to uploaded profile image, Try again!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(ProfileSettings.this, "Error in uploading image", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

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
                }

                if (task_successfull) {
                    Toast.makeText(ProfileSettings.this, "Done", Toast.LENGTH_SHORT).show();
                    profileProgressBar.setVisibility(View.GONE);
                } else {
                    profileProgressBar.setVisibility(View.GONE);
                }
            }
        });

        registerForContextMenu(profileImage);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.profile_image_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.removeImg) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete Image");
            builder.setMessage("Do you really want to delete image");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            StorageReference thumbref = FirebaseStorage.getInstance().getReference().child("profile_images").child("thumbnail_images").child(uid + ".jpg");
                            thumbref.delete();
                            profileImage.setImageResource(R.drawable.default_avatar);
                            databaseReference.child("image").setValue("default");
                            databaseReference.child("thumbnail").setValue("default");
                            Toast.makeText(ProfileSettings.this, "Successfully deleted profile image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (image.equals("default")) {
                                Toast.makeText(ProfileSettings.this, "This is default image", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProfileSettings.this, "Error occurred in deleting profile image", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
            builder.setNegativeButton("No", null);
            builder.setCancelable(false);
            builder.show();
        }
        if (item.getItemId() == R.id.viewImage) {
            Intent imagePreview = new Intent(ProfileSettings.this, ImagePreviewActivity.class);
            imagePreview.putExtra("profile_image", image);
            startActivity(imagePreview);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                profileImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(ProfileSettings.this, "Error in cropping image", Toast.LENGTH_LONG).show();
            }
        }
    }
}
