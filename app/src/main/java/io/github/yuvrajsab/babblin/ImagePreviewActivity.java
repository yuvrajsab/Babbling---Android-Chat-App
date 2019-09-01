package io.github.yuvrajsab.babblin;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ImagePreviewActivity extends AppCompatActivity {

    ImageView imagePreviewView;
    ProgressBar image_preview_progress;
    String profileImage;
    String profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.profile_image);
        }

        imagePreviewView = findViewById(R.id.imagePreviewView);
        image_preview_progress = findViewById(R.id.image_preview_progress);

        profileName = getIntent().getStringExtra("profile_name");
        profileImage = getIntent().getStringExtra("profile_image");

        if (actionBar != null) {
            if (profileName != null) {
                actionBar.setTitle(profileName);
            }
        }

        if (profileImage != null) {
            if (!profileImage.equals("default")) {
                Picasso.get().load(profileImage).into(imagePreviewView, new Callback() {
                    @Override
                    public void onSuccess() {
                        image_preview_progress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        image_preview_progress.setVisibility(View.GONE);
                        Toast.makeText(ImagePreviewActivity.this, "Error in loading profile image", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                image_preview_progress.setVisibility(View.GONE);
            }
        }


    }
}
