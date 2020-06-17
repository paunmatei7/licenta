package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseReference usersRef, chatReqRef, contactsRef, notificationRef;
    private FirebaseAuth mAuth;

    private String receiveUserID, senderUserId, currentState;

    private CircleImageView userProfileImage;
    private TextView userProfileName,  userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Initialize();

        mAuth = FirebaseAuth.getInstance();
        chatReqRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiveUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = mAuth.getCurrentUser().getUid();

        RetrieveInfo();
    }

    private void Initialize() {
        userProfileImage = (CircleImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_user_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_user_status);
        sendMessageRequestButton = (Button) findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = (Button) findViewById(R.id.decline_message_request_button);

        currentState = "new";

    }

    private void RetrieveInfo() {
        usersRef.child(receiveUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && dataSnapshot.hasChild("image")) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageRequest();

                }
                else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageRequest() {
        chatReqRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiveUserID)) {
                    String req_type = dataSnapshot.child(receiveUserID).child("request_type").getValue().toString();

                    if (req_type.equals("sent")) {
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("Cancel Friend Request");
                    }
                    else {
                        if(req_type.equals("received")) {
                            currentState = "request_received";

                            sendMessageRequestButton.setText("Accept Friend Request");
                            declineMessageRequestButton.setVisibility(View.VISIBLE);
                            declineMessageRequestButton.setEnabled(true);

                            declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CancelRequest();
                                }
                            });
                        }
                    }
                }
                else {
                    contactsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiveUserID)) {
                                currentState = "friends";
                                sendMessageRequestButton.setText("Remove Contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (!senderUserId.equals(receiveUserID)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);

                    if (currentState.equals("new")) {
                        SendRequest();
                    }

                    if (currentState.equals("request_sent")) {
                        CancelRequest();
                    }

                    if (currentState.equals("request_received")) {
                        AcceptRequest();
                    }

                    if (currentState.equals("friends")) {
                        RemoveContact();
                    }
                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void RemoveContact() {
        contactsRef.child(senderUserId).child(receiveUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiveUserID).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                currentState = "new";
                                                sendMessageRequestButton.setText("Send Friend Request");
                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                declineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptRequest() { /// salvez contacte, elimin requesturile
        contactsRef.child(senderUserId).child(receiveUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    contactsRef.child(receiveUserID).child(senderUserId).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        chatReqRef.child(senderUserId).child(receiveUserID).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    chatReqRef.child(receiveUserID).child(senderUserId).removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    currentState = "friends";
                                                                    sendMessageRequestButton.setText("Remove Friend");

                                                                    declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                    declineMessageRequestButton.setEnabled(false);
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                }
            }
        });
    }

    private void CancelRequest() {
        chatReqRef.child(senderUserId).child(receiveUserID).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    chatReqRef.child(receiveUserID).child(senderUserId).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        sendMessageRequestButton.setEnabled(true);
                                        currentState = "new";
                                        sendMessageRequestButton.setText("Send Friend Request");
                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        declineMessageRequestButton.setEnabled(false);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void SendRequest() {
        chatReqRef.child(senderUserId).child(receiveUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    chatReqRef.child(receiveUserID).child(senderUserId).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        HashMap<String, String> chatNotif = new HashMap<>();

                                        chatNotif.put("from", senderUserId);
                                        chatNotif.put("type", "request");

                                        notificationRef.child(receiveUserID).push().setValue(chatNotif)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        sendMessageRequestButton.setEnabled(true);
                                                        currentState = "request_sent";
                                                        sendMessageRequestButton.setText("Cancel Friend Request");
                                                    }
                                                }
                                            });
                                    }
                                }
                            });
                }
            }
        });

    }
}
