package io.github.yuvrajsab.babblin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MainSettings extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    String uid;

    ListView mainSettingsList;
    String[] settingsList = {"Account", "Chats", "Help"};

    ConstraintLayout profileItem;

    ImageView settingsThumbImg;
    TextView settingsDisplayName;
    TextView settingsStatus;

    String name, status, image, thumbImg;

    ConstraintLayout settings_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);

        settingsDisplayName = findViewById(R.id.settings_display_name);
        settingsThumbImg = findViewById(R.id.settings_thumbnail);
        settingsStatus = findViewById(R.id.settings_status);
        settings_layout = findViewById(R.id.settings_layout);

        mainSettingsList = findViewById(R.id.mainSettingsList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, settingsList);
        mainSettingsList.setAdapter(arrayAdapter);

        profileItem = findViewById(R.id.profileItem);
        profileItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileSettings = new Intent(MainSettings.this, ProfileSettings.class);
                profileSettings.putExtra("profile_name", name);
                profileSettings.putExtra("profile_status", status);
                profileSettings.putExtra("profile_image", image);
                startActivity(profileSettings);
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();

            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            databaseReference.keepSynced(true);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                    status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                    thumbImg = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();
                    image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                    settingsDisplayName.setText(name);
                    settingsStatus.setText(status);

                    if (!thumbImg.equals("default")) {
                        Picasso.get().load(thumbImg).placeholder(R.drawable.default_avatar)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(settingsThumbImg, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(thumbImg).placeholder(R.drawable.default_avatar).into(settingsThumbImg);
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(settings_layout, "Error In Fetching User Details", Snackbar.LENGTH_LONG).show();
                }
            });
        }
        mainSettingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent accountSettings = new Intent(MainSettings.this, AccountSettings.class);
                        startActivity(accountSettings);
                        break;
                    case 1:
                        Intent chatSettings = new Intent(MainSettings.this, ChatSettings.class);
                        startActivity(chatSettings);
                        break;
                    case 2:
                        Intent helpIntent = new Intent(MainSettings.this, Help.class);
                        startActivity(helpIntent);
                        break;
                }
            }
        });
    }
}
