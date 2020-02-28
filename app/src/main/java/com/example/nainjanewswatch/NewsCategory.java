package com.example.nainjanewswatch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewsCategory extends AppCompatActivity implements AdapterView.OnItemClickListener{
    ListView lv;
    ArrayList<Product> arraylist;
    ProgressDialog pd;
    private static int SPLASH_TIME_OUT = 1000;//5seconds
    TextView connectionStatus;
    String search_query=null;

    URLConnection urlconnection;
    URL url;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";
    byte[] image_data;
    String fullname,email;
    private boolean isConnected = false;
    public NetworkChangeReceiver receiver;
    AlertDialog.Builder builder;
    public String categoryData;
    SharedPreferences categoryNews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_category);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        builder = new AlertDialog.Builder(this);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      //  CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapse_toolbar);
       // collapsingToolbar.setTitle(getString(R.string.app_name));

        Intent intent = getIntent();
        search_query = intent.getStringExtra("EXTRA_SEARCH");

        fullname = intent.getStringExtra("FULL_NAME");
        email = intent.getStringExtra("USERNAME");
        image_data = intent.getByteArrayExtra("PROF_PICS");

       // search_query="Politics";
        lv = (ListView) findViewById(R.id.list);
        connectionStatus = (TextView) findViewById(R.id.status);
       // arraylist = new ArrayList<>();
        lv.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        lv.setFastScrollEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new testConnection().execute();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        //for add new menu
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.performLongClick();
            }
        });
        //register for contextmenu
        registerForContextMenu(fab);


        pd = new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Fetching News ...");
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        // pd.show();

        categoryNews = this.getSharedPreferences("categoryNews", Context.MODE_PRIVATE);
        //retrieve content saved in preference - politicsData
        categoryData = categoryNews.getString("categoryData", "");
        if(categoryData != ""){
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatus.setText("Refresh For Latest "+ search_query+" News !!");
            //load into the view
            loadValues(categoryData);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new testConnection().execute();
                }
            },SPLASH_TIME_OUT);
        }
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

       initNavigationDrawer();

    }

    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            isNetworkAvailable(context);
        }
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
                intent = new Intent (NewsCategory.this,UploadNews.class);
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
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        if(!isConnected){
                            isConnected = true;
                            new testConnection().execute();
                        }
                        return true;
                    }
                }
            }
        }
        isConnected = false;
        return false;
    }
    public void open_news(String newsCategory){
        search_query = newsCategory;
        new testConnection().execute();
        //overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
                        intent = new Intent (NewsCategory.this,blogView.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_profile:
                        drawerLayout.closeDrawers();
                        intent = new Intent (NewsCategory.this,Profileview.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pics:
                        drawerLayout.closeDrawers();
                        intent = new Intent (NewsCategory.this,ChangePics.class);
                        intent.putExtra("FULL_NAME", fullname);
                        intent.putExtra("USERNAME", email);
                        intent.putExtra("PROF_PICS", image_data);
                        startActivity(intent);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        finish();
                        break;
                    case R.id.action_pas:
                        drawerLayout.closeDrawers();
                        intent = new Intent (NewsCategory.this,ChangePassword.class);
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
                        intent = new Intent (NewsCategory.this,UploadNews.class);
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

                Dialog d = new Dialog(NewsCategory.this);
                d.requestWindowFeature(Window.FEATURE_NO_TITLE);
                d.setCanceledOnTouchOutside(true);
                //  d.setTitle(fullname);
                d.setContentView(snackView);
                d.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
                d.show();
                btnPics.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent (NewsCategory.this,ChangePics.class);
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
                        Intent intent = new Intent (NewsCategory.this,ChangePassword.class);
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
                        Intent intent = new Intent (NewsCategory.this,Profileview.class);
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
                intent = new Intent (NewsCategory.this,blogView.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_profile:
                drawerLayout.closeDrawers();
                intent = new Intent (NewsCategory.this,Profileview.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pics:
                drawerLayout.closeDrawers();
                intent = new Intent (NewsCategory.this,ChangePics.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);
                startActivity(intent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
                break;
            case R.id.action_pas:
                drawerLayout.closeDrawers();
                intent = new Intent (NewsCategory.this,ChangePassword.class);
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
                intent = new Intent (NewsCategory.this,UploadNews.class);
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
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //String new_id_name = arraylist.get(position).getNews_id();
        String pics_pat = arraylist.get(position).getPics_path();
        String author_w = arraylist.get(position).getAuthor();
        String new_info = arraylist.get(position).getBody_two_2();
        String new_date = arraylist.get(position).getDate_two();
        String new_title = arraylist.get(position).getNews_head();

        // Toast.makeText(getActivity(), "" + new_title, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(NewsCategory.this, ReadNews.class);
        intent.putExtra("EXTRA_MESSAGE", pics_pat);
        intent.putExtra("EXTRA_MESSAGE_INFO", new_info);
        intent.putExtra("EXTRA_MESSAGE_AUTHOR", author_w);
        intent.putExtra("EXTRA_MESSAGE_DATE", new_date);
        intent.putExtra("EXTRA_MESSAGE_TITLE", new_title);
        intent.putExtra("EXTRA_SEARCH", search_query);
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
                Intent intent = new Intent(NewsCategory.this, blogView.class);
                intent.putExtra("FULL_NAME", fullname);
                intent.putExtra("USERNAME", email);
                intent.putExtra("PROF_PICS", image_data);

                startActivity(intent);
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                finish();
        }
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
                if (outre.equals("true")) {
                    connectionStatus.setText("");
                    connectionStatus.setVisibility(View.INVISIBLE);
                    connectionStatus.setVisibility(View.GONE);
                    //new ReadJSON().execute();
                    pd.show();
                    volleyJsonArrayRequest(address);
                } else {
                    //mSwipeRefreshLayout.setRefreshing(false);
                    pd.hide();
                    connectionStatus.setVisibility(View.VISIBLE);
                    connectionStatus.setText("No Internet Connection !!");
                    if (categoryData != "") {
                        connectionStatus.setVisibility(View.VISIBLE);
                        connectionStatus.setText("Refresh For Latest " + search_query + " News !!");
                        //load into the view
                        loadValues(categoryData);
                    }
                }
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
                Intent intent = new Intent(NewsCategory.this, HomeScreen.class);
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
        public void volleyJsonArrayRequest(String url){
            String  REQUEST_TAG = "com.volley.volleyJsonArrayRequest";
            StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>()
                    {
                        @Override
                        public void onResponse(String response) {
                            pd.hide();
                            loadValues(response);
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
                    params.put("opr", "search");
                    params.put("searchQuery", search_query);
                    return params;
                }
            };
            AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(postRequest, REQUEST_TAG);
        }


  /**  class ReadJSON extends AsyncTask<String, Integer, String> {
        String result="sherif";
        String data = null;

        protected String doInBackground(String... params) {
            String opr = "search";
            try {
                data = URLEncoder.encode("opr", "UTF-8")
                        + "=" + URLEncoder.encode(opr, "UTF-8");
                data += "&" + URLEncoder.encode("searchQuery", "UTF-8") + "="
                        + URLEncoder.encode(search_query, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }


            //return readURL(address);
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
            return result;
        }
        @Override
        protected void onPostExecute(String content) {
            pd.hide();
            if(result != null) {
                loadValues(result);
            }else{
                connectionStatus.setVisibility(View.VISIBLE);
                connectionStatus.setText("No News Availlable For " +search_query);
                if(categoryData != ""){
                    connectionStatus.setVisibility(View.VISIBLE);
                    connectionStatus.setText("Refresh For Latest "+ search_query+" News !!");
                    //load into the view
                    loadValues(categoryData);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            pd.show();
            super.onPreExecute();
        }
    }**/

    public void loadValues(String comingNews) {
        try {
            arraylist = new ArrayList<>();
            JSONObject jsonobject = null;
            JSONArray jsonarray = null;
            jsonarray = new JSONArray(comingNews);
            for (int i = 0; i < jsonarray.length(); i++) {
                jsonobject = jsonarray.getJSONObject(i);
                arraylist.add(new Product(
                        jsonobject.getString("news_head"),
                        jsonobject.getString("pics_path"),
                        jsonobject.getString("author"),
                        jsonobject.getString("date_two"),
                        jsonobject.getString("news_id"),
                        jsonobject.getString("body_two"),
                        jsonobject.getString("full_info")
                ));
            }
            //  bl.politiclist = arraylist;
            SharedPreferences.Editor editor = categoryNews.edit();
            //update content saved in preference - politicsData
            editor.putString("categoryData", comingNews);
            editor.commit();
            CustomListAdapter adapter = new CustomListAdapter(NewsCategory.this, R.layout.custom_list_layout, arraylist);
            lv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
