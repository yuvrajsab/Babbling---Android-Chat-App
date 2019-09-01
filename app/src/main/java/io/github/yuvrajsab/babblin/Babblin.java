package io.github.yuvrajsab.babblin;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class Babblin extends Application {

    DatabaseReference userRef;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso build = builder.build();
        build.setLoggingEnabled(true);
        Picasso.setSingletonInstance(build);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String uid = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (firebaseUser != null) {
//                        userRef.child("online").onDisconnect().setValue(false);
//                        userRef.child("online").setValue(true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
