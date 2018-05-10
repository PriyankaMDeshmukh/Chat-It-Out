package com.sdsuchatapp.chatitout;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowActivity extends AppCompatActivity {

    private TextView displayName;
    private CircleImageView profileThumbnailView;
    private DatabaseReference database;
    private FirebaseAuth auth;
    private String chatWithUserId;
    private String currentUserId;
    private ImageButton chatOptions;
    private ImageButton chatSendButton;
    private EditText chatMessage;
    private RecyclerView messagesListView;

    private final List<MessageBean> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference messageNotification;

    private int totalLoad = 10;
    private int currentlyLoaded = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        String userId =getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profileThumbnail = getIntent().getStringExtra("profileThumbnail");
        chatWithUserId = userId;
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        chatOptions = findViewById(R.id.chatOptions);
        chatSendButton = findViewById(R.id.chatSendButton);
        chatMessage = findViewById(R.id.chatMessage);

        messageAdapter = new MessageAdapter(messageList);

        messagesListView = findViewById(R.id.messagesList);
        linearLayoutManager = new LinearLayoutManager(this);
        messagesListView.setHasFixedSize(true);
        messagesListView.setLayoutManager(linearLayoutManager);

        messagesListView.setAdapter(messageAdapter);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);


        database = FirebaseDatabase.getInstance().getReference();
        messageNotification = FirebaseDatabase.getInstance().getReference().child("MessageNotifications");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chatScreenBar = inflater.inflate(R.layout.chat_screen_bar, null);
        actionBar.setCustomView(chatScreenBar);
        showAllMessages();
        displayName = findViewById(R.id.userName);
        profileThumbnailView = findViewById(R.id.chatUserThumbnail);
        displayName.setText(userName);
        if(!profileThumbnail.equalsIgnoreCase("default")){
            Picasso.get().load(profileThumbnail).placeholder(R.drawable.ic_person_black_48px).into(profileThumbnailView);

        }

        database.child("Chat").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( !dataSnapshot.hasChild(chatWithUserId)){
                    HashMap chatDetails = new HashMap();
                    chatDetails.put("chatSeen", false);
                    chatDetails.put("timestamp", ServerValue.TIMESTAMP);

                    //add chatDetails to both users map
                    HashMap chatUsers = new HashMap();
                    chatUsers.put("Chat/"+currentUserId+"/"+chatWithUserId, chatDetails);
                    chatUsers.put("Chat/"+chatWithUserId+"/"+chatWithUserId, chatDetails);

                    database.updateChildren(chatUsers, (databaseError, databaseReference) -> {
                        if(databaseError != null){
                            Log.d("ChatWindowActivity", databaseError.getMessage().toString());
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //on click listeners for chat message buttons
        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }


        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentlyLoaded++;
                showAllMessages();
            }
        });
    }

    private void showAllMessages() {

        DatabaseReference messageReference = database.child("messages").child(currentUserId).child(chatWithUserId);

        Query messageQuery = messageReference.limitToLast(currentlyLoaded+totalLoad);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageBean message = dataSnapshot.getValue(MessageBean.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                messagesListView.scrollToPosition(messageList.size()-1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



    private void sendMessage() {
        String message = chatMessage.getText().toString();
        if(!TextUtils.isEmpty(message)){
            //Message will be temporarily stored in Chat-It-Out Firebase Server and permanently in users sql-lite database
            //Whatsapp does something on the same lines to save storage space so we are trying to achieve something similar

            DatabaseReference userMessage = database.child("messages").child(currentUserId).child(chatWithUserId).push();

            //Store messages in Firebase for both the sender and reciever
            String senderReference = "messages/"+currentUserId+"/"+chatWithUserId;
            String recieverReference = "messages/"+chatWithUserId+"/"+currentUserId;

            String id = userMessage.getKey();

            //add the message to be stored, in a map along with its details
            HashMap messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen",false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", currentUserId);

            HashMap userMessageMap = new HashMap();
            userMessageMap.put(senderReference+"/"+id, messageMap);
            userMessageMap.put(recieverReference+"/"+id, messageMap);


            chatMessage.setText("");
            //now add this to firebase database
            database.updateChildren(userMessageMap, (databaseError, databaseReference) -> {
                if(databaseError != null){
                    Log.d("ChatWindowActivity", databaseError.getMessage().toString());
                }
            });
            HashMap<String,String> notificationMessage =new HashMap<>();
            notificationMessage.put(currentUserId+"ChatItOut"+message,"");


            messageNotification.child(chatWithUserId).push().setValue(notificationMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
            }

            );
        }
    }
}
