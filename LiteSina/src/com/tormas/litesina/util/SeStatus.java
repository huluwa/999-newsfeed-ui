package com.tormas.litesina.util;

import java.util.Date;

import twitter4j.RetweetDetails;

public class SeStatus implements java.io.Serializable{
    public Date createdAt;
    public long id;
    public String text;
    public boolean isFavorited;
    public boolean ismytweets;
    public boolean selected;
    public SeSimplyUser user;
    
    public String thumbnail_pic;
    public String bmiddle_pic;
    public String original_pic;
    public SeRetweetDetails retweetDetails;
    
    
    public int commentsCount;
    public int rtCount;
}
