package io.github.yuvrajsab.babblin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatSettings extends AppCompatActivity {

    ListView chatSettingsList;

    DatabaseReference databaseReference;
    String currentUserId;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        chatSettingsList = findViewById(R.id.chatSettingsList);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(currentUserId);
        }

        String[] items = {"Clear All Chats"};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        chatSettingsList.setAdapter(arrayAdapter);

        chatSettingsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatSettings.this);
                    builder.setTitle("Clear Chats");
                    builder.setMessage("Your all chats will be cleared.\nDo you want to continue ?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            databaseReference.removeValue();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    builder.setCancelable(false);
                    builder.show();
                }
            }
        });
    }
}
