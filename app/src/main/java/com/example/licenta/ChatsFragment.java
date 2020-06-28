package com.example.licenta;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatList;

    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;

    private String currentUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView =  inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = (RecyclerView) privateChatView.findViewById(R.id.private_chat_list);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String userIds = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            final String name = dataSnapshot.child("name").getValue().toString();
                            final String status = dataSnapshot.child("status").getValue().toString();
                            final String university = dataSnapshot.child("university").getValue().toString();

                            if (dataSnapshot.hasChild("image")) {
                                retImage[0] = dataSnapshot.child("image").getValue().toString();

                                Picasso.get().load(retImage[0]).into(holder.profileImage);

                            }

                            holder.userName.setText(name);
                            holder.userUniversity.setText(university);


                            if (dataSnapshot.child("User State").hasChild("state")) {
                                String state = dataSnapshot.child("User State").child("state").getValue().toString();
                                String date = dataSnapshot.child("User State").child("date").getValue().toString();
                                String time = dataSnapshot.child("User State").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.userStatus.setText("online");
                                }
                                else {
                                    if (state.equals("offline")) {
                                        holder.userStatus.setText("Last Seen: " + date + " " + time);
                                    }
                                }
                            }
                            else {
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userIds);
                                    chatIntent.putExtra("visit_user_name", name);
                                    chatIntent.putExtra("visit_user_image", retImage[0]);

                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent,false);

                return new ChatsViewHolder(view);

            }
        };
        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus, userUniversity;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_username);
            userStatus = itemView.findViewById(R.id.user_status);
            userUniversity = itemView.findViewById(R.id.user_university);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}