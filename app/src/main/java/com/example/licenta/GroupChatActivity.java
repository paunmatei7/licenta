 package com.example.licenta;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

 public class GroupChatActivity extends AppCompatActivity {

     private FirebaseAuth myAuth;

    private Toolbar myToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView myScrollView;
    private TextView displayTextMessages;

    private DatabaseReference rootRef, usersRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        myAuth = FirebaseAuth.getInstance();

        currentUserId = myAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("Users");
        groupNameRef = rootRef.child("Groups").child(currentGroupName);


        InitializeFields();
        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageInfoToDatabase();

                userMessageInput.setText("");
                myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

     @Override
     protected void onStart() {
         super.onStart();

         groupNameRef.addChildEventListener(new ChildEventListener() {
             @Override
             public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 if (dataSnapshot.exists()) {
                     DisplayMessages(dataSnapshot);
                 }
             }

             @Override
             public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                 if (dataSnapshot.exists()) {
                     DisplayMessages(dataSnapshot);
                 }
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

     private void DisplayMessages(DataSnapshot dataSnapshot) {
         Iterator iterator = dataSnapshot.getChildren().iterator();

         while (iterator.hasNext()) {
             String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
             String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
             String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
             String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();

             displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n");

             myScrollView.fullScroll(ScrollView.FOCUS_DOWN);
         }
     }

     private void InitializeFields() {
        myToolbar = (Toolbar) findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        userMessageInput = (EditText) findViewById(R.id.input_group_message);
        displayTextMessages = (TextView) findViewById(R.id.group_chat_text_display);

         myScrollView = (ScrollView) findViewById(R.id.my_scroll_view);
     }

     private void GetUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
     }

     private void SendMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey(); /// create key

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Write message before send", Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calendarDate.getTime());

            Calendar calendarTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calendarTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>(), messageInfoMap = new HashMap<>();

            groupNameRef.updateChildren(groupMessageKey);
            groupMessageKeyRef = groupNameRef.child(messageKey);

            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            groupMessageKeyRef.updateChildren(messageInfoMap);

        }
     }
 }
