package io.github.yuvrajsab.babblin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class Feedback extends AppCompatActivity {

    Button feedbackSubmitBtn;
    EditText feedbackEt;
    DatabaseReference databaseReference;
    FirebaseUser currentuser;
    Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackEt = findViewById(R.id.feedbackEt);
        feedbackSubmitBtn = findViewById(R.id.feedbackSubmitBtn);

        currentDate = Calendar.getInstance().getTime();

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Feedback").child(currentuser.getUid());


        feedbackSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String feedbackMsg = feedbackEt.getText().toString();

                if (!TextUtils.isEmpty(feedbackMsg)) {
                    String time = currentDate.toString();
                    databaseReference.child(time).child("feedback_message").setValue(feedbackMsg).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Feedback.this, "Thanks for your feedback!", Toast.LENGTH_LONG).show();
                        }
                    });
                    feedbackEt.getText().clear();
                } else {
                    Toast.makeText(Feedback.this, "Please write feedback before submitting it", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
