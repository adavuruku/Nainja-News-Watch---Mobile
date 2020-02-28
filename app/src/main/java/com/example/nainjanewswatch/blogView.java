package com.example.nainjanewswatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class blogView extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    byte[] image_data;
    String fullname,email;

    public ArrayList<Product> articleslist;
    public ArrayList<Product> sportlist;
    public ArrayList<Product> politiclist;
    public ArrayList<Product> otherslist;

    AlertDialog.Builder builder;
    public String mmyhost=null;

    private ImageView mProfileImage;
   // articles articles;politics politics;sport sport;others others;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_view);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        builder = new AlertDialog.Builder(this);
        Intent intent = getIntent();
        fullname = intent.getStringExtra("FULL_NAME");
        email = intent.getStringExtra("USERNAME");
        image_data = intent.getByteArrayExtra("PROF_PICS");

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               fab.performLongClick();
           }
       });
        //register for contextmenu
        registerForContextMenu(fab);
        //start Navigation drawer
        initNavigationDrawer();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.createmenu,menu);
    }

    @Override
    public boolean onContextItemSelected(
            MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_upvideo:
                break;
            case R.id.action_upnews:
                intent = new Intent (blogView.this,UploadNews.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }

    public void open_news(String newsCategory){
      Intent intent = new Intent(blogView.this, NewsCategory.class);
        intent.putExtra("EXTRA_SEARCH", newsCategory);
        intent.putExtra("FULL_NAME", fullname);
        intent.putExtra("USERNAME", email);
        intent.putExtra("PROF_PICS", image_data);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();
                Intent intent;
                switch (id){
                    case R.id.action_home:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent (blogView.this,Profileview.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent (blogView.this,ChangePics.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();
                        intent = new Intent (blogView.this,ChangePassword.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;

                    case R.id.action_politics:
                        drawerLayout.closeDrawers();
                        open_news("Politics");
                        break;
                    case R.id.action_sport:
                        drawerLayout.closeDrawers();
                        open_news("Sports");
                        break;
                    case R.id.action_articles:
                        drawerLayout.closeDrawers();
                        open_news("Articles");
                        break;
                    case R.id.action_tech:
                        drawerLayout.closeDrawers();
                        open_news("Tech");
                        break;
                    case R.id.action_health:
                        drawerLayout.closeDrawers();
                        open_news("Health");
                        break;
                    case R.id.action_entertainment:
                        drawerLayout.closeDrawers();
                        open_news("Entertainment");
                        break;
                    case R.id.action_metron:
                        drawerLayout.closeDrawers();
                        open_news("Metro");
                        break;
                    case R.id.action_bussines:
                        drawerLayout.closeDrawers();
                        open_news("Business");
                        break;
                    case R.id.action_interview:
                        drawerLayout.closeDrawers();
                        open_news("Features / Interview");
                        break;
                    case R.id.action_rellationship:
                        drawerLayout.closeDrawers();
                        open_news("Relationship");
                        break;
                    case R.id.action_videos:
                        drawerLayout.closeDrawers();
                      //  open_news("Videos");
                        break;
                    case R.id.action_signout:
                        drawerLayout.closeDrawers();
                        verify_close();
                        break;
                    case R.id.action_upnews:
                        drawerLayout.closeDrawers();
                        intent = new Intent (blogView.this,UploadNews.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_upvideo:
                        drawerLayout.closeDrawers();
                        // view_Account();
                        break;
                }
                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView tv_email = (TextView)header.findViewById(R.id.tv_email);
        TextView tv_fullname = (TextView)header.findViewById(R.id.tv_name);
        tv_email.setText(email);
        tv_fullname.setText(fullname);
        ImageView imageV = (ImageView)header.findViewById(R.id.profile_image);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_data,0,image_data.length);
        imageV.setImageBitmap(bitmap);

        imageV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View snackView = getLayoutInflater().inflate(R.layout.dialogview, null);
                ImageView imv = (ImageView) snackView.findViewById(R.id.diaprofile_pic);
                Bitmap bitmap = BitmapFactory.decodeByteArray(image_data,0,image_data.length);
                imv.setImageBitmap(bitmap);
                TextView myvi = (TextView) snackView.findViewById(R.id.txtUser);
                TextView btnPassword = (TextView) snackView.findViewById(R.id.btnPassword);
                TextView btnPics = (TextView) snackView.findViewById(R.id.btnPics);
                myvi.setText(fullname);

                Dialog d = new Dialog(blogView.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCanceledOnTouchOutside(true);
              //  d.setTitle(fullname);
                d.setContentView(snackView);
                d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                d.show();
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (blogView.this,ChangePics.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
                btnPassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (blogView.this,ChangePassword.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
               imv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (blogView.this,Profileview.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                    }
                });
            }
        });

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.drawer_open,R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
    public void verify_close(){

        builder.setMessage("Do You Really Want to Exit Nainja News Watch... ?. ");
        builder.setTitle("Nainja News Watch");
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dialogInterface.cancel();
                // System.exit(0);
                Intent intent = new Intent(blogView.this, HomeScreen.class);
                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
                //System.exit(0);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
        alert.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_navigation_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.action_home:
                drawerLayout.closeDrawers();
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent (blogView.this,Profileview.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent (blogView.this,ChangePics.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
                intent = new Intent (blogView.this,ChangePassword.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_politics:
                drawerLayout.closeDrawers();
                open_news("Politics");
                break;
            case R.id.action_sport:
                drawerLayout.closeDrawers();
                open_news("Sports");
                break;
            case R.id.action_articles:
                drawerLayout.closeDrawers();
                open_news("Articles");
                break;
            case R.id.action_tech:
                drawerLayout.closeDrawers();
                open_news("Tech");
                break;
            case R.id.action_health:
                drawerLayout.closeDrawers();
                open_news("Health");
                break;
            case R.id.action_entertainment:
                drawerLayout.closeDrawers();
                open_news("Entertainment");
                break;
            case R.id.action_metron:
                drawerLayout.closeDrawers();
                open_news("Metro");
                break;
            case R.id.action_bussines:
                drawerLayout.closeDrawers();
                open_news("Business");
                break;
            case R.id.action_interview:
                drawerLayout.closeDrawers();
                open_news("Features / Interview");
                break;
            case R.id.action_rellationship:
                drawerLayout.closeDrawers();
                open_news("Relationship");
                break;
            case R.id.action_videos:
                drawerLayout.closeDrawers();
                //  open_news("Videos");
                break;
            case R.id.action_signout:
                drawerLayout.closeDrawers();
                verify_close();
                break;
            case R.id.action_upnews:
                drawerLayout.closeDrawers();
                intent = new Intent (blogView.this,UploadNews.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_upvideo:
                drawerLayout.closeDrawers();
                // view_Account();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

   // deleted placeholder fragment

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    politics politicsnews = new politics();
                    return politicsnews;
                case 1:
                    sport sportnews = new sport();
                    return sportnews;
                case 2:
                    articles articlesnew = new articles();
                    return articlesnew;
             case 3:
                    others others = new others();
                    return others;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "POLITICS";
                case 1:
                    return "SPORTS";
                case 2:
                    return "ARTICLES";
                case 3:
                    return "OTHERS";

            }
            return null;
        }
    }
}
