package com.example.clone.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.clone.R;
import com.example.clone.models.Comment;
import com.example.clone.models.Like;
import com.example.clone.models.Photo;
import com.example.clone.models.UserAccountSettings;
import com.example.clone.models.UserSettings;
import com.example.clone.utils.BottomNavigationViewHelper;
import com.example.clone.utils.FirbaseMethods;
import com.example.clone.utils.GridImageAdapter;
import com.example.clone.utils.UniversalImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    public interface OnGridImageSelectedListener{
        void onGridImageSelected(Photo photo, int activityNumber);
    }
    OnGridImageSelectedListener mOnGridImageSelectedListener;

    private static final int ACTIVITY_NUM =4;
    private static final int NUM_GRID_COLUMNS =3;


    // firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirbaseMethods mFirbaseMethods;

// widgets
    private TextView mPosts, mFollowers, mFollowing, mDisplayName, mUsername, mWebsite,mDescription;
    private ProgressBar mProgressBar;
    private CircleImageView mProfilePhoto;
    private GridView gridView;
    private Toolbar toolbar;
    private ImageView profileMenu;
    private BottomNavigationViewEx buttomNavigationView;

    private Context mContext;

    // vars

    private int mFollowersCount=0;
    private int mFollowingCount=0;
    private int mPostsCount=0;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile,container,false);
        mDisplayName =(TextView)view.findViewById(R.id.display_name);
        mUsername =(TextView)view.findViewById(R.id.username);
        mWebsite =(TextView)view.findViewById(R.id.website);
        mDescription =(TextView)view.findViewById(R.id.description);
        mProfilePhoto=(CircleImageView)view.findViewById(R.id.profile_photo);
        mPosts =(TextView)view.findViewById(R.id.tvpost);
        mFollowing =(TextView)view.findViewById(R.id.tvfollowing);
        mFollowers =(TextView)view.findViewById(R.id.tvfollowers);
        mProgressBar=(ProgressBar)view.findViewById(R.id.profileprogressbar);
        gridView=(GridView)view.findViewById(R.id.gridview);
        toolbar=(Toolbar) view.findViewById(R.id.profileToolbar);
        profileMenu=(ImageView)view.findViewById(R.id.profileMenu);
        buttomNavigationView=(BottomNavigationViewEx)view.findViewById(R.id.buttomNavViewBar);
        mContext=getActivity();
        mFirbaseMethods=new FirbaseMethods(getActivity());
        Log.d(TAG, "onCreateView: started");

        setupBottomNavigationView();
        setupToolbar();

        setupFirebaseAuth();
        setupGridView();

        getFollowersCount();
        getFollowingCount();
        getPostsCount();

        TextView editProfile =(TextView)view.findViewById(R.id.texteditprofile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to" + mContext.getString(R.string.edit_profile_fragment));
                Intent intent=new Intent(getActivity(),AccountSettingActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);

            }
        });

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
            mOnGridImageSelectedListener =(OnGridImageSelectedListener)getActivity();
        }
        catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException"+ e.getMessage() );
        }

        super.onAttach(context);
    }

    private void setupGridView(){
        Log.d(TAG, "setupGridView: setting up image grid");

        final ArrayList<Photo> photos=new ArrayList<>();
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for( DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                   // photos.add(singleSnapshot.getValue(Photo.class));
                    Photo photo = new Photo();
                    Map<String, Object> objectMap =(HashMap<String, Object>)singleSnapshot.getValue();

                    try {
                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setData_created(objectMap.get(getString(R.string.field_data_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()) {
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setData_created(dSnapshot.getValue(Comment.class).getData_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);

                        List<Like> likesList = new ArrayList<Like>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_likes)).getChildren()) {
                            Like like = new Like();
                            like.setUser_id(dSnapshot.getValue(Like.class).getUser_id());
                            likesList.add(like);
                        }
                        photo.setLikes(likesList);
                        photos.add(photo);
                    }catch (NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException"+ e.getMessage() );
                    }
                }
                //setup our img grid
                int gridWidth=getResources().getDisplayMetrics().widthPixels;
                int imageWidth =gridWidth/NUM_GRID_COLUMNS;
                gridView.setColumnWidth(imageWidth);

                ArrayList<String> imgUrls = new ArrayList<String>();
                for (int i=0; i<photos.size();i++){
                    imgUrls.add(photos.get(i).getImage_path());
                }
                GridImageAdapter adapter=new GridImageAdapter(getActivity(),R.layout.layout_grid_imageview,"",imgUrls);
                gridView.setAdapter(adapter);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mOnGridImageSelectedListener.onGridImageSelected(photos.get(position),ACTIVITY_NUM);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: query canceled");

            }
        });
    }

    private void getFollowersCount(){
        mFollowersCount=0;

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_followers))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found followers" + singleSnapshot.getValue());
                    mFollowersCount++;
                }
                mFollowers.setText(String.valueOf(mFollowersCount));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFollowingCount(){
        mFollowingCount=0;

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_following))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found following user" + singleSnapshot.getValue());
                    mFollowingCount++;
                }
                mFollowing.setText(String.valueOf(mFollowingCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getPostsCount(){
        mPostsCount=0;

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: found post" + singleSnapshot.getValue());
                    mPostsCount++;
                }
                mPosts.setText(String.valueOf(mPostsCount));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void setProfileWidgets(UserSettings userSettings){
       // Log.d(TAG, "setProfileWidgets: setting widgets with data retriving from firebase database"+userSettings.toString());
       // Log.d(TAG, "setProfileWidgets: setting widgets with data retriving from firebase database"+userSettings.getSettings().getUsername());

       // User user=userSettings.getUser();
        UserAccountSettings settings=userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(),mProfilePhoto,null,"");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());


        mProgressBar.setVisibility(View.GONE);

    }

    //responsible for setting up the profile toolbar

    private void setupToolbar(){

        ((ProfileActivity)getActivity()).setSupportActionBar(toolbar);

        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account setting");
                Intent intent=new Intent(mContext,AccountSettingActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            }
        });
    }

    // buttom navigation setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewHelper.setupBottomNavigationView(buttomNavigationView);
        BottomNavigationViewHelper.enableNavigation(mContext,getActivity(),buttomNavigationView);

        Menu menu=buttomNavigationView.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
    //---------------------------FIREBASE-------------------------------------------------

    // setup the firebase auth object
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();

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
