package com.sdsuchatapp.chatitout;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowActivity extends AppCompatActivity {

    private TextView displayName;
    private CircleImageView profileThumbnailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        String userId =getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profileThumbnail = getIntent().getStringExtra("profileThumbnail");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View chatScreenBar = inflater.inflate(R.layout.chat_screen_bar, null);
        actionBar.setCustomView(chatScreenBar);

        displayName = findViewById(R.id.userName);
        profileThumbnailView = findViewById(R.id.chatUserThumbnail);
        displayName.setText(userName);
        if(!profileThumbnail.equalsIgnoreCase("default")){
            Picasso.get().load(profileThumbnail).placeholder(R.drawable.ic_person_black_48px).into(profileThumbnailView);

        }

    }
}
