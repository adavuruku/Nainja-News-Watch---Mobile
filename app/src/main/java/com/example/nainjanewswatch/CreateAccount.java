package com.example.nainjanewswatch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccount extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    TextView browse, useCamera,btnClose,status;
    Button saveRecord;
    EditText fullName, userName, password,phone,permAdd,retypepassword;
    ImageView profilePic;
    Spinner states;
    SQLiteHelper sqLiteHelper;
    String imageSelected = "No";

    String fullname,username,gender,userpermAdd,userphone,retypeP,userPassword,userState;
    byte [] imagesPics,imageBytes;
    Bitmap photo;
    ProgressDialog pd;
    AlertDialog.Builder builder;
    Uri FileUri;
    final int REQUEST_CODE_GALLERY=999;
    final int REQUEST_CODE_CAMERA=777;
    public static final int MEDIA_TYPE_IMAGE = 1;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";
    URLConnection urlconnection;
    URL url;
    Snackbar snackbar;
    RadioButton male, female;
    String [] allStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        pd = new ProgressDialog(this);

        builder = new AlertDialog.Builder(this);
        getInitials();
        sqLiteHelper = new SQLiteHelper(this,null,null,1);


        allStates = getResources().getStringArray(R.array.country_arrays);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,allStates);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        states.setAdapter(dataAdapter);

        states.setOnItemSelectedListener(this);

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
                                CreateAccount.this,new String[]{
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
        male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                female.setChecked(false);
            }
        });
        female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                male.setChecked(false);
            }
        });
        //save record
        saveRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //check for empty boxes
                String fullname = fullName.getText().toString();
                String username = userName.getText().toString(); //email id

                String userPassword = password.getText().toString();
                String retypeP = retypepassword.getText().toString();

                String userphone = phone.getText().toString();
                String userpermAdd = permAdd.getText().toString();

                if(male.isChecked()) gender = "Male";
                if(female.isChecked()) gender = "Female";

                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage("Processing Details ...");
                pd.setIndeterminate(true);
                pd.setCancelable(true);
                pd.show();

                //verify if all details are provided
                if(fullname.isEmpty() || username.isEmpty() || userPassword.isEmpty() || imageSelected.equals("No")
                        || userphone.isEmpty() || userpermAdd.isEmpty() || userState.isEmpty() || gender.isEmpty() || userState.equals("-select state-") || !retypeP.equals(userPassword)){
                    //dont save
                    pd.hide();
                    builder.setMessage("Error: Some Field are Empty !");
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
                    status.setVisibility(View.VISIBLE);
                    status.setText("Error: Empty Data Provided !!");
                }else{
                    //save
                    new testConnection().execute();
                    status.setVisibility(View.GONE);
                }
            }
        });
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
                status.setText("");
                status.setVisibility(View.INVISIBLE);
                status.setVisibility(View.GONE);

                //new ReadJSON().execute();
                volleyJsonArrayRequest(address);
            }
            else{
                pd.hide();
                status.setVisibility(View.VISIBLE);
                status.setText("No Internet Connection - Fail To Save!!");
            }
        }
    }
//save record
    public void volleyJsonArrayRequest(String url){
        String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        pd.hide();
                        try {
                            if(response.equals("Record Saved")) {
                                sqLiteHelper.insertData(
                                        fullname, username, userphone, userState, userpermAdd, userPassword, gender, imagesPics
                                );
                                Toast.makeText(CreateAccount.this,"Account Saved Successfully !!",Toast.LENGTH_LONG).show();
                                //move to home Screen

                                Intent intent = new Intent(CreateAccount.this, blogView.class);
                                intent.putExtra("FULL_NAME", fullname);
                                intent.putExtra("USERNAME", username);
                                intent.putExtra("PROF_PICS", imagesPics);
                                startActivity(intent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                finish();
                            }else{
                                status.setVisibility(View.VISIBLE);
                                status.setText(response);
                                builder.setMessage(response);
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

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.hide();
                        status.setVisibility(View.VISIBLE);
                        status.setText(error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();

                //get values
                fullname = fullName.getText().toString();
                username = userName.getText().toString(); //email id
                userPassword = password.getText().toString();
                userphone = phone.getText().toString();
                userpermAdd = permAdd.getText().toString();
                imagesPics = imageViewToByte(profilePic);
                //String encoded_string = Base64.encodeToString(imageBytes,0);
                String encoded_string  = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                params.put("opr", "new");
                params.put("fullname", fullname);
                params.put("username", username);
                params.put("gender", gender);
                params.put("userpermAdd", userpermAdd);
                params.put("userphone", userphone);

                params.put("userState", userState);
                params.put("userPassword", userPassword);
                params.put("encoded_string", encoded_string);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }
// ends saving

    //when the state is change
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        userState = parent.getItemAtPosition(position).toString();
        // Showing selected spinner item
       // Toast.makeText(parent.getContext(), "Selected: " + userState, Toast.LENGTH_LONG).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    public void getInitials(){
        male = (RadioButton) findViewById(R.id.male);
        female = (RadioButton) findViewById(R.id.female);
        saveRecord = (Button) findViewById(R.id.btnSave);
        fullName = (EditText) findViewById(R.id.fullname);
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        status = (TextView) findViewById(R.id.status);
        states = (Spinner) findViewById(R.id.spinner);
        phone = (EditText) findViewById(R.id.phone);
        permAdd = (EditText) findViewById(R.id.permAdd);
        retypepassword = (EditText) findViewById(R.id.retypepassword);
        profilePic = (ImageView) findViewById(R.id.profile_pic);
    }
    //check if device use camera
    private boolean isDeviceSupportCamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    //capturing the image with camera
    /*
 * Capturing Camera Image will lauch camera app requrest image capture
 */

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //when browse to gallery
        snackbar.dismiss();

       // photo = (Bitmap) data.getExtras().get("data");
        if(requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data !=null){
            Uri uri = data.getData();
            try{
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                photo = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                profilePic.setImageBitmap(bitmap);
                imageSelected = "Yes";
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                imageBytes = baos.toByteArray();
               // Toast.makeText(getApplicationContext(),"Image Selected !!",Toast.LENGTH_LONG).show();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //when use device camera

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK && data !=null) {
            photo = (Bitmap) data.getExtras().get("data");
            profilePic.setImageBitmap(photo);
            imageSelected = "Yes";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();
           // Toast.makeText(getApplicationContext(),"Image Selected !!",Toast.LENGTH_LONG).show();
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    // menu issued
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blog_view, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (CreateAccount.this,HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    public void verify_close(){

        builder.setMessage("Do You Really Want to Exit Naija News Watch ? ");
        builder.setTitle("Nainja News Watch");
        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dialogInterface.cancel();
                onBackPressed();
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
    public void new_user(){
        Intent intent = new Intent (CreateAccount.this,CreateAccount.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        // finish();
    }
    public void user_login(){
     Intent intent = new Intent (CreateAccount.this,LoginScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
          ///  new_user();
            return true;
        }
        if (id == R.id.action_exit) {
            verify_close();
            return true;
        }
        if (id == R.id.action_login) {
            user_login();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


    //saving record
/**class ReadJSON extends AsyncTask<String, Integer, String> {
 String result;
 String data = null;

 protected String doInBackground(String... params) {
 String opr = "new";
 try {
 String encoded_string = Base64.encodeToString(imageBytes,0);
 // encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
 data = URLEncoder.encode("opr", "UTF-8")
 + "=" + URLEncoder.encode(opr, "UTF-8");

 data += "&" + URLEncoder.encode("fullname", "UTF-8") + "="
 + URLEncoder.encode(fullname, "UTF-8");

 data += "&" + URLEncoder.encode("username", "UTF-8") + "="
 + URLEncoder.encode(username, "UTF-8");

 data += "&" + URLEncoder.encode("gender", "UTF-8") + "="
 + URLEncoder.encode(gender, "UTF-8");

 data += "&" + URLEncoder.encode("userpermAdd", "UTF-8") + "="
 + URLEncoder.encode(userpermAdd, "UTF-8");

 data += "&" + URLEncoder.encode("userphone", "UTF-8") + "="
 + URLEncoder.encode(userphone, "UTF-8");

 data += "&" + URLEncoder.encode("userState", "UTF-8") + "="
 + URLEncoder.encode(userState, "UTF-8");

 data += "&" + URLEncoder.encode("userPassword", "UTF-8") + "="
 + URLEncoder.encode(userPassword, "UTF-8");

 data += "&" + URLEncoder.encode("encoded_string", "UTF-8") + "="
 + URLEncoder.encode(encoded_string, "UTF-8");

 } catch (UnsupportedEncodingException e) {
 e.printStackTrace();
 }

 try{
 StringBuilder context = new StringBuilder();
 URL url = new URL(address);
 URLConnection urlconnection = url.openConnection();
 urlconnection.setDoOutput(true);
 OutputStreamWriter wr = new OutputStreamWriter(urlconnection.getOutputStream());
 wr.write(data);
 wr.flush();

 //get server response
 BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(urlconnection.getInputStream()));
 String line=null;
 //readURL
 while((line = bufferedreader.readLine())!=null){
 context.append(line +"\n");
 }
 bufferedreader.close();
 result = context.toString();
 }catch (Exception e) {
 e.printStackTrace();
 }

 if(!result.equals("Record Saved")) {
 sqLiteHelper.insertData(
 fullname, username, userphone, userState, userpermAdd, userPassword, gender, imagesPics
 );
 }
 return result;
 }
 @Override
 protected void onPostExecute(String content) {
 pd.hide();

 try {
 if(!content.equals("Record Saved")) {
 /**  sqLiteHelper.insertData(
 fullname,username,userphone,userState,userpermAdd,userPassword,gender,imagesPics
 );
    Toast.makeText(CreateAccount.this,"Account Saved Successfully !!",Toast.LENGTH_LONG).show();
    //move to home Screen

    Intent intent = new Intent(CreateAccount.this, blogView.class);
    intent.putExtra("FULL_NAME", fullname);
    intent.putExtra("USERNAME", username);
    intent.putExtra("PROF_PICS", imagesPics);
    startActivity(intent);
    overridePendingTransition(R.anim.right_in, R.anim.left_out);
    finish();
}else{
        status.setVisibility(View.VISIBLE);
        // status.setText("Error: Unable to Save Record Retry !");
        status.setText(content);
        builder.setMessage(content);
        builder.setTitle("Nainja News Watch");
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

        }
        } catch (Exception e) {
        e.printStackTrace();
        }
        }

@Override
protected void onPreExecute() {
        fullname = fullName.getText().toString();
        username = userName.getText().toString(); //email id
        userPassword = password.getText().toString();
        userphone = phone.getText().toString();
        userpermAdd = permAdd.getText().toString();
        imagesPics = imageViewToByte(profilePic);

        super.onPreExecute();
        }
        }**/
}

