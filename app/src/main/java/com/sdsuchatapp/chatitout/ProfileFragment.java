package com.sdsuchatapp.chatitout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    View view;
    private DatabaseReference database;
    private static final int INTENT_EXAMPLE_REQUEST = 123;
    private Toolbar toolbar;
    private FirebaseUser currentUser;
    private StorageReference profilePictureStorage;
    private ProgressDialog progressDialog;
    private String displayName;
    private String phoneNumber;
    private String profilePicture;
    private String profileThumbnail;
    private EditText displayNameInput;
    private CircleImageView profilePictureView;
    private String uid;
    private FirebaseAuth auth;
    private String currentUserId;
    private CircleImageView changeImage;
    private Button changeDisplayName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        profilePictureStorage = FirebaseStorage.getInstance().getReference();
        displayNameInput = view.findViewById(R.id.displayName);
        profilePictureView = view.findViewById(R.id.userImage);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(currentUserId)) {
                    database = database.child(currentUserId);
                    database.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            displayName = dataSnapshot.child("displayName").getValue().toString();
                            profileThumbnail = dataSnapshot.child("profileThumbnail").getValue().toString();
                            profilePicture = dataSnapshot.child("profilePicture").getValue().toString();
                            phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                            if (!displayName.equalsIgnoreCase("")) {
                                displayNameInput.setText(displayName);
                            }
                            if (!profileThumbnail.equalsIgnoreCase("default")) {
                                Picasso.get().load(profileThumbnail).placeholder(R.drawable.ic_person_black_48px).into(profilePictureView);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            progressDialog.dismiss();
                            Log.w("Profile", "Failed to read value.", databaseError.toException());
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        changeImage = view.findViewById(R.id.userImage);
        changeDisplayName = view.findViewById(R.id.button);

        changeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDisplayImage(view);
            }
        });
        changeDisplayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(view);
            }
        });
        return view;
    }

    public void changeDisplayImage(View view) {
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(getActivity(),this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Uploading Image");
                progressDialog.setMessage("Please wait while the image is being uploaded");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                File thumbnailFile = new File(resultUri.getPath());
                StorageReference profilePictureLocation = profilePictureStorage.child("profilePictures").child(uid+".jpg");
                try {
                    Bitmap thumbnail = new Compressor(getActivity())
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumbnailFile);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumbnailBytes = baos.toByteArray();
                    final StorageReference profileThumbnailLocation = profilePictureStorage.child("profilePictures").child("thumbnail").child(uid+".jpg");
                    profilePictureLocation.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = profileThumbnailLocation.putBytes(thumbnailBytes);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful()){
                                            final String thumbnailDownloadUrl = task.getResult().getDownloadUrl().toString();
                                            Map pictures = new HashMap<>();
                                            pictures.put("profilePicture",downloadUrl);
                                            pictures.put("profileThumbnail",thumbnailDownloadUrl);
                                            database.updateChildren(pictures).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(), "Successful uploading", Toast.LENGTH_SHORT).show();
                                                        Picasso.get().load(thumbnailDownloadUrl).into(profilePictureView);
                                                        progressDialog.dismiss();

                                                    }else {
                                                        progressDialog.dismiss();
                                                    }
                                                }
                                            });
                                        }else{
                                            Toast.makeText(getContext(), "Error in Uploading Thumbnail", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(getContext(), "Error in Uploading Picture", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }


    public void loginUser(View view) {
        final String displayName = displayNameInput.getText().toString();
        if(TextUtils.isEmpty(displayName)){
            displayNameInput.setError("Enter Display Name");
        }
        else{
            Map pictures = new HashMap<>();
            pictures.put("displayName",displayName);
            database.updateChildren(pictures).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getContext(), "Changes saved successfully", Toast.LENGTH_SHORT).show();

                    }else {
                        Toast.makeText(getContext(), "Error in Uploading Display Picture", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
}