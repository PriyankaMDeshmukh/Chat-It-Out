package com.sdsuchatapp.chatitout;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class TestActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView userLists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        userLists=(RecyclerView) this.findViewById(R.id.userLists);
        userLists.setHasFixedSize(true);
        userLists.setLayoutManager(new LinearLayoutManager(this));
    }
    @Override
    public void onStart(){
        super.onStart();
        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");
        FirebaseRecyclerOptions<UserInformation> options =
                new FirebaseRecyclerOptions.Builder<UserInformation>()
                        .setQuery(query, UserInformation.class)
                        .build();
        FirebaseRecyclerAdapter<UserInformation, AllUsersFragment.IndividualUserInfo> f=new FirebaseRecyclerAdapter<UserInformation, AllUsersFragment.IndividualUserInfo>(options) {
            @Override
            protected void onBindViewHolder(@NonNull AllUsersFragment.IndividualUserInfo holder, int position, @NonNull UserInformation model) {
                holder.setFirstName(model.displayName);
            }

            @NonNull
            @Override
            public AllUsersFragment.IndividualUserInfo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_chat, parent, false);

                return new AllUsersFragment.IndividualUserInfo(view);

            }
        };
        userLists.setAdapter(f);
        f.startListening();
    }
    public static class IndividualUserInfo extends RecyclerView.ViewHolder {
        View mView;
        public IndividualUserInfo(View view){
            super(view);
            mView=view;
        }

        public void setFirstName(String firstName) {
            TextView userfirstName =mView.findViewById(R.id.userName);
            userfirstName.setText(firstName);
        }
    }
}
