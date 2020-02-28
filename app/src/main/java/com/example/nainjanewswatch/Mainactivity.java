package com.example.nainjanewswatch;

/**
 * Created by sherif146 on 10/06/2017.
 */
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class Mainactivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 3000;//5seconds
    TextView tvLabel,textView,txt3;
    SQLiteHelper sqLiteHelper;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLiteHelper = new SQLiteHelper(this,null,null,1);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new ReadJSON().execute();
            }
        }, SPLASH_TIME_OUT);

        textView = (TextView) findViewById(R.id.textView);
        txt3 = (TextView) findViewById(R.id.textView2);
        img = (ImageView) findViewById(R.id.imageView);
        Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in);
        // start the animation
        img.startAnimation(animFadeOut);
        textView.startAnimation(animFadeOut);
        txt3.startAnimation(animFadeOut);
    }

    public void clearAnim(){
        try {

            //Inflate animation from XML
            Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_anim);
            // Setup listeners (optional)
            animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Fires when animation starts
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //call the home screen
                    Animation animFadeOut1 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out);
                    txt3.setVisibility(View.INVISIBLE);textView.setVisibility(View.INVISIBLE);img.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // ...
                }
            });
            // start the animation
            img.startAnimation(animFadeOut);
            textView.startAnimation(animFadeOut);
            txt3.startAnimation(animFadeOut);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ReadJSON extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... params) {

            //return readURL(address);
            try {

                //verify if a user still has an account
                Cursor cursor = sqLiteHelper.searchUser();
                if(cursor.getCount() <= 0 ) {
                    clearAnim();
                    Intent intent = new Intent (Mainactivity.this,HomeScreen.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    finish();
                }else{

                    cursor.moveToFirst();

                    String  FullName = cursor.getString(1);
                    String email = cursor.getString(2);
                    byte[] imageP = cursor.getBlob(8);

                    // Toast.makeText(getApplicationContext(), "Login Successfully !!", Toast.LENGTH_LONG).show();
                    //move to home Screen
                    clearAnim();
                    Intent intent = new Intent(Mainactivity.this, blogView.class);
                    intent.putExtra("FULL_NAME", FullName);
                    intent.putExtra("USERNAME", email);
                    intent.putExtra("PROF_PICS", imageP);
                    startActivity(intent);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
