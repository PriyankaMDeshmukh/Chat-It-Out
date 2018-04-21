package com.sdsuchatapp.chatitout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        Bundle bundle = this.getIntent().getExtras();
        HashMap<String,String> userData = new HashMap<>();

        if(bundle != null) {
            userData = (HashMap<String, String>) bundle.getSerializable("HashMap");

        }

    }

    public void changeDisplayImage(View view) {

    }
}
