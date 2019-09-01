package io.github.yuvrajsab.babblin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AccountSettings extends AppCompatActivity {

    private ListView accountSettingsList;
    private DatabaseReference databaseReference;
    private DatabaseReference messagesRef;
    private StorageReference storageReference;
    private FirebaseUser user;
    private String currentUserId;
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_RE_SIGN_IN = 2;
    private GoogleSignInAccount acct = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        String[] items = {"Delete Account"};

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        messagesRef = FirebaseDatabase.getInstance().getReference().child("messages");
        storageReference = FirebaseStorage.getInstance().getReference();

        accountSettingsList = findViewById(R.id.accountSettingsList);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        accountSettingsList.setAdapter(arrayAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(AccountSettings.this, "Sorry, can't authenticate you", Toast.LENGTH_SHORT).show();
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();


        accountSettingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        signIn();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AccountSettings.this);
                        builder.setTitle("Delete Account");
                        builder.setMessage("Your account will be permanently deleted.\nAre your sure you want to delete your account ?");
                        builder.setNegativeButton("No", null);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (acct != null) {
                                    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                                    user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                messagesRef.child(currentUserId).removeValue();
                                                databaseReference.child(currentUserId).removeValue();
                                                storageReference.child(currentUserId).delete();
                                                user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(AccountSettings.this, "Account deleted successfully", Toast.LENGTH_LONG).show();
                                                            Intent startIntent = new Intent(AccountSettings.this, StartActivity.class);
                                                            startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                            startActivity(startIntent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(AccountSettings.this, "Failed", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
                        break;
                }
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_RE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_RE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            acct = result.getSignInAccount();
        }
    }
}
