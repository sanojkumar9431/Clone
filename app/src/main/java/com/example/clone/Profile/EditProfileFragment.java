package com.example.clone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clone.R;
import com.example.clone.Share.ShareActivity;
import com.example.clone.dialogs.ConfirnPasswordDialog;
import com.example.clone.models.User;
import com.example.clone.models.UserAccountSettings;
import com.example.clone.models.UserSettings;
import com.example.clone.utils.FirbaseMethods;
import com.example.clone.utils.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment implements ConfirnPasswordDialog.OnConfirmPasswordListener {

    @Override
    public void onConfirmPassword(String password) {
        Log.d(TAG, "onConfirmPassword: get the paswword"+password);

        AuthCredential credential= EmailAuthProvider
                .getCredential(mAuth.getCurrentUser().getEmail(),password);

        mAuth.getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "user re authenticated ");

                            // ///////// checked to see if the email is not already present in the database
                            mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    if (task.isSuccessful()) {
                                        try {

                                        if (task.getResult().getSignInMethods().size() == 1) {
                                            Log.d(TAG, "onComplete: that email is already in use");
                                            Toast.makeText(getActivity(), "that email already exits", Toast.LENGTH_SHORT).show();
                                        } else  {
                                            Log.d(TAG, "onComplete: that email is already available");
                                            ///////////////the email is availabe for update
                                            mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d(TAG, "user email addresss updated: ");
                                                                Toast.makeText(getActivity(), "email updated", Toast.LENGTH_SHORT).show();
                                                                mFirbaseMethods.updateEmail(mEmail.getText().toString());
                                                            }
                                                        }
                                                    });

                                        }
                                        }
                                        catch (NullPointerException e)
                                        {
                                            Log.e(TAG, "onComplete: NullPointerException"+e.getMessage() );
                                        }
                                    }
                                }
                            });

                        }else {
                            Log.d(TAG, "onComplete: re auntheticate failed");
                        }
                    }
                });

    }
    private static final String TAG = "EditProfileFragment";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirbaseMethods mFirbaseMethods;
    private String userID;

    // edit profile fragment widget
    private EditText mDisplayName,mUsername,mWebsite,mDescription,mEmail,mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private ImageView mProfilePhoto;

    //variables
    private UserSettings mUserSettings;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view=inflater.inflate(R.layout.fragment_editprofile,container,false);
       mProfilePhoto=(CircleImageView) view.findViewById(R.id.profile_photo);
       mDisplayName=(EditText)view.findViewById(R.id.display_name);
       mUsername=(EditText)view.findViewById(R.id.username);
       mWebsite=(EditText)view.findViewById(R.id.website);
       mDescription=(EditText)view.findViewById(R.id.description);
       mEmail=(EditText)view.findViewById(R.id.email);
       mPhoneNumber=(EditText)view.findViewById(R.id.phoneNumber);
       mChangeProfilePhoto=(TextView)view.findViewById(R.id.changeProfilePhoto);
       mFirbaseMethods=new FirbaseMethods(getActivity());


       //setProfileImage();
        setupFirebaseAuth();

       // back arrow for navigating back to profile activity
        ImageView backArrow=(ImageView)view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to profile activity");
                getActivity().finish();
            }
        });

        ImageView checkmark=(ImageView)view.findViewById(R.id.savechanges);
        checkmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to save changes");
                saveProfileSettings();
            }
        });

       return view;
    }
    // retrives the data contained in the widget and submit it to the database
    //  before doing so it checks to make sure the username choosen is unique
    private void saveProfileSettings(){
        final String displayName=mDisplayName.getText().toString();
        final String username=mUsername.getText().toString();
        final String website=mWebsite.getText().toString();
        final String description=mDescription.getText().toString();
        final String email=mEmail.getText().toString();
        final long phoneNumber=Long.parseLong(mPhoneNumber.getText().toString());


        // case 1: if the user made a change to their username
        if(!mUserSettings.getUser().getUsername().equals(username)){
            checkIfUsernameExits(username);
        }
        // case 2: if the user made a change to their email
        if(!mUserSettings.getUser().getEmail().equals(email)){

            // step 1: reauthenticate ...... confrm password and email

            ConfirnPasswordDialog dialog=new ConfirnPasswordDialog();
            dialog.show(getFragmentManager(),getString(R.string.confirm_password_dialog));
            dialog.setTargetFragment(EditProfileFragment.this,1);

            // step 2 ;;    check if the email already exits ...... fetchProvidersforEmail(String email)
            // step 3::    check the email ..   ..   submit the new email to the database and authenticate

        }
        // change the rest of the settings that do not require uniquness
        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName)){
            mFirbaseMethods.updateUserAccountSettings(displayName,null,null,0);
        }
        if(!mUserSettings.getSettings().getWebsite().equals(website)){
            mFirbaseMethods.updateUserAccountSettings(null,website,null,0);
        }
        if(!mUserSettings.getSettings().getDescription().equals(description)){
            mFirbaseMethods.updateUserAccountSettings(null,null,description,0);
        }
        /*if(!mUserSettings.getSettings().getPhoneNumber().(Long.parseLong(equals(phoneNumber)){
            mFirbaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
        }*/
       if(!String.valueOf(mUserSettings.getSettings().getPhone_number()).equals(phoneNumber)){
           mFirbaseMethods.updateUserAccountSettings(null,null,null,phoneNumber);
       }
    }


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
                if(!dataSnapshot.exists()){
                    // add the user name
                    mFirbaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(),"saved username",Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    if(singleSnapshot.exists()){
                        Log.d(TAG, "check if username exits: found a match "+ singleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(),"that username already exits ",Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings){
        Log.d(TAG, "setProfileWidgets: setting widgets with data retriving from firebase database"+userSettings.toString());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retriving from firebase database"+userSettings.getUser().getEmail());
        Log.d(TAG, "setProfileWidgets: setting widgets with data retriving from firebase database"+userSettings.getUser().getPhone_number());

        mUserSettings=userSettings;
        // User user=userSettings.getUser();
        UserAccountSettings settings=userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText((String.valueOf(settings.getPhone_number())));
        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent=new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 268654345
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });

    }
    //---------------------------FIREBASE-------------------------------------------------

    // setup the firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        userID=mAuth.getCurrentUser().getUid();

        mAuthListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    Log.d(TAG, "onAuthStateChanged: signed_in" + user.getUid());
                }else {
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // retrieve user information from the database
                setProfileWidgets(mFirbaseMethods.getUserSettings(dataSnapshot));

                // retrive images for the user in question
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
