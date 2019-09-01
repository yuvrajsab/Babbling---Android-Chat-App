package io.github.yuvrajsab.babblin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class AllUsers extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    private FirebaseUser user;
    private RecyclerView usersList;

    private FirebaseRecyclerAdapter adapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        mAuth = FirebaseAuth.getInstance();

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        //main
        usersList = findViewById(R.id.usersList);
        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(linearLayoutManager);


        //current user
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("online");
            FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(query, Users.class).build();

            adapter = new FirebaseRecyclerAdapter<Users, AllUsers.usersviewholder>(options) {
                @NonNull
                @Override
                public usersviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.users_single_layout, parent, false);
                    return new usersviewholder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull usersviewholder holder, int position, @NonNull Users model) {
                    holder.setDisplayName(model.getName());
                    holder.setDisplayStatus(model.getStatus());
                    if (model.getThumbnail() != null) {
                        holder.setDisplayThumbnail(model.getThumbnail());
                    }
                    holder.setOnlineStatus(model.isOnline());

                    String phone = model.getPhone();

                    holder.v.setEnabled(true);
                    //current device phone
                    String currentPhone = user.getPhoneNumber();
                    if (currentPhone != null) {
                        if (currentPhone.equals(phone)) {
                            holder.setDisplayName("You");
                            holder.v.setEnabled(false);
                        } else {
                            holder.v.setEnabled(true);
                        }
                    }

                    final String uid = getRef(position).getKey();
                    final String uname = model.getName();
                    final String uimg = model.getThumbnail();

                    holder.v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            /*Intent chatIntent = new Intent(AllUsers.this, chatActivity.class);
                            chatIntent.putExtra("user_id", uid);
                            chatIntent.putExtra("user_name", uname);
                            chatIntent.putExtra("user_img", uimg);
                            startActivity(chatIntent);*/
                            Intent chatIntent = new Intent(AllUsers.this, ProfileActivity.class);
                            chatIntent.putExtra("user_id", uid);
                            startActivity(chatIntent);
                        }
                    });
                }
            };
            usersList.setAdapter(adapter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            sendtostart();
        } else {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (user != null) {
            adapter.stopListening();
        }
    }

    private void sendtostart() {
        Intent startIntent = new Intent(AllUsers.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    public class usersviewholder extends RecyclerView.ViewHolder {

        View v;

        usersviewholder(View itemView) {
            super(itemView);
            v = itemView;
        }

        void setDisplayName(String name) {
            TextView userName = v.findViewById(R.id.usersName);
            userName.setText(name);
        }

        void setDisplayStatus(String status) {
            TextView userStatus = v.findViewById(R.id.usersStatus);
            userStatus.setText(status);
        }

        void setDisplayThumbnail(String thumbnail) {
            ImageView userImage = v.findViewById(R.id.usersImage);
            if (!thumbnail.equals("default")) {
                Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.default_avatar);
            }
        }

        void setOnlineStatus(boolean online_status) {
            TextView onlineStatusText = v.findViewById(R.id.onlineStatusText);
            if (online_status) {
                onlineStatusText.setVisibility(View.VISIBLE);
            } else {
                onlineStatusText.setVisibility(View.INVISIBLE);
            }
        }
    }
}
