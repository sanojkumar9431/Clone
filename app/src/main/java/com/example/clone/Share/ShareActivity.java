package com.example.clone.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.clone.R;
import com.example.clone.utils.BottomNavigationViewHelper;
import com.example.clone.utils.Permission;
import com.example.clone.utils.SectionPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

public class ShareActivity extends AppCompatActivity {
    private static final String TAG = "ShareActivity";

    // constants
    private static final int ACTIVITY_NUM =2;
    private static final int VERIFY_PERMISSION_REQUEST=1;

    private ViewPager mViewPager;

    private Context mContext=ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        Log.d(TAG, "onCreate: started.");

        if(checkPermissionArray(Permission.PERMISSION)){
            setupViewPager();

        }
        else {
            verifyPermission(Permission.PERMISSION);
        }
        
        //setupBottomNavigationView();
    }

    //  return the current tab number  // 0-gallery  1- photo
    public int getCurrentTabNumber(){
        return mViewPager.getCurrentItem();
    }

    // setup view pager to manager the tabs

    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GalleryFragment());
        adapter.addFragment(new PhotoFragment());

        mViewPager=(ViewPager)findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setText(getString(R.string.gallery));
        tabLayout.getTabAt(1).setText(getString(R.string.photo));
    }

    public int getTask(){
        Log.d(TAG, "getTask: Task"+getIntent().getFlags());
        return getIntent().getFlags();
    }


// verify all the permission passed to the array
    public void verifyPermission(String[] permission){
        Log.d(TAG, "verifyPermission: verifying permission");

        ActivityCompat.requestPermissions(
                ShareActivity.this,
                permission,
                VERIFY_PERMISSION_REQUEST
        );
    }

    // check on array permission
    public boolean checkPermissionArray(String[] permission) {
        Log.d(TAG, "checkPermissionArray: checking permission array");

        for (int i=0;i<permission.length; i++){
            String check=permission[i];
            if (!checkPermissions(check)){
                return false;
            }
        }
        return true;
    }

    // check single permission is it has been verified
    public boolean checkPermissions(String permission) {
        Log.d(TAG, "checkPermissions: checking permission"+permission);

        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this,permission);

        if(permissionRequest!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermissions: \n permission was not grant for " + permission);
            return false;
        }
        else {
            Log.d(TAG, "checkPermissions: \n permission was granted for "+permission);
            return true;
        }
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
