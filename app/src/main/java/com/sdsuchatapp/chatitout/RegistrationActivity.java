package com.sdsuchatapp.chatitout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

//Reference for Image cropping: http://square.github.io/picasso/
//Picasso is a cropping library for android which we used for the image feature.
public class RegistrationActivity extends AppCompatActivity {

    private final String TAG = "RegistrationActivity";
    private DatabaseReference database;
    private FirebaseUser currentUser;
    private StorageReference profilePictureStorage;
    private ProgressDialog progressDialog;
    String displayName;
    String phoneNumber;
    String profilePicture;
    String profileThumbnail;
    private EditText displayNameInput;
    private CircleImageView profilePictureView;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        profilePictureStorage = FirebaseStorage.getInstance().getReference();
        displayNameInput = findViewById(R.id.displayName);
        profilePictureView = findViewById(R.id.userImage);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Bundle bundle = this.getIntent().getExtras();
        HashMap<String,String> userData;

        if(bundle != null) {
            userData = (HashMap<String, String>) bundle.getSerializable("HashMap");
            displayName = userData.get("displayName");
            profileThumbnail = userData.get("profileThumbnail");
            profilePicture = userData.get("profilePicture");
            phoneNumber = userData.get("phoneNumber");
            uid = userData.get("uid");
            database = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
            if (!displayName.equalsIgnoreCase("")){
                displayNameInput.setText(displayName);
            }
            if(!profileThumbnail.equalsIgnoreCase("default")){
                Picasso.get().load(profileThumbnail).placeholder(R.drawable.ic_person_black_48px).into(profilePictureView);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                signoutUser();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent startActivity = new Intent(this, LoginActivity.class);
        startActivity(startActivity);
        finish();

    }

    public void changeDisplayImage(View view) {

        CropImage.activity()
                .setAspectRatio(1,1)
                .start(this);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Uploading Image");
                    progressDialog.setMessage("Please wait while the image is being uploaded");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    Uri resultUri = result.getUri();
                    File thumbnailFile = new File(resultUri.getPath());
                    StorageReference profilePictureLocation = profilePictureStorage.child("profilePictures").child(uid+".jpg");
                    try {
                        Bitmap thumbnail = new Compressor(this)
                                .setMaxWidth(200)
                                .setMaxHeight(200)
                                .setQuality(50)
                                .compressToBitmap(thumbnailFile);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        final byte[] thumbnailBytes = baos.toByteArray();
                        final StorageReference profileThumbnailLocation = profilePictureStorage.child("profilePictures").child("thumbnail").child(uid+".jpg");
                        profilePictureLocation.putFile(resultUri).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){

                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = profileThumbnailLocation.putBytes(thumbnailBytes);
                                uploadTask.addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){
                                        final String thumbnailDownloadUrl = task1.getResult().getDownloadUrl().toString();
                                        Map pictures = new HashMap<>();
                                        pictures.put("profilePicture",downloadUrl);
                                        pictures.put("profileThumbnail",thumbnailDownloadUrl);
                                        database.updateChildren(pictures).addOnCompleteListener((OnCompleteListener<Void>) task11 -> {
                                            if(task11.isSuccessful()){
                                                Log.d(TAG, "Successfully uploaded picture");
                                                Picasso.get().load(thumbnailDownloadUrl).into(profilePictureView);
                                                progressDialog.dismiss();
                                            }else {
                                                progressDialog.dismiss();
                                            }
                                        });
                                    }else{
                                        Log.d(TAG, "Error in uploading thumbnail");
                                        progressDialog.dismiss();
                                    }
                                });


                            }else{
                                Log.d(TAG, "Error in uploading picture");
                                progressDialog.dismiss();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.d(TAG, result.getError().toString());
            }
        }
    }


    public void loginUser(View view) {
        final String displayName = displayNameInput.getText().toString();
        if(TextUtils.isEmpty(displayName)){
            displayNameInput.setError("Please Enter Display Name");
        }
        else{
            Map pictures = new HashMap<>();
            pictures.put("displayName",displayName);
            database.updateChildren(pictures).addOnCompleteListener((OnCompleteListener<Void>) task -> {
                if(task.isSuccessful()){
                    Intent go = new Intent(getApplicationContext(),ChatActivity.class);
                    Bundle bundle = new Bundle();
                    HashMap<String,String> userData = new HashMap<>();
                    userData.put("displayName", displayName);
                    userData.put("profileThumbnail", profileThumbnail);
                    userData.put("profilePicture", profilePicture);
                    userData.put("phoneNumber",phoneNumber);
                    userData.put("uid",uid);
                    bundle.putSerializable("HashMap", userData);
                    go.putExtras(bundle);
                    startActivity(go);
                    finish();

                }else {
                    Log.d(TAG, "Error in uploading display picture");

                }
            });
        }
    }
}
