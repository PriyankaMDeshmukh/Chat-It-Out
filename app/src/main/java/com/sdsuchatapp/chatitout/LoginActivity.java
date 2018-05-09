package com.sdsuchatapp.chatitout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int INTENT_EXAMPLE_REQUEST = 123;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;
    private static final int STATE_ALREADY_LOGGED_IN = 7;
    private static final String TAG = "LoginActivity";
    private ProgressDialog progressDialog;

    private String verificationId;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private ViewGroup mPhoneNumberViews;
    private ViewGroup mSignedInViews;

    private TextView mStatusText;
    private TextView mDetailText;

    private EditText mPhoneNumberField;
    private EditText mVerificationField;
    private EditText phoneNumber;
    private EditText phoneVerificationCode;

    private Button mStartButton;
    private Button mVerifyButton;
    private Button mResendButton;
    private Button mSignOutButton;
    private Button verifyCodeButton;

    private ProgressBar phoneProgressBar;
    private ProgressBar phoneVerificationProgressBar;

    private TextInputLayout phoneVerification;

    private FirebaseAuth auth;
    private DatabaseReference database;

    private boolean verificationInProgress = false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        setContentView(R.layout.activity_login);




        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Authenticating User");
        progressDialog.setMessage("Please wait while Chat-It-Out verifies you");
        progressDialog.setCanceledOnTouchOutside(false);

        phoneNumber = findViewById(R.id.phoneNumber);
        phoneProgressBar = findViewById(R.id.phoneProgressBar);
        phoneVerification = findViewById(R.id.phoneVerificationHolder);
        phoneVerificationProgressBar = findViewById(R.id.phoneVerificationProgress);
        verifyCodeButton = findViewById(R.id.verifyCodeButton);
        phoneVerificationCode = findViewById(R.id.phoneVerificationCode);

        phoneProgressBar.setVisibility(View.INVISIBLE);
        phoneVerification.setVisibility(View.INVISIBLE);
        phoneVerificationProgressBar.setVisibility(View.INVISIBLE);
        verifyCodeButton.setVisibility(View.INVISIBLE);
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);



        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                progressDialog.show();
                Log.d(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                phoneProgressBar.setVisibility(View.INVISIBLE);
                verificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                phoneProgressBar.setVisibility(View.INVISIBLE);
                verificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    phoneNumber.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verification,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verification);

                // Save verification ID and resending token so we can use them later

                verificationId = verification;
                mResendToken = token;

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT);
                // [END_EXCLUDE]
            }
        };
        // [END phone_auth_callbacks]
    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            // [START_EXCLUDE]
                            updateUI(STATE_SIGNIN_SUCCESS, user);
                            // [END_EXCLUDE]
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                mVerificationField.setError("Invalid code.");
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]




    private void updateUI(int uiState) {
        updateUI(uiState, auth.getCurrentUser(), null);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            updateUI(STATE_ALREADY_LOGGED_IN);
        } else {
            updateUI(STATE_INITIALIZED);
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, PhoneAuthCredential cred) {
        updateUI(uiState, null, cred);
    }

    private void updateUI(int uiState, final FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {
            case STATE_INITIALIZED:
                // Initialized state, show only the phone number field and start button
                Toast.makeText(getApplicationContext(), "State has been initialized", Toast.LENGTH_SHORT).show();
                break;
            case STATE_CODE_SENT:
                // Code sent state, show the verification field, the
                Toast.makeText(getApplicationContext(), "Code has been sent", Toast.LENGTH_SHORT).show();
                phoneProgressBar.setVisibility(View.INVISIBLE);
                phoneVerification.setVisibility(View.VISIBLE);
                verifyCodeButton.setVisibility(View.VISIBLE);
                break;
            case STATE_VERIFY_FAILED:
                // Verification has failed, show all options
                Toast.makeText(getApplicationContext(), "Verification has failed", Toast.LENGTH_SHORT).show();
                break;
            case STATE_VERIFY_SUCCESS:
                // Verification has succeeded, proceed to firebase sign in
                Toast.makeText(getApplicationContext(), "Verification has succeded", Toast.LENGTH_SHORT).show();
                break;
            case STATE_ALREADY_LOGGED_IN:
                Intent go = new Intent(getApplicationContext(),ChatActivity.class);
                startActivity(go);
                finish();

                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
                Toast.makeText(getApplicationContext(), "sign in failed", Toast.LENGTH_SHORT).show();
                phoneVerificationProgressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_SIGNIN_SUCCESS:
                // Np-op, handled by sign-in check
                phoneVerificationProgressBar.setVisibility(View.INVISIBLE);
                if(!progressDialog.isShowing())
                    progressDialog.show();

                //Go to Main Activity
                final String uid = user.getUid();
                if(uid!=null) {
                    Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_SHORT).show();
                    database = FirebaseDatabase.getInstance().getReference().child("Users");

                    //check if uid in database else enter uid
                    // Read from the database
                    database.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(uid)) {
                                database = database.child(uid);
                                database.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String name = dataSnapshot.child("displayName").getValue().toString();
                                        String profileThumbnail = dataSnapshot.child("profileThumbnail").getValue().toString();
                                        String profilePicture = dataSnapshot.child("profilePicture").getValue().toString();
                                        String phoneNumber = dataSnapshot.child("phoneNumber").getValue().toString();
                                        Intent go = new Intent(getApplicationContext(),RegistrationActivity.class);
                                        Bundle bundle = new Bundle();
                                        HashMap<String,String> userData = new HashMap<>();
                                        userData.put("displayName", name);
                                        userData.put("profileThumbnail", profileThumbnail);
                                        userData.put("profilePicture", profilePicture);
                                        userData.put("phoneNumber",phoneNumber);
                                        userData.put("uid",uid);
                                        bundle.putSerializable("HashMap", userData);
                                        go.putExtras(bundle);
                                        progressDialog.dismiss();
                                        startActivity(go);
                                        finish();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                        Log.w(TAG, "Failed to read value.", databaseError.toException());
                                    }
                                });
                            } else {
                                database = database.child(uid);
                                final HashMap<String, String> userData = new HashMap<>();
                                userData.put("displayName", "");
                                userData.put("profileThumbnail", "default");
                                userData.put("profilePicture", "default");
                                userData.put("uid",uid);
                                userData.put("phoneNumber",phoneNumber.getText().toString());
                                database.setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
                                            Intent go = new Intent(getApplicationContext(),RegistrationActivity.class);
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("HashMap", userData);
                                            go.putExtras(bundle);
                                            progressDialog.dismiss();
                                            startActivity(go);
                                            finish();


                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            progressDialog.dismiss();
                            Log.w(TAG, "Failed to read value.", error.toException());
                        }
                    });
                }
                break;
        }

        if (user == null) {
            // Signed out


        } else {
            // Signed in


        }
    }

    private boolean validatePhoneNumber() {
        String phone = phoneNumber.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            phoneNumber.setError("Invalid phone number.");
            return false;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    public void authenticate(View view){
        if(validatePhoneNumber())
            startPhoneNumberAuthentication(this.phoneNumber.getText().toString());
    }

    private void startPhoneNumberAuthentication(String phoneNumber) {
        // [START start_phone_auth]

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                callbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true;
        phoneProgressBar.setVisibility(View.VISIBLE);
    }


    public void verifyCode(View view) {

        phoneVerificationProgressBar.setVisibility(View.VISIBLE);
        String code = phoneVerificationCode.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }
}
