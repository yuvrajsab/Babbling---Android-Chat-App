package io.github.yuvrajsab.babblin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.scottyab.aescrypt.AESCrypt;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class chatActivity extends AppCompatActivity {

    DatabaseReference rootRef;
    FirebaseAuth auth;
    String currentUserid;
    String otherUserid;
    String otherUserImage;

    EditText chatEt;
    ImageButton chatAddBtn;
    ImageButton chatSendBtn;

    RecyclerView messagesList;
    final List<Messages> list_messages = new ArrayList<>();
    MessageAdapter adapter;

    public final static int Gallery_Pick = 2;
    StorageReference filePath;
    public final static int filePermission = 1;

    StorageChooser chooser;
    StorageReference fileUpload;

    String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();

        auth = FirebaseAuth.getInstance();
        currentUserid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        otherUserid = getIntent().getStringExtra("user_id");
        final String userName = getIntent().getStringExtra("user_name");
        final String userImg = getIntent().getStringExtra("user_img");

        LayoutInflater inflater = getLayoutInflater();
        View v = inflater.inflate(R.layout.custom_chat_toolbar, null);

        TextView display_name = v.findViewById(R.id.custom_chat_displayName);
        ImageView display_img = v.findViewById(R.id.custom_chat_toolbar_img);
        final TextView online_text = v.findViewById(R.id.custom_chat_isOnline);
        display_name.setText(userName);

        if (!userImg.equals("default")) {
            Picasso.get().load(userImg).placeholder(R.drawable.default_avatar).into(display_img);
        }

        DatabaseReference imageRef = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserid).child("image");
        imageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherUserImage = Objects.requireNonNull(dataSnapshot.getValue()).toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        display_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent = new Intent(chatActivity.this, ImagePreviewActivity.class);
                if (otherUserImage != null) {
                    imageIntent.putExtra("profile_image", otherUserImage);
                }
                imageIntent.putExtra("profile_name", userName);
                startActivity(imageIntent);
            }
        });

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(v);
        }

        rootRef.child("Users").child(otherUserid).child("online").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isOnline = (boolean) dataSnapshot.getValue();
                if (isOnline) {
                    online_text.setVisibility(View.VISIBLE);
                } else {
                    online_text.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        chatEt = findViewById(R.id.chatEt);
        chatAddBtn = findViewById(R.id.chatAddBtn);
        chatSendBtn = findViewById(R.id.chatSendBtn);

        adapter = new MessageAdapter(list_messages);
        messagesList = findViewById(R.id.messages_list);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(this));
        messagesList.setAdapter(adapter);

        loadMessages();

        //send message
        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        StorageChooser.Theme theme = new StorageChooser.Theme(this);
        int[] myScheme = theme.getDefaultScheme();
        final int color = Color.rgb(25, 118, 210);
        final int black = Color.rgb(0, 0, 0);

        myScheme[StorageChooser.Theme.OVERVIEW_STORAGE_TEXT_INDEX] = black;
        myScheme[StorageChooser.Theme.SEC_FOLDER_TINT_INDEX] = color;
        myScheme[StorageChooser.Theme.OVERVIEW_HEADER_INDEX] = color;
        myScheme[StorageChooser.Theme.OVERVIEW_MEMORYBAR_INDEX] = Color.rgb(255, 193, 7);
        myScheme[StorageChooser.Theme.SEC_ADDRESS_BAR_BG] = color;
        myScheme[StorageChooser.Theme.OVERVIEW_INDICATOR_INDEX] = black;

        theme.setScheme(myScheme);
        chooser = new StorageChooser.Builder()
                .withActivity(chatActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .setTheme(theme)
                .build();

        //chat add button
        chatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(chatActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.send_menu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.addImage:
                                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                                galleryIntent.setType("image/*");
                                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), Gallery_Pick);
                                return true;
                            case R.id.addFile:
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

                                    if (!hasPermissions(chatActivity.this, permissions)) {
                                        ActivityCompat.requestPermissions(chatActivity.this, permissions, filePermission);
                                    } else {
                                        AddFileData();
                                    }
                                } else {
                                    AddFileData();
                                }
                                return true;
                            default:
                                return false;
                        }
                    }
                });
            }
        });
    }

    public void AddFileData() {
        chooser.show();
        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String s) {
                File selectedFile = new File(s);
                long fileSize = selectedFile.length() / (1024 * 1024);
                if (fileSize <= 100) {
                    final String currentUserRef = "messages/" + currentUserid + "/" + otherUserid;
                    final String otherUserRef = "messages/" + otherUserid + "/" + currentUserid;

                    Uri file = Uri.fromFile(new File(s));
                    String filename = file.getLastPathSegment();
                    final DatabaseReference file_message = rootRef.child("messages").child(currentUserid).child(otherUserid).push();
                    final String push_id = file_message.getKey();

                    if (push_id != null) {
                        fileUpload = FirebaseStorage.getInstance().getReference().child(currentUserid).child(filename);
                    }
                    UploadTask uploadTask = fileUpload.putFile(file);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return fileUpload.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String download_url = task.getResult().toString();
                                Map fileMap = new HashMap();
                                fileMap.put("message", download_url);
                                fileMap.put("type", "other");
                                fileMap.put("time", ServerValue.TIMESTAMP);
                                fileMap.put("from", currentUserid);

                                Map otherfileMap = new HashMap();
                                otherfileMap.put(currentUserRef + "/" + push_id, fileMap);
                                otherfileMap.put(otherUserRef + "/" + push_id, fileMap);

                                rootRef.updateChildren(otherfileMap);
                            } else {
                                Toast.makeText(chatActivity.this, "unable to send file, try again.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(chatActivity.this, "File size is too large, File size must be under 100mb", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void loadMessages() {
        rootRef.child("messages").child(currentUserid).child(otherUserid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);

                long timestamp = messages.getTime() + 86400000;
                long currentTime = System.currentTimeMillis();

                if (timestamp > currentTime) {
                    list_messages.add(messages);
                    adapter.notifyDataSetChanged();

                    messagesList.scrollToPosition(list_messages.size() - 1);
                } else {
                    DatabaseReference databaseReference1 = rootRef.child("messages").child(currentUserid).child(otherUserid);
                    DatabaseReference databaseReference2 = rootRef.child("messages").child(otherUserid).child(currentUserid);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(currentUserid);
                    storageReference.delete();
                    databaseReference1.removeValue();
                    databaseReference2.removeValue();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String password = "Babblin-Chat";
        String message = chatEt.getText().toString();
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.message_send);

        if (!TextUtils.isEmpty(message)) {
            try {
                msg = AESCrypt.encrypt(password, message);
            } catch (GeneralSecurityException e) {
                Toast.makeText(this, "Some error occured,\nPlease report error to us in Report Problem section.", Toast.LENGTH_LONG).show();
            }
            //map for current user
            String currentUserRef = "messages/" + currentUserid + "/" + otherUserid;
            String otherUserRef = "messages/" + otherUserid + "/" + currentUserid;

            DatabaseReference user_message_push = rootRef.child("messages").child(currentUserid).child(otherUserid).push();

            String push_id = user_message_push.getKey();

            Map msgCurrentUserMap = new HashMap();
            msgCurrentUserMap.put("message", msg);
            msgCurrentUserMap.put("type", "text");
            msgCurrentUserMap.put("time", ServerValue.TIMESTAMP);
            msgCurrentUserMap.put("from", currentUserid);

            Map msgOtherUserMap = new HashMap();
            msgOtherUserMap.put(currentUserRef + "/" + push_id, msgCurrentUserMap);
            msgOtherUserMap.put(otherUserRef + "/" + push_id, msgCurrentUserMap);

            rootRef.updateChildren(msgOtherUserMap);
            messagesList.scrollToPosition(list_messages.size() - 1);
            chatEt.getText().clear();


            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
            }
            mp.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            final String currentUserRef = "messages/" + currentUserid + "/" + otherUserid;
            final String otherUserRef = "messages/" + otherUserid + "/" + currentUserid;

            final DatabaseReference image_message = rootRef.child("messages").child(currentUserid).child(otherUserid).push();

            final String push_id = image_message.getKey();

            filePath = FirebaseStorage.getInstance().getReference().child(currentUserid).child(push_id + ".jpg");
            if (imageUri != null) {
                filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String download_url = uri.toString();

                                    Map imageMap = new HashMap();
                                    imageMap.put("message", download_url);
                                    imageMap.put("type", "image");
                                    imageMap.put("time", ServerValue.TIMESTAMP);
                                    imageMap.put("from", currentUserid);

                                    Map otherImageMap = new HashMap();
                                    otherImageMap.put(currentUserRef + "/" + push_id, imageMap);
                                    otherImageMap.put(otherUserRef + "/" + push_id, imageMap);

                                    rootRef.updateChildren(otherImageMap);
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == filePermission) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AddFileData();
            } else {
                Toast.makeText(this, "It requires permission to use this feature, Please Allow It", Toast.LENGTH_LONG).show();
            }
        }
    }
}
