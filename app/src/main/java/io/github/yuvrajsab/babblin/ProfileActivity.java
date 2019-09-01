package io.github.yuvrajsab.babblin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileDisplayName, profileStatus;
    private ImageView profileImage;
    private DatabaseReference databaseReference, friendReqDatabase, friendDatabase, notifyDatabase;
    private FirebaseUser current_user;
    private ProgressBar profileProgressBar;
    private int currentStatus;
    private Button sendReqBtn, rejectReqBtn;

    private String displayName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String uid = getIntent().getStringExtra("user_id");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        friendReqDatabase = FirebaseDatabase.getInstance().getReference().child("FriendReq");
        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
        notifyDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        profileDisplayName = findViewById(R.id.profileDisplayName);
        profileStatus = findViewById(R.id.profileStatus);
        profileImage = findViewById(R.id.profileImage);
        profileProgressBar = findViewById(R.id.profileProgressBar);
        sendReqBtn = findViewById(R.id.sendReqBtn);
        rejectReqBtn = findViewById(R.id.rejectReqBtn);
        current_user = FirebaseAuth.getInstance().getCurrentUser();

        profileProgressBar.setVisibility(View.VISIBLE);
        rejectReqBtn.setVisibility(View.GONE);
        rejectReqBtn.setEnabled(false);

        // 0 - Not Friends
        // 1 - Request sent
        // 2 - Request received
        // 3 - Friend
        currentStatus = 0;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                displayName = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                profileDisplayName.setText(displayName);
                profileStatus.setText(status);
                if (!image.equals("default")) {
                    Picasso.get().load(image).placeholder(R.drawable.default_avatar).into(profileImage);
                }

                friendReqDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(uid)) {
                            String req_type = Objects.requireNonNull(dataSnapshot.child(uid).child("request_type").getValue()).toString();

                            if (req_type.equals("received")) {
                                currentStatus = 2;
                                sendReqBtn.setText("Accept Friend Request");

                                rejectReqBtn.setVisibility(View.VISIBLE);
                                rejectReqBtn.setEnabled(true);
                            } else if (req_type.equals("sent")) {
                                currentStatus = 1;
                                sendReqBtn.setText("Cancel Friend Request");
                            }
                        } else {
                            friendDatabase.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(uid)) {
                                        currentStatus = 3;
                                        if (displayName != null) {
                                            sendReqBtn.setText("Unfriend " + displayName);
                                        } else {
                                            sendReqBtn.setText("Unfriend User");
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        profileProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        profileProgressBar.setVisibility(View.GONE);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                profileProgressBar.setVisibility(View.GONE);
            }
        });

        sendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReqBtn.setEnabled(false);

                // send request
                if (currentStatus == 0) {
                    Map reqMap = new HashMap();
                    reqMap.put(current_user.getUid() + "/" + uid + "/request_type", "sent");
                    reqMap.put(uid + "/" + current_user.getUid() + "/request_type", "received");

                    friendReqDatabase.updateChildren(reqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Failed to send request", Toast.LENGTH_LONG).show();
                            } else {
                                HashMap<String, String> notificationData = new HashMap<>();
                                notificationData.put("from", current_user.getUid());
                                notificationData.put("type", "request");

                                notifyDatabase.child(uid).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        currentStatus = 1;
                                        sendReqBtn.setText("Cancel Friend Request");
                                    }
                                });

                            }
                            sendReqBtn.setEnabled(true);
                        }
                    });
                }

                //cancel request
                if (currentStatus == 1) {
                    Map cancelReqMap = new HashMap();
                    cancelReqMap.put(current_user.getUid() + "/" + uid, null);
                    cancelReqMap.put(uid + "/" + current_user.getUid(), null);

                    friendReqDatabase.updateChildren(cancelReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Failed to cancel request", Toast.LENGTH_LONG).show();
                            } else {
                                currentStatus = 0;
                                sendReqBtn.setText("Send Friend Request");
                            }
                            sendReqBtn.setEnabled(true);
                        }
                    });
                }

                //accept request
                if (currentStatus == 2) {
                    final String currentDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).format(new Date());

                    Map acceptReqMap = new HashMap();
                    acceptReqMap.put(current_user.getUid() + "/" + uid + "/date", currentDate);
                    acceptReqMap.put(uid + "/" + current_user.getUid() + "/date", currentDate);

                    friendDatabase.updateChildren(acceptReqMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Failed to accept request", Toast.LENGTH_LONG).show();
                            } else {
                                Map cancelReqMap = new HashMap();
                                cancelReqMap.put(current_user.getUid() + "/" + uid, null);
                                cancelReqMap.put(uid + "/" + current_user.getUid(), null);

                                friendReqDatabase.updateChildren(cancelReqMap, null);

                                if (displayName != null) {
                                    sendReqBtn.setText("Unfriend " + displayName);
                                } else {
                                    sendReqBtn.setText("Unfriend User");
                                }
                                rejectReqBtn.setVisibility(View.GONE);
                                rejectReqBtn.setEnabled(false);
                                currentStatus = 3;
                            }
                            sendReqBtn.setEnabled(true);
                        }
                    });
                }

                //unfriend
                if (currentStatus == 3) {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put(current_user.getUid() + "/" + uid, null);
                    unfriendMap.put(uid + "/" + current_user.getUid(), null);

                    friendDatabase.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Failed to unfriend", Toast.LENGTH_LONG).show();
                            } else {
                                currentStatus = 0;
                                sendReqBtn.setText("Send Friend Request");
                            }
                            sendReqBtn.setEnabled(true);
                        }
                    });
                }
            }
        });

        rejectReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectReqBtn.setEnabled(false);

                Map cancelReqMap = new HashMap();
                cancelReqMap.put(current_user.getUid() + "/" + uid, null);
                cancelReqMap.put(uid + "/" + current_user.getUid(), null);

                friendReqDatabase.updateChildren(cancelReqMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(ProfileActivity.this, "Failed to decline request", Toast.LENGTH_LONG).show();
                            rejectReqBtn.setEnabled(true);
                        } else {
                            currentStatus = 0;
                            sendReqBtn.setText("Send Friend Request");
                            rejectReqBtn.setVisibility(View.GONE);
                            sendReqBtn.setEnabled(true);
                        }
                    }
                });
            }
        });

    }
}
