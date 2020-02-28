package com.example.nainjanewswatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ChangePassword extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    byte[] image_data;
    String fullname,email,current_p,retype_p,prev_p;
    ImageView profile_pic;
    SQLiteHelper sqLiteHelper;
    AlertDialog.Builder builder;

    Button saveRecord;
    EditText prev, newP,retype;
    TextView connectionStatus;
    ProgressDialog pd;

    String allResult = null;
    URLConnection urlconnection;
    URL url;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";

    Toolbar toolbar;
    private static int SPLASH_TIME_OUT = 1000;//5seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        builder = new AlertDialog.Builder(this);
        sqLiteHelper = new SQLiteHelper(this,null,null,1);

        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Processing Login ...");
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        builder = new AlertDialog.Builder(this);
        getInitials();

        saveRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                connectionStatus.setText("");
                connectionStatus.setVisibility(View.INVISIBLE);
                connectionStatus.setVisibility(View.GONE);

                prev_p = prev.getText().toString();
                current_p = newP.getText().toString();
                retype_p = retype.getText().toString();
                //verify if all details are provided
                if(prev_p.isEmpty() || current_p.isEmpty()|| retype_p.isEmpty() || !current_p.equals(retype_p)){
                    //dont save
                    builder.setMessage("Error: Some Field are Empty Verify!");
                    builder.setTitle("Nainja News Watch");
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setCancelable(false);
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
                    alert.show();
                    connectionStatus.setVisibility(View.VISIBLE);
                    connectionStatus.setText("Error: Some Field are Empty Verify!");
                }
                else
                {
                   //check if previous password is correct
                    Cursor cursor = sqLiteHelper.userLogin(email,prev_p);
                    if(cursor.getCount() > 0 ) {
                        connectionStatus.setText("");
                        connectionStatus.setVisibility(View.INVISIBLE);
                        connectionStatus.setVisibility(View.GONE);
                        new testConnection().execute();
                    }else{
                        connectionStatus.setVisibility(View.VISIBLE);
                        connectionStatus.setText("Error: Previous Password is Incorrect !!");
                    }
                }
            }
        });
        Intent intent = getIntent();

        fullname = intent.getStringExtra("FULL_NAME");
        email = intent.getStringExtra("USERNAME");
        image_data = intent.getByteArrayExtra("PROF_PICS");
        CollapsingToolbarLayout tot = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        tot.setTitle(fullname);
        profile_pic = (ImageView) findViewById(R.id.profile_pic);
        Bitmap bitmap = BitmapFactory.decodeByteArray(image_data,0,image_data.length);
        profile_pic.setImageBitmap(bitmap);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(fab);

        initNavigationDrawer();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
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
                intent = new Intent (ChangePassword.this,UploadNews.class);
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

    class testConnection extends AsyncTask<String, Integer, String> {
        String outre;
        @Override
        protected String doInBackground(String... strings) {
            try{
                url = new URL(address);
                urlconnection = url.openConnection();
                urlconnection.setConnectTimeout(1500);
                urlconnection.connect();
                outre = "true";
                return outre;
            } catch (Exception e) {
                outre = "false";
                return outre;
            }
            //return null;
        }

        @Override
        protected void onPostExecute(String content) {
            if(outre.equals("true")) {
                connectionStatus.setText("");
                connectionStatus.setVisibility(View.INVISIBLE);
                connectionStatus.setVisibility(View.GONE);
                // new ReadJSON().execute();
                pd.show();
                volleyJsonArrayRequest(address);
            }
            else{
                pd.hide();
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText("Error: No Internet Connection !!");
            }
        }
    }

    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        pd.hide();
                        if(response.equals("Successfully Updated !!!")){
                            sqLiteHelper.updatePassword(current_p, email);
                            Toast.makeText(ChangePassword.this,"Your Password was Successfully Updated / Change !!",Toast.LENGTH_LONG).show();
                            //move to home Screen

                            Intent intent = new Intent(ChangePassword.this, blogView.class);
                            intent.putExtra("FULL_NAME", fullname);
                            intent.putExtra("USERNAME", email);
                            intent.putExtra("PROF_PICS", image_data);
                            startActivity(intent);
                            overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            finish();
                        }else{
                            connectionStatus.setVisibility(View.VISIBLE);
                            connectionStatus.setText(response);
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.hide();
                        connectionStatus.setVisibility(View.VISIBLE);
                        connectionStatus.setText(error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("opr", "changePassword");
                params.put("newPassword", current_p);
                params.put("userMail", email);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }


    public void getInitials(){
        saveRecord = (Button) findViewById(R.id.btnSave);
        prev = (EditText) findViewById(R.id.prev);
        newP = (EditText) findViewById(R.id.current);
        retype = (EditText) findViewById(R.id.current_re);
        connectionStatus   = (TextView)findViewById(R.id.status);
    }

    //navigation drawer
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
                        intent = new Intent (ChangePassword.this,blogView.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent (ChangePassword.this,Profileview.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent (ChangePassword.this,ChangePics.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();

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
                        intent = new Intent (ChangePassword.this,UploadNews.class);
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

                final Dialog d = new Dialog(ChangePassword.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCanceledOnTouchOutside(true);
                //  d.setTitle(fullname);
                d.setContentView(snackView);
                d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                d.show();
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (ChangePassword.this,ChangePics.class);
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
                        d.hide();
                    }
                });
                imv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (ChangePassword.this,Profileview.class);
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
    public void open_news(String newsCategory){
        Intent intent = new Intent(ChangePassword.this, NewsCategory.class);
        intent.putExtra("EXTRA_SEARCH", newsCategory);
        intent.putExtra("FULL_NAME", fullname);
        intent.putExtra("USERNAME", email);
        intent.putExtra("PROF_PICS", image_data);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
            Intent intent = new Intent(ChangePassword.this, blogView.class);
            intent.putExtra("FULL_NAME", fullname);
            intent.putExtra("USERNAME", email);
            intent.putExtra("PROF_PICS", image_data);
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
            finish();
        }
    }
    public void verify_close(){
        builder.setMessage("Do You Really Want to Exit Nainja News Watch... ?. ");
        builder.setTitle("Nainja News Watch");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dialogInterface.cancel();
                // System.exit(0);
                Intent intent = new Intent(ChangePassword.this, HomeScreen.class);
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
        getMenuInflater().inflate(R.menu.menu_navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.action_home:
                drawerLayout.closeDrawers();
                intent = new Intent (ChangePassword.this,blogView.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent (ChangePassword.this,Profileview.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent (ChangePassword.this,ChangePics.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
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
                intent = new Intent (ChangePassword.this,UploadNews.class);
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
}
