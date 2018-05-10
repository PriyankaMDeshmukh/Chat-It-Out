package com.sdsuchatapp.chatitout;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class AllUsersFragment extends Fragment {
    public final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS=100;
    private String TAG = "AllUsersFragment";
    private ProgressDialog progressDialog;
    ArrayList<String> listAllContacts;
    private RecyclerView userLists; // RecyclerView is used to get a list of scrollable items. An alternative to ListView
    static Context refOfChatActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_all_users, container, false);
        refOfChatActivity =getContext(); //needed to implement onclickListener on ViewHolder
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.contactsSyncProgressTitle));
        progressDialog.setMessage(getString(R.string.contactsSyncProgressMessage));
        progressDialog.setCanceledOnTouchOutside(false);
        userLists= view.findViewById(R.id.userLists);
        userLists.setLayoutManager(new LinearLayoutManager(getActivity())); //allows custom layout unlike ListView
        return view;
    }
    @Override
    public void onStart(){
        super.onStart();
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

        }
        else{

           showAllUsers();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showAllUsers();
                } else {
                    FirebaseAuth.getInstance().signOut();
                    Intent startActivity = new Intent(getActivity(), LoginActivity.class);
                    startActivity(startActivity);
                    getActivity().finish();

                }
                return;
            }

        }
    }

    private void showAllUsers() {



        try {
            InputStream file = new BufferedInputStream(getContext().openFileInput(getString(R.string.allContactsFile)));
            byte[] data = new byte[file.available()];
            file.read(data, 0, file.available());
            listAllContacts = new ArrayList(Arrays.asList(new String(data).replace("[","").replace("]","").split("\\s*,\\s*")));
            file.close();
        } catch (Exception noFile) {

        }
        if(listAllContacts==null) {

            if(!progressDialog.isShowing())
            {
                progressDialog.show();

            }

            listAllContacts = contactRegisteredForApp();
            try {
                OutputStream file = new BufferedOutputStream(getContext().openFileOutput(getString(R.string.allContactsFile), MODE_PRIVATE));
                file.write(listAllContacts.toString().getBytes());
                file.close();
            } catch (Exception noFile) {
                Log.e(TAG, getString(R.string.writeFail) + noFile.toString());
            }
        }

        Query getAllFriendsData = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebaseDatabaseUsers));
        FirebaseRecyclerOptions<UserInformation> options = new FirebaseRecyclerOptions.Builder<UserInformation>()
                .setQuery(getAllFriendsData, UserInformation.class)
                .build();
        FirebaseRecyclerAdapter<UserInformation, IndividualUserInfo> getAllFriendsList=new FirebaseRecyclerAdapter<UserInformation, IndividualUserInfo>(options) {
            @Override
            protected void onBindViewHolder(@NonNull IndividualUserInfo eachFriendDetails, int position, @NonNull UserInformation userDetails) {
                if(listAllContacts.contains(userDetails.phoneNumber)){

                    eachFriendDetails.setFirstName(userDetails.displayName);
                    eachFriendDetails.setProfileThumbnail(userDetails.profileThumbnail);
                    final String userId=getRef(position).getKey();
                    final String displayName = userDetails.displayName;
                    final String profileThumbnail = userDetails.profileThumbnail;
                    eachFriendDetails.itemView.setOnClickListener(arg0 -> {
                        Intent chatWindow = new Intent(refOfChatActivity, ChatWindowActivity.class);
                        chatWindow.putExtra(getString(R.string.userId),userId);
                        chatWindow.putExtra(getString(R.string.userName),displayName);
                        chatWindow.putExtra(getString(R.string.usersThumbnail),profileThumbnail);
                        refOfChatActivity.startActivity(chatWindow);
                    });
                }
                else{
                    eachFriendDetails.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                    eachFriendDetails.itemView.setVisibility(View.GONE);
                }
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
        if(progressDialog.isShowing())
            progressDialog.dismiss();
    }

    public ArrayList<String> contactRegisteredForApp(){
        ArrayList<String> listAllContacts = new ArrayList();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phone = pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if(!listAllContacts.contains(phone))
                        listAllContacts.add(phone);
                    }
                    pCur.close();
                }
            }
        }
        return listAllContacts;

    }

    public static class IndividualUserInfo extends RecyclerView.ViewHolder {
        View mView;
        public IndividualUserInfo(View view){
            super(view);
            mView=view;
        }
        public void setFirstName(String firstName) {
            TextView userFirstName =mView.findViewById(R.id.userName);
            userFirstName.setText(firstName);
        }
        public void setProfileThumbnail(String profileThumbnail) {
            CircleImageView profileThumbnailImage =mView.findViewById(R.id.userImage);
            Picasso.get().load(profileThumbnail).placeholder(R.drawable.defaultuser).into(profileThumbnailImage);
        }
    }
}
