package io.github.yuvrajsab.babblin;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyActivity extends AppCompatActivity {

    EditText verifyEt;
    Button verifyBtn;
    FirebaseAuth mAuth;

    ProgressBar verifyProgressBar;

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        mAuth = FirebaseAuth.getInstance();

        String phoneNo = Objects.requireNonNull(getIntent().getExtras()).getString("phonenumber");

        verifyEt = findViewById(R.id.verifyEt);
        verifyBtn = findViewById(R.id.verifyBtn);

        verifyProgressBar = findViewById(R.id.verifyProgressBar);
        verifyProgressBar.setVisibility(View.INVISIBLE);

        if (phoneNo != null) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNo, 60, TimeUnit.SECONDS, VerifyActivity.this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                            signInWithPhoneAuthCredential(phoneAuthCredential);
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Toast.makeText(VerifyActivity.this, "Verification Failed:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCodeSent(String verificationId,
                                               PhoneAuthProvider.ForceResendingToken token) {
                            // The SMS verification code has been sent to the provided phone number, we
                            // now need to ask the user to enter the code and then construct a credential
                            // by combining the code with a verification ID.
                            //                        Log.d(TAG, "onCodeSent:" + verificationId);

                            // Save verification ID and resending token so we can use them later
                            mVerificationId = verificationId;
                            mResendToken = token;

                            // ...
                        }
                    }
            );
        }

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

                    if (isConnected) {
                        String code = verifyEt.getText().toString();
                        if (TextUtils.isEmpty(code)) {
                            verifyEt.setError("Enter verification code");
                            verifyEt.requestFocus();
                        } else {
                            verifyBtn.setEnabled(false);
                            verifyProgressBar.setVisibility(View.VISIBLE);
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                            signInWithPhoneAuthCredential(credential);
                        }
                    } else {
                        Toast.makeText(VerifyActivity.this, "Please connect to the internet first", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            String uid = user.getUid();

                            verifyProgressBar.setVisibility(View.GONE);

                            Intent profileIntent = new Intent(VerifyActivity.this, ProfileSetup.class);
                            profileIntent.putExtra("userId", uid);
                            startActivity(profileIntent);
                            finish();

                        } else {
                            // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            verifyProgressBar.setVisibility(View.GONE);
                            Toast.makeText(VerifyActivity.this, "Verification Failed:\n" + task.getException().getMessage(), Toast.LENGTH_LONG).show();

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(VerifyActivity.this, "verification code entered was invalid", Toast.LENGTH_LONG).show();
                                verifyBtn.setEnabled(true);
                            }
                        }
                    }
                });
    }

}
