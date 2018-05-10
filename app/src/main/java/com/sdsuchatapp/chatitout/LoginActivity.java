package com.sdsuchatapp.chatitout;

import android.app.ProgressDialog;
import android.content.Intent;
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


import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
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
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

//Reference: https://firebase.google.com/docs/auth/android/phone-auth

public class LoginActivity extends AppCompatActivity {

    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int STATE_CODE_SENT = 1;
    private static final int STATE_SIGNIN_FAILED = 2;
    private static final int STATE_SIGNIN_SUCCESS = 3;
    private static final int STATE_ALREADY_LOGGED_IN = 4;
    private int timeoutDuration = 60;
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
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }
        setContentView(R.layout.activity_login);




        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.loginProgressTitle));
        progressDialog.setMessage(getString(R.string.loginProgressMessage));
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

                progressDialog.show();
                Log.d(TAG, getString(R.string.loginVerificationSuccess) + credential);
                phoneProgressBar.setVisibility(View.INVISIBLE);
                verificationInProgress = false;
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Log.w(TAG, getString(R.string.loginVerificationFailed), e);
                phoneProgressBar.setVisibility(View.INVISIBLE);
                verificationInProgress = false;


                if (e instanceof FirebaseAuthInvalidCredentialsException) {

                    phoneNumber.setError(getString(R.string.invalidPhone));

                } else if (e instanceof FirebaseTooManyRequestsException) {

                    Snackbar.make(findViewById(android.R.id.content), R.string.qoutaExceeded,
                            Snackbar.LENGTH_SHORT).show();

                }


            }

            @Override
            public void onCodeSent(String verification,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, getString(R.string.codeSentMsg) + verification);

                // Save verification ID and resending token so we can use them later

                verificationId = verification;
                mResendToken = token;

                updateUI(STATE_CODE_SENT);
            }
        };

    }

    // [START sign_in_with_phone]
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, getString(R.string.signInSuccess));
                        FirebaseUser user = task.getResult().getUser();
                        updateUI(STATE_SIGNIN_SUCCESS, user);




                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, getString(R.string.signInFailiure), task.getException());
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            phoneVerificationCode.setError(getString(R.string.invalidCode));
                        }
                        updateUI(STATE_SIGNIN_FAILED);
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
        }
    }

    private void updateUI(int uiState, FirebaseUser user) {
        updateUI(uiState, user, null);
    }

    private void updateUI(int uiState, final FirebaseUser user, PhoneAuthCredential cred) {
        switch (uiState) {

            case STATE_CODE_SENT:
                // Code sent state, show the verification fieldx
                phoneProgressBar.setVisibility(View.INVISIBLE);
                phoneVerification.setVisibility(View.VISIBLE);
                verifyCodeButton.setVisibility(View.VISIBLE);
                break;

            case STATE_ALREADY_LOGGED_IN:
                Intent go = new Intent(getApplicationContext(),ChatActivity.class);
                startActivity(go);
                finish();
                break;
            case STATE_SIGNIN_FAILED:
                // No-op, handled by sign-in check
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
                    database = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebaseDatabaseUsers));

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
                                        String name = dataSnapshot.child(getString(R.string.usersName)).getValue().toString();
                                        String profileThumbnail = dataSnapshot.child(getString(R.string.usersThumbnail)).getValue().toString();
                                        String profilePicture = dataSnapshot.child(getString(R.string.usersPicture)).getValue().toString();
                                        String phoneNumber = dataSnapshot.child(getString(R.string.usersPhone)).getValue().toString();
                                        Intent go = new Intent(getApplicationContext(),RegistrationActivity.class);
                                        Bundle bundle = new Bundle();
                                        HashMap<String,String> userData = new HashMap<>();
                                        userData.put(getString(R.string.usersName), name);
                                        userData.put(getString(R.string.usersThumbnail), profileThumbnail);
                                        userData.put(getString(R.string.usersPicture), profilePicture);
                                        userData.put(getString(R.string.usersPhone),phoneNumber);
                                        userData.put(getString(R.string.usersUid),uid);
                                        bundle.putSerializable("HashMap", userData);
                                        go.putExtras(bundle);
                                        progressDialog.dismiss();
                                        startActivity(go);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        progressDialog.dismiss();
                                        Log.w(TAG, getString(R.string.databaseException), databaseError.toException());
                                    }
                                });
                            } else {
                                database = database.child(uid);
                                String userPhoneToken = FirebaseInstanceId.getInstance().getToken();
                                final HashMap<String, String> userData = new HashMap<>();
                                userData.put(getString(R.string.usersName), "");
                                userData.put(getString(R.string.usersThumbnail), getString(R.string.defaultDbValues));
                                userData.put(getString(R.string.usersPicture), getString(R.string.defaultDbValues));
                                userData.put(getString(R.string.usersUid),uid);
                                userData.put(getString(R.string.usersPhone),phoneNumber.getText().toString());
                                userData.put(getString(R.string.usersToken),userPhoneToken);
                                database.setValue(userData).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Intent go1 = new Intent(getApplicationContext(),RegistrationActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("HashMap", userData);
                                        go1.putExtras(bundle);
                                        progressDialog.dismiss();
                                        startActivity(go1);
                                        finish();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            progressDialog.dismiss();
                            Log.w(TAG, getString(R.string.databaseException), error.toException());
                        }
                    });
                }
                break;
        }


    }

    private boolean validatePhoneNumber() {
        String phone = phoneNumber.getText().toString();
        if (TextUtils.isEmpty(phone)) {
            phoneNumber.setError(getString(R.string.phoneNumberEmpty));
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


        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                timeoutDuration,
                TimeUnit.SECONDS,
                this,
                callbacks);


        verificationInProgress = true;
        phoneProgressBar.setVisibility(View.VISIBLE);
    }


    public void verifyCode(View view) {
        if(!TextUtils.isEmpty(phoneVerificationCode.getText().toString())) {
            phoneVerificationProgressBar.setVisibility(View.VISIBLE);

            String code = phoneVerificationCode.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
        else{
            phoneVerificationCode.setError(getString(R.string.verificationCodeEmpty));
        }
    }
}
