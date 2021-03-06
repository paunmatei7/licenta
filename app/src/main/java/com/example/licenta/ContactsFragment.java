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
import android.widget.ImageView;
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

public class ContactsFragment extends Fragment {

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;

    private View contactsView;
    private RecyclerView contactsList;

    private String currentUserId;


    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        contactsView =  inflater.inflate(R.layout.fragment_contacts, container, false);

        Initialize();

        return contactsView;
    }

    private void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        contactsList = (RecyclerView) contactsView.findViewById(R.id.contacts_list);
        contactsList.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        final  FirebaseRecyclerAdapter <Contacts, ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull Contacts model) {
                String userIds = getRef(position).getKey();

                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userStatus = dataSnapshot.child("status").getValue().toString();

                            String userUniversity = dataSnapshot.child("university").getValue().toString();

                            holder.userName.setText(userName);
                            holder.userStatus.setText(userStatus);
                            holder.userUniversity.setText(userUniversity);

                            if (dataSnapshot.child("User State").hasChild("state")) {
                                String state = dataSnapshot.child("User State").child("state").getValue().toString();
                                String date = dataSnapshot.child("User State").child("date").getValue().toString();
                                String time = dataSnapshot.child("User State").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else {
                                    if (state.equals("offline")) {
                                        holder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }
                                }
                            }
                            else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (dataSnapshot.hasChild("image")) {
                                String userImage = dataSnapshot.child("image").getValue().toString();

                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        profileIntent.putExtra("isGroupChatContacts", 0);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                ContactsViewHolder viewHolder = new ContactsViewHolder(view);

                return viewHolder;
            }
        };

        contactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus, userUniversity;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_username);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userUniversity  = itemView.findViewById(R.id.user_university);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }

    }

}