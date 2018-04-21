package com.sdsuchatapp.chatitout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllUsersFragment extends Fragment {

    private RecyclerView userLists; // RecyclerView is used to get a list of scrollable items. An alternative to ListView
    static Context refOfChatActicity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_all_users, container, false);
        refOfChatActicity=getContext(); //needed to implement onclickListener on ViewHolder
        userLists=(RecyclerView) view.findViewById(R.id.userLists);
        userLists.setLayoutManager(new LinearLayoutManager(getActivity())); //allows custom layout unlike ListView
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        Query getAllFriendsData = FirebaseDatabase.getInstance().getReference().child("Users");
        FirebaseRecyclerOptions<UserInformation> options = new FirebaseRecyclerOptions.Builder<UserInformation>()
                        .setQuery(getAllFriendsData, UserInformation.class)
                        .build();
        FirebaseRecyclerAdapter<UserInformation, IndividualUserInfo> getAllFriendsList=new FirebaseRecyclerAdapter<UserInformation, IndividualUserInfo>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IndividualUserInfo eachFriendDetails, int position, @NonNull UserInformation userDetails) {
                eachFriendDetails.setFirstName(userDetails.displayName);
                eachFriendDetails.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        Intent chatWindow = new Intent(refOfChatActicity, ChatWindowActivity.class); //this keyword did not work
                        refOfChatActicity.startActivity(chatWindow);
                    } });
            }

            @NonNull
            @Override
            public IndividualUserInfo onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_chat, parent, false);
                return new IndividualUserInfo(view);
            }
        };
        userLists.setAdapter(getAllFriendsList);
        getAllFriendsList.startListening();
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
