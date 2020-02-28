package com.example.nainjanewswatch;

/**
 * Created by sherif146 on 06/04/2017.
 */
public class Product {

        private String news_head;
        private String pics_path;
        private String author;
        private String date_two;
        private String news_id;
        private String body_two;
        private String body_two_2;

        public Product(String news_head, String pics_path, String author, String date_two, String news_id, String body_two,String body_two_2){
            this.news_head = news_head;
            this.pics_path = pics_path;
            this.author = author;
            this.date_two = date_two;
            this.news_id = news_id;
            this.body_two = body_two;
            this.body_two_2 = body_two_2;
        }

        public String getNews_head(){
            return news_head;
        }
        public void setNews_head(String news_head){
            this.news_head = news_head;
        }

        public String getPics_path(){
            return pics_path;
        }
        public void setPics_path(String pics_path){
            this.pics_path = pics_path;
        }

        public String getAuthor(){
            return author;
        }
        public void setAuthor(String author){
            this.author = author;
        }

        public String getNews_id (){
            return news_id;
        }
        public void setNews_id_two (String news_id ){
            this.news_id  = news_id;
        }

        public String getBody_two (){
            return body_two;
        }
        public void setBody_two (String body_two ){
            this.body_two  = body_two;
        }

        public String getBody_two_2 (){
            return body_two_2;
        }
        public void setBody_two_2 (String body_two_2 ){
        this.body_two_2  = body_two_2;
    }

        public String getDate_two (){
            return date_two;
        }
        public void setDate_two (String date_two ){
            this.date_two  = date_two;
        }

    }
