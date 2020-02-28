package com.example.nainjanewswatch;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

/**
 * Created by sherif146 on 04/06/2017.
 */
public class sport extends Fragment implements AdapterView.OnItemClickListener{
    ListView lv;
    ArrayList<Product> arraylist;
    ProgressDialog pd;
    private static int SPLASH_TIME_OUT = 1000;//5seconds

    byte[] image_data;
    String fullname,email;
    TextView connectionStatus;
    String address = "http://192.168.230.1/NainjaNewsWatch/listViewAndroid.php";
    URLConnection urlconnection;
    SwipeRefreshLayout mSwipeRefreshLayout;
    URL url;
    blogView bl;
    SharedPreferences sportsNews;
    public String mmyhost = "sport";
    private boolean isConnected = false;
    public NetworkChangeReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sport, container, false);
        lv = (ListView) rootView.findViewById(R.id.list);
        lv.setFastScrollEnabled(true);
        connectionStatus = (TextView) rootView.findViewById(R.id.status);

        arraylist = new ArrayList<>();
        lv.setOnItemClickListener((AdapterView.OnItemClickListener) this);
        lv.setFastScrollEnabled(true);

         // bl = new blogView();
        bl = (blogView) getActivity();
        bl.mmyhost ="sport";
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                    new testConnection().execute();
                    mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        pd = new ProgressDialog(getActivity());
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Fetching News ...");
        pd.setIndeterminate(true);
        pd.setCancelable(true);

        Intent intent = getActivity().getIntent();
        fullname = intent.getStringExtra("FULL_NAME");
        email = intent.getStringExtra("USERNAME");
        image_data = intent.getByteArrayExtra("PROF_PICS");

      /**  if(bl.sportlist != null){
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatus.setText("Refresh For Latest Sport News !!");
            CustomListAdapter adapter = new CustomListAdapter(getActivity(), R.layout.custom_list_layout, bl.sportlist);
            lv.setAdapter(adapter);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new testConnection().execute();
                }
            },SPLASH_TIME_OUT);
        }**/

        sportsNews = this.getActivity().getSharedPreferences("sportsNews", Context.MODE_PRIVATE);
        //retrieve content saved in preference - politicsData
        String sportsData = sportsNews.getString("sportsData", "");
        if(sportsData != ""){
            connectionStatus.setVisibility(View.VISIBLE);
            connectionStatus.setText("Refresh for Latest Sport News !!");
            //load into the view
            loadValues(sportsData);
        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    new testConnection().execute();
                }
            },SPLASH_TIME_OUT);
        }
        return rootView;
    }

    @Override
    public void onResume(){
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        getActivity().registerReceiver(receiver, filter);
        super.onResume();
    }
    @Override
    public void onPause(){
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }
    //verify once networ is change - wifi or sim data
    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            isNetworkAvailable(context);
        }
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
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //String new_id_name = arraylist.get(position).getNews_id();
        String pics_pat = arraylist.get(position).getPics_path();
        String author_w = arraylist.get(position).getAuthor();
        String new_info = arraylist.get(position).getBody_two_2();
        String new_date = arraylist.get(position).getDate_two();
        String new_title = arraylist.get(position).getNews_head();

        Intent intent = new Intent(getActivity(), ReadNews.class);
         intent.putExtra("EXTRA_MESSAGE", pics_pat);
         intent.putExtra("EXTRA_MESSAGE_INFO", new_info);
         intent.putExtra("EXTRA_MESSAGE_AUTHOR", author_w);
         intent.putExtra("EXTRA_MESSAGE_DATE", new_date);
         intent.putExtra("EXTRA_MESSAGE_TITLE", new_title);
        intent.putExtra("FULL_NAME", fullname);
        intent.putExtra("USERNAME", email);
        intent.putExtra("PROF_PICS", image_data);
         startActivity(intent);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
                        loadValues( response);
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
                params.put("opr", "sports");
                return params;
            }
        };
        AppSingleton.getInstance(getActivity()).addToRequestQueue(postRequest, REQUEST_TAG);
    }
   /** class ReadJSON extends AsyncTask<String, Integer, String> {
        String result="sherif";
        String data = null;

        protected String doInBackground(String... params) {
            String opr = "sports";
            try {
                data = URLEncoder.encode("opr", "UTF-8")
                        + "=" + URLEncoder.encode(opr, "UTF-8");

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
                connectionStatus.setText("No News Availlable For Politics");
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
            SharedPreferences.Editor editor = sportsNews.edit();
            //update content saved in preference - politicsData
            editor.putString("sportsData", comingNews);
            editor.commit();
            CustomListAdapter adapter = new CustomListAdapter(getActivity(), R.layout.custom_list_layout, arraylist);
            lv.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
