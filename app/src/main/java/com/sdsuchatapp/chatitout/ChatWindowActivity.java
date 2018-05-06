package com.sdsuchatapp.chatitout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ChatWindowActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        String userId =getIntent().getStringExtra("userId");
        TextView textView=this.findViewById(R.id.textView);
        textView.setText(userId);
    }
}
