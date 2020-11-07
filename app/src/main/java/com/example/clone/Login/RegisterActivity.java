package com.example.clone.Login;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clone.R;
import com.example.clone.models.User;
import com.example.clone.utils.FirbaseMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private Context mContext;
    private String email, password,username;
    private ProgressBar mProgressBar;
    private EditText mEmail,mPassword,mUsername;
    private TextView loadingPleaseWait;
    private Button btnRegister;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirbaseMethods firbaseMethods;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference myRef;
    private String append ="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mContext=RegisterActivity.this;
        firbaseMethods=new FirbaseMethods(mContext);
        Log.d(TAG, "onCreate: started");

        initWidgets();
        setupFirebaseAuth();
        init();
    }

    private void init(){
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=mEmail.getText().toString();
                username=mUsername.getText().toString();
                password=mPassword.getText().toString();

                if(checkInputs(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    loadingPleaseWait.setVisibility(View.VISIBLE);

                    firbaseMethods.registerNewEmail(email,password,username);
                }
            }
        });
    }


    private boolean checkInputs(String email, String username, String password){
        Log.d(TAG, "checkInputs: checking inputs for null values");
        if(email.equals("") || username.equals("") || password.equals("")){
            Toast.makeText(mContext,"All fields must be filled out",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // initialize the activity widgets
    private void initWidgets(){
        Log.d(TAG, "initWidgets: Initialising Widgets");
        mEmail=(EditText)findViewById(R.id.input_email);
        mUsername=(EditText)findViewById(R.id.input_username);
        btnRegister=(Button)findViewById(R.id.btn_register);
        mProgressBar=(ProgressBar)findViewById(R.id.progressbar);
        loadingPleaseWait=(TextView)findViewById(R.id.loadingPleaseWait);
        mPassword=(EditText)findViewById(R.id.input_password);
        mContext=RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
        loadingPleaseWait.setVisibility(View.GONE);
    }
    private boolean isStringNul(String string){
        Log.d(TAG, "isStringNul: checking string if null");
        if(string.equals("")){
            return true;
        }else {
            return false;
        }
    }
//------------------------firebase--------------------------------------
// check @param username already exits in the database
private void checkIfUsernameExits(final String username) {
    Log.d(TAG, "checkIfUsernameExits: checking if"+ username + "already exits");

    DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
    Query query=reference
            .child(getString(R.string.dbname_users))
            .orderByChild(getString(R.string.field_username))
            .equalTo(username);
    query.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                if(singleSnapshot.exists()){
                    Log.d(TAG, "check if username exits: found a match "+ singleSnapshot.getValue(User.class).getUsername());
                    append=myRef.push().getKey().substring(3,10);
                    Log.d(TAG, "onDataChange: username already exits. appending random string to name:" +append);
                }
            }
            String mUsername = "";
            mUsername=username + append;

            // add new user to the database

            firbaseMethods.addNewUser(email,mUsername,"","","",0L);
            Toast.makeText(mContext,"Signup successfull. sending verification email",Toast.LENGTH_SHORT).show();

            mAuth.signOut();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });

}
    // setup the firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mfirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mfirebaseDatabase.getReference();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null){
                    Log.d(TAG, "onAuthStateChanged: signed_in" + user.getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUsernameExits(username);


                            //add new user_account_settings to the database
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    finish();

                }else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };
    }
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    public void onStop(){
        super.onStop();
        if(mAuthListener!=null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
