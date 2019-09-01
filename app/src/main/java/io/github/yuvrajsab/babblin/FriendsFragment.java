package io.github.yuvrajsab.babblin;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;
    private View mainView;
    private FirebaseRecyclerAdapter adapter;
    private DatabaseReference userDatabase;

    private String currentUserId;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = mainView.findViewById(R.id.friendsList);
        friendsList.setHasFixedSize(true);
        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference().child("Friend").child(currentUserId);
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();

        adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());

                String listUserId = getRef(position).getKey();
                userDatabase.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String username = dataSnapshot.child("name").getValue().toString();
                        String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                        holder.setName(username);
                        holder.setThumbnail(thumbnail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                final String uid = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatIntent = new Intent(getContext(), chatActivity.class);
                        chatIntent.putExtra("user_id", uid);
                        chatIntent.putExtra("user_name", "name");
                        chatIntent.putExtra("user_img", "default");
                        startActivity(chatIntent);
                    }
                });
            }
        };

        friendsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();

        adapter.stopListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        View view;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            view = itemView;
        }

        public void setDate(String date) {
            TextView friendsDate = view.findViewById(R.id.usersStatus);
            friendsDate.setText("Friend since - " + date);
        }

        public void setName(String name) {
            TextView userName = view.findViewById(R.id.usersName);
            userName.setText(name);
        }

        public void setThumbnail(String thumbnail) {
            ImageView userImage = view.findViewById(R.id.usersImage);
            if (!thumbnail.equals("default")) {
                Picasso.get().load(thumbnail).placeholder(R.drawable.default_avatar).into(userImage);
            } else {
                userImage.setImageResource(R.drawable.default_avatar);
            }
        }
    }
}
