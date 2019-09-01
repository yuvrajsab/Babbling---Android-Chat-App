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

public class ReportProblem extends AppCompatActivity {

    EditText reportErrorEt;
    Button reportErrorBtn;
    DatabaseReference databaseReference;
    FirebaseUser currentuser;
    Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_problem);

        reportErrorEt = findViewById(R.id.reportErrorEt);
        reportErrorBtn = findViewById(R.id.reportErrorBtn);

        currentDate = Calendar.getInstance().getTime();

        currentuser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Problem").child(currentuser.getUid());

        reportErrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = reportErrorEt.getText().toString();

                if (!TextUtils.isEmpty(message)) {
                    String time = currentDate.toString();
                    databaseReference.child(time).child("problem_message").setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ReportProblem.this, "we've recorded your problem, we'll try to fix it ASAP.", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    reportErrorEt.setError("Required");
                    reportErrorEt.requestFocus();
                }
            }
        });
    }
}
