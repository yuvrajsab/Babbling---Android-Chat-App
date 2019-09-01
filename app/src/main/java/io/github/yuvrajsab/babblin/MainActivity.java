package io.github.yuvrajsab.babblin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private ConstraintLayout main_layout;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_layout = findViewById(R.id.main_layout);
        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        viewPager = findViewById(R.id.mainPager);
        viewPager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));

        tabLayout = findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void sendtostart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                Snackbar.make(main_layout, "No Internet Connection", Snackbar.LENGTH_LONG)
                        .setAction("Settings", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(intent);
                            }
                        }).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.main_menu_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Logout");
            builder.setMessage("Do you really want to logout ?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    databaseReference.child(currentUser.getUid()).child("online").setValue(false);
                    FirebaseAuth.getInstance().signOut();
                    sendtostart();
                }
            });
            builder.setNegativeButton("No", null);
            builder.setCancelable(false);
            builder.show();
        }

        if (id == R.id.main_menu_settings) {
            Intent mainsettings = new Intent(MainActivity.this, MainSettings.class);
            startActivity(mainsettings);
        }

        if (id == R.id.allUsers) {
            Intent allusers = new Intent(MainActivity.this, AllUsers.class);
            startActivity(allusers);
        }

        return super.onOptionsItemSelected(item);
    }
}
