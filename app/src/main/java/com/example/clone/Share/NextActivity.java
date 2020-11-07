package com.example.clone.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clone.R;
import com.example.clone.utils.FirbaseMethods;
import com.example.clone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";

    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirbaseMethods mFirbaseMethods;

    //widgets
    private EditText mCaption;

    // vars
    private String mAppend="file:///";
    private int imageCount=0;
    private String imgUrl;
    private Bitmap bitmap;
    private Intent intent;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        mFirbaseMethods=new FirbaseMethods(NextActivity.this);
        mCaption=(EditText)findViewById(R.id.caption);

        setupFirebaseAuth();

        ImageView backArrow = (ImageView)findViewById(R.id.ivbackarrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });

        TextView share =(TextView)findViewById(R.id.tvshare);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to the final share screen");
                // upload the images to firebase
                Toast.makeText(NextActivity.this,"Attempting to upload new photo",Toast.LENGTH_SHORT).show();
                String caption=mCaption.getText().toString();

                if(intent.hasExtra(getString(R.string.selected_image))){
                    imgUrl=intent.getStringExtra(getString(R.string.selected_image));
                    mFirbaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,imgUrl,null);

                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
                    mFirbaseMethods.uploadNewPhoto(getString(R.string.new_photo),caption,imageCount,null,bitmap);

                }

            }
        });
        setImage();
    }

    // get the image url from the incoming intent and displays the choosen image
    private void setImage(){
        intent=getIntent();
        ImageView image=(ImageView)findViewById(R.id.imageshare);

        if(intent.hasExtra(getString(R.string.selected_image))){
            imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            Log.d(TAG, "setImage: got new imgurl" + imgUrl);
            UniversalImageLoader.setImage(imgUrl,image,null,mAppend);
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            Log.d(TAG, "setImage: got new bitmap");
            image.setImageBitmap(bitmap);
        }

       // imgUrl=intent.getStringExtra(getString(R.string.selected_image));
       // UniversalImageLoader.setImage(imgUrl,image,null,mAppend);
    }

    //---------------------------FIREBASE-------------------------------------------------

    // setup the firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        Log.d(TAG, "onDataChange: image count "+ imageCount);

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

                imageCount=mFirbaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: image count "+ imageCount);


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
