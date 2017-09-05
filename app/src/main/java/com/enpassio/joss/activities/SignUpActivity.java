package com.enpassio.joss.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.enpassio.joss.R;
import com.enpassio.joss.utils.InternetConnectivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignUpActivity extends AppCompatActivity {
    @BindView(R.id.user_name_edit_text)
    EditText userNameEditText;

    @BindView(R.id.user_city_edit_text)
    EditText userCityEditText;

    @BindView(R.id.user_email_edit_text)
    EditText userEmailEditText;

    @BindView(R.id.password_edit_text)
    EditText accountPasswordEditText;

    @BindView(R.id.button_sign_up)
    Button signUpButton;

    @BindView(R.id.button_already_registered)
    Button logInButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String email;
    private String password;
    private String name;
    private String city;
    private int gender;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDrivesDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            //if user is signed in, then go to the HiringListActivity
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        }
    }

    @OnClick(R.id.button_sign_up)
    public void signUpButtonClicked() {
        email = userEmailEditText.getText().toString().trim();
        password = accountPasswordEditText.getText().toString().trim();
        name = userNameEditText.getText().toString().trim();
        city = userCityEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            userNameEditText.setError(getResources().getString(R.string.error_enter_your_name));
            return;
        }
        if (TextUtils.isEmpty(city)) {
            userCityEditText.setError(getResources().getString(R.string.error_enter_your_city));
            return;
        }
        if (TextUtils.isEmpty(email)) {
            userEmailEditText.setError(getResources().getString(R.string.error_enter_valid_email));
            return;
        }
        if (password.length() < 6) {
            accountPasswordEditText.setError(getResources().getString(R.string
                    .error_password_less_than_six_character_long));
            return;
        }
        if (InternetConnectivity.isInternetConnected(SignUpActivity.this)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information

                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    // User is signed in
                                    // NOTE: this Activity should get onpen only when the user is not signed in, otherwise
                                    // the user will receive another verification email.

                                    mFirebaseDatabase = FirebaseDatabase.getInstance();
                                    mDrivesDatabaseReference = mFirebaseDatabase.getReference().child(getResources()
                                            .getString(R.string.firebase_database_child_drives));

//                                    mUserProfile = new UserProfile("", "", email);
//                                    FirebaseDatabase.getInstance().getReference().child("userProfile").child(FirebaseAuth
//                                            .getInstance().getCurrentUser().getUid()).push().setValue(mUserProfile);

                                    mAuth.signOut();
                                    sendVerificationEmail();
                                } else {
                                    // User is signed out

                                }

                            } else {
                                // If sign up fails, display a message to the user.

                                Toast.makeText(SignUpActivity.this, getResources()
                                                .getString(R.string.toast_problem_creating_account),
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        } else {
            Toast.makeText(SignUpActivity.this, getResources()
                    .getString(R.string.check_internet_connectivity), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.button_already_registered)
    public void alreadyRegisteredSignIn() {
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }

    /* code below referenced from: https://stackoverflow.com/a/41780828/5770629 */
    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

                        }
                    }
                });
    }
}
