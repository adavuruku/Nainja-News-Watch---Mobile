package com.example.nainjanewswatch;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginScreen extends AppCompatActivity {
    Button saveRecord;
    EditText userName, password;
    TextView connectionStatus;

    SQLiteHelper sqLiteHelper;
    String userPassword,user_name;
    String allResult = null;
    ProgressDialog pd;
    AlertDialog.Builder builder;
    URLConnection urlconnection;
    URL url;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";
    public String MyName,MyEmail,MyState,MyGender,MyPermAdd,MyPassword,MyPhone,MyPics;
    public byte[] byteArray=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        sqLiteHelper = new SQLiteHelper(this,null,null,1);
        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Processing Login ...");
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        builder = new AlertDialog.Builder(this);
        getInitials();
        //login users

        saveRecord.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                user_name = userName.getText().toString();
                userPassword = password.getText().toString();
                //verify if all details are provided
                if(user_name.isEmpty() || userPassword.isEmpty()){
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
                }
                else
                {
                    new testConnection().execute();
                }
            }
        });
    }
    public void getInitials(){
        saveRecord = (Button) findViewById(R.id.btnSave);
        userName = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        connectionStatus   = (TextView)findViewById(R.id.status);
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
                connectionStatus.setText("No Internet Connection !!");
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

                        try {
                            JSONObject jsonobject = new JSONObject(response);
                            String error = jsonobject.getString("Error");
                            if(error.equals("Error: Wrong Username Or Password !!!")){
                                connectionStatus.setVisibility(View.VISIBLE);
                                connectionStatus.setText(error);
                            }else{
                                allResult = response;
                                connectionStatus.setVisibility(View.INVISIBLE);
                                connectionStatus.setVisibility(View.GONE);
                               // connectionStatus.setText(error);
                                new ReadJSON().execute();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            connectionStatus.setVisibility(View.VISIBLE);
                            connectionStatus.setText(e.getMessage());
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
                params.put("opr", "login");
                params.put("email", user_name);
                params.put("password", userPassword);
                return params;
            }
        };
        AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
    }
    class ReadJSON extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {

            try {
                    JSONObject jsonobject = new JSONObject(allResult);
                    MyName = jsonobject.getString("MyName");
                    MyEmail = jsonobject.getString("MyEmail");
                    MyState = jsonobject.getString("MyState");
                    MyGender = jsonobject.getString("MyGender");
                    MyPermAdd = jsonobject.getString("MyPermAdd");
                    MyPassword = jsonobject.getString("MyPassword");
                    MyPhone = jsonobject.getString("MyPhone");
                    MyPics = jsonobject.getString("MyPics");
                    Bitmap bitmap =  Picasso.with(getApplicationContext()).load(MyPics).get();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byteArray = stream.toByteArray();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                //Bitmap bitmap= BitmapFactory.decodeStream((InputStream)new URL("http://www.thecrazyprogrammer.com/wp-content/uploads/2015/07/The-Crazy-Programmer.png").getContent());
                //   Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(MyPics).getContent());

                // Picasso.with(context).load(MyPics).into(holder.iv);

                sqLiteHelper.insertData(
                        MyName, MyEmail, MyPhone, MyState, MyPermAdd, MyPassword, MyGender, byteArray
                );
                Toast.makeText(LoginScreen.this,"Welcome To Nainja News Watch !!",Toast.LENGTH_LONG).show();
                //move to home Screen

                Intent intent = new Intent(LoginScreen.this, blogView.class);
                intent.putExtra("FULL_NAME", MyName);
                intent.putExtra("USERNAME", MyEmail);
                intent.putExtra("PROF_PICS", byteArray);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText(e.getMessage()+MyPics+" picture load error");
            }


            super.onPostExecute(s);
        }
    }
    public void saveAll(String comingNews) {

    }

    //menus continue
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blog_view, menu);
        return true;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent (LoginScreen.this,HomeScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }
    public void verify_close(){

        builder.setMessage("Do You Really Want to Exit Naija News Watch ? ");
        builder.setTitle("Nainja News Watch");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // dialogInterface.cancel();
                System.exit(0);
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
    public void new_user(){
        Intent intent = new Intent (LoginScreen.this,CreateAccount.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }
    public void user_login(){
        Intent intent = new Intent (LoginScreen.this,HomeScreen.class);
         startActivity(intent);
         overridePendingTransition(R.anim.right_in, R.anim.left_out);
        //finish();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            new_user();
            return true;
        }
        if (id == R.id.action_exit) {
            verify_close();
            return true;
        }
        if (id == R.id.action_login) {
           // user_login();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
