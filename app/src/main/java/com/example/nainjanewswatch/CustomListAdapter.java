package com.example.nainjanewswatch;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nainjanewswatch.Product;
//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by sherif146 on 06/04/2017.
 */
public class CustomListAdapter extends ArrayAdapter<Product> {
    ArrayList<Product> products;
    Context context;
    int resource;
    public CustomListAdapter(Context context, int resource, ArrayList<Product> products){
        super(context,resource,products);
        this.products = products;
        this.context = context;
        this.resource =resource;
    }
    private class ViewHolder{
        ImageView iv;
        TextView author;
        TextView title;
        TextView date;
    }
    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.custom_list_layout, null, true);

            Product product = getItem(position);
            holder = new ViewHolder();
            //get control instances
            holder.iv = (ImageView) convertView.findViewById(R.id.imageViewProduct);
            holder.author = (TextView) convertView.findViewById(R.id.newsAuthor);
            holder.title = (TextView) convertView.findViewById(R.id.newstitle);
            holder.date = (TextView) convertView.findViewById(R.id.newsDate);
            // TextView info = (TextView) convertView.findViewById(R.id.newsInfo);
            //WebView wv =(WebView) convertView.findViewById(R.id.webView);

            //set control values
            String htmlText = "<p alighn=\"justify\">";
            holder.author.setText(product.getAuthor());
            holder.title.setText(product.getNews_head());
            holder.date.setText(product.getDate_two());
            // info.setText(product.getBody_two());
            // wv.loadData(String.format(htmlText,product.getBody_two()),"text/html","utf8");
           // Picasso.with(context).load(product.getPics_path()).into(holder.iv);
            Glide.with(context).load(product.getPics_path()).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).thumbnail(0.5f).into(holder.iv);
        }
        return convertView;
    }
}
