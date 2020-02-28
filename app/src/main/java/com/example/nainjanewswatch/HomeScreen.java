package com.example.nainjanewswatch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HomeScreen extends AppCompatActivity {
    TextView txtNew, txtAccount,txtLogin,txtExit;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        builder = new AlertDialog.Builder(this);

        txtNew = (TextView) findViewById(R.id.txtNew);
        txtLogin = (TextView) findViewById(R.id.txtLogin);
        txtExit = (TextView) findViewById(R.id.txtExit);

        //  Typeface font = Typeface.createFromAsset(getAssets(), "values/DUE.ttf");
        txtNew.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new_user();
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                user_login();
            }
        });
        txtExit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                verify_close();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blog_view, menu);
        return true;
    }
    public void onBackPressed() {
        verify_close();
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
        Intent intent = new Intent (HomeScreen.this,CreateAccount.class);
        startActivity(intent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        // finish();
    }
    public void user_login(){
       Intent intent = new Intent (HomeScreen.this,LoginScreen.class);
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
            user_login();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
