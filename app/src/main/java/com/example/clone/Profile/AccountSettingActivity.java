package com.example.clone.Profile;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.example.clone.R;
import com.example.clone.utils.BottomNavigationViewHelper;
import com.example.clone.utils.FirbaseMethods;
import com.example.clone.utils.SectionPagerAdapter;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class AccountSettingActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingActivity";
    private static final int ACTIVITY_NUM =4;



    private Context mContext;
    public SectionPagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetting);
        mContext=AccountSettingActivity.this;
        Log.d(TAG, "onCreate: started");
        mViewPager=(ViewPager)findViewById(R.id.viewpager_container);
        mRelativeLayout=(RelativeLayout)findViewById(R.id.relLayout1);

        setupSettingList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();

        //setup the backarrow for navigating back to profileactivity
        ImageView backArrow=(ImageView)findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to profileactivity");
                finish();
            }
        });
    }
    private void getIncomingIntent(){
        Intent intent=getIntent();

        if(intent.hasExtra(getString(R.string.selected_image)) ||
                intent.hasExtra(getString(R.string.selected_bitmap))){

        // if there is an imageUrl attached as an extra, there it was chooosen from the gallery/ photo fragment
       // if(intent.hasExtra(getString(R.string.selected_image))){
            Log.d(TAG, "getIncomingIntent: new incoming imgUrl");
            if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))){

                if(intent.hasExtra(getString(R.string.selected_image))){
                    // set the new profile picture
                    FirbaseMethods firbaseMethods=new FirbaseMethods(AccountSettingActivity.this);
                    firbaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null,0,
                            intent.getStringExtra(getString(R.string.selected_image)),null);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    // set the new profile picture
                    FirbaseMethods firbaseMethods=new FirbaseMethods(AccountSettingActivity.this);
                    firbaseMethods.uploadNewPhoto(getString(R.string.profile_photo),null,0,
                            null,(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }
            }
        }

        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getIncomingIntent: received incoming intent from" + getString((R.string.profile_activity)));
            setViewPager(pagerAdapter.getItemPosition(getString(R.string.edit_profile_fragment)));

        }
    }

    private void setupFragments(){
        pagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment());
        pagerAdapter.addFragment(new SignOutFragment());

    }

    public void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment #:" +fragmentNumber);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);

    }

    private void setupSettingList(){
        Log.d(TAG, "setupSettingList: initializing 'account setting' list.");
        ListView listView=(ListView)findViewById(R.id.lvAccountsettings);

        ArrayList<String> options =new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.sign_out_fragment));

        ArrayAdapter adapter = new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,options);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment # "+position);
                setViewPager(position);
            }
        });
    }
    // buttom navigation setup
    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx=(BottomNavigationViewEx) findViewById(R.id.buttomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext,this,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
