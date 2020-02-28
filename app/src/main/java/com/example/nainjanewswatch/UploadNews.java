package com.example.nainjanewswatch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class UploadNews extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    byte[] image_data,imageBytes;
    String fullname,email,newsDetailsP,descriptionP,newsID;
    ImageView profile_pic,myPics;
    EditText description, newsDetails;
    AlertDialog.Builder builder;
    TextView btnClose,status;
    Button saveRecord;
    SQLiteHelper sqLiteHelper;
    TextView connectionStatus;
    String imageSelected = "No";
    ProgressDialog pd;
    URLConnection urlconnection;
    Snackbar snackbar;
    Uri FileUri;
    final int REQUEST_CODE_GALLERY=999;
    final int REQUEST_CODE_CAMERA=777;
    URL url;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";
    Toolbar toolbar;
    private static int SPLASH_TIME_OUT = 1000;//5seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_news);

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

                newsDetailsP = newsDetails.getText().toString();
                descriptionP = description.getText().toString();
                //verify if all details are provided
                if(newsDetailsP.isEmpty() || descriptionP.isEmpty() || imageSelected.equals("No")){
                    //dont save
                    builder.setMessage("Error: Some Field are Empty Verify!");
                    builder.setTitle("Nainja News Watch");
                    builder.setCancelable(false);
                    builder.setIcon(R.mipmap.ic_launcher);
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
                        connectionStatus.setText("");
                        connectionStatus.setVisibility(View.INVISIBLE);
                        connectionStatus.setVisibility(View.GONE);
                    new testConnection().execute();
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.browse);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE);
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
                TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
                textView.setVisibility(View.INVISIBLE);
                View snackView = getLayoutInflater().inflate(R.layout.browsefile, null);
                layout.addView(snackView, 0);
                snackbar.show();

                TextView fab = (TextView) snackView.findViewById(R.id.btnChoose);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(
                                UploadNews.this,new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE
                                },REQUEST_CODE_GALLERY
                        );
                    }
                });

                TextView fab1 = (TextView) snackView.findViewById(R.id.btnCamera);
                fab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CODE_CAMERA);
                    }
                });

                btnClose = (TextView) snackView.findViewById(R.id.btnClose);
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });

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

        //for add new menu
        final FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab1.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(fab1);
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
               /** intent = new Intent (UploadNews.this,UploadNews.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();**/
                break;
            case R.id.action_upnews:

                break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
    }
private int genID(){
    final int min = 298673322;
    final int max = 808989797;
    Random random = new Random();
    int newsID = random.nextInt((max - min) + 1) + min;
    return newsID;
}
    private void captureImage() {
        String imagename = "urPics.jpg";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + imagename);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        FileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileUri);
        // start the image capture Intent
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }
    //convert image to byte
    private byte[] imageViewToByte(ImageView image){
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE_GALLERY);
            }else{
                Toast.makeText(getApplicationContext(),"You don't have permission to acces Gallery",Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if(requestCode == REQUEST_CODE_CAMERA){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                captureImage();
            }else{
                Toast.makeText(getApplicationContext(),"You don't have permission to acces Camera ",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    ///

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //when browse to gallery
        snackbar.dismiss();
        Bitmap photo = null;
        // photo = (Bitmap) data.getExtras().get("data");
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data !=null){
            Uri uri = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                myPics.setImageBitmap(bitmap);
               // profile_pic.setImageBitmap(bitmap);
                imageSelected = "Yes";
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageBytes = baos.toByteArray();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //when use device camera

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data !=null) {
            photo = (Bitmap) data.getExtras().get("data");
            myPics.setImageBitmap(photo);
           // profile_pic.setImageBitmap(photo);
            imageSelected = "Yes";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                pd.show();
                newsID = String.valueOf(genID());
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
                        if(response.equals("Record Saved")){

                            SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss");
                            String date_R = df.format(Calendar.getInstance().getTime());


                            sqLiteHelper.insertNewsData(email,newsID,"File",newsDetailsP,descriptionP,date_R,imageBytes);

                            Toast.makeText(UploadNews.this,"Your News Was Successfully Uploaded to Server !!",Toast.LENGTH_LONG).show();
                            //move to home Screen

                            Intent intent = new Intent(UploadNews.this, blogView.class);
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
                String encoded_string  = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                params.put("opr", "UploadNews");
                params.put("newsDetails", newsDetailsP);
                params.put("newsDesc", descriptionP);
                params.put("newsID", newsID);
                params.put("newsType", "File");
                params.put("newsEmail", email);
                params.put("encoded_string", encoded_string);

                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }


    public void getInitials(){
        saveRecord = (Button) findViewById(R.id.btnSave);
        description = (EditText) findViewById(R.id.desc);
        newsDetails = (EditText) findViewById(R.id.news);
        connectionStatus   = (TextView)findViewById(R.id.status);
        myPics = (ImageView) findViewById(R.id.my_pics);
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
                        intent = new Intent (UploadNews.this,blogView.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent (UploadNews.this,Profileview.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent (UploadNews.this,ChangePics.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();
                        intent = new Intent (UploadNews.this,ChangePassword.class);
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
                        // view_Account();
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

                Dialog d = new Dialog(UploadNews.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCanceledOnTouchOutside(true);
                //  d.setTitle(fullname);
                d.setContentView(snackView);
                d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                d.show();
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (UploadNews.this,ChangePics.class);
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
                        Intent intent = new Intent (UploadNews.this,ChangePassword.class);
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
                        Intent intent = new Intent (UploadNews.this,Profileview.class);
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
        Intent intent = new Intent(UploadNews.this, NewsCategory.class);
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
            Intent intent = new Intent(UploadNews.this, blogView.class);
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
                Intent intent = new Intent(UploadNews.this, HomeScreen.class);
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
                intent = new Intent (UploadNews.this,blogView.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent (UploadNews.this,Profileview.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent (UploadNews.this,ChangePics.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
                intent = new Intent (UploadNews.this,ChangePassword.class);
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
                // view_Account();
                break;
            case R.id.action_upvideo:
                drawerLayout.closeDrawers();
                // view_Account();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
