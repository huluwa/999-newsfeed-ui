package com.tormas.litetwitter.ui.view;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.ui.TwitterHelper;
import com.tormas.litetwitter.ui.TwitterSearchActivity;
import com.tormas.litetwitter.ui.TwitterTweetsActivity;
import com.tormas.litetwitter.ui.TwitterTweetsDetailActivity;
import twitter4j.Status;
import twitter4j.Tweet;
import oms.sns.TwitterStatus;
import oms.sns.TwitterUser;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class StatusItemView extends SNSItemView {
	private final String TAG="StatusItemView";
	private ImageView imageView;
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private Status status;
	private Tweet  tweet;	
	String  imageURL;
	Handler handler;
	int type;//0, status, 1 tweet
	
	java.util.Random random = new java.util.Random();
	public StatusItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		
		mContext = ctx;
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);   
        
        handler = new Handler();
    }
	
	public StatusItemView(Context context, Status di) 
	{		
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call TweetItemView");
		type = 0;
		handler = new Handler();
		init();
	}
	
	public StatusItemView(Context context, Tweet di) 
	{		
		super(context);
		mContext = context;
		tweet = di;
		
		Log.d(TAG, "call TweetItemView");
		type = 1;
		handler = new Handler();
		init();
	}
	
	public String getTwitterID()
    {
        String ret = "";
        if(tweet != null)
            ret = tweet.getFromUser();
        else if(status != null)
            ret = status.getUser().getScreenName();
        
        return ret;
    }
	
	public long getStatusID()
	{
	    long ret = -1;
	    if(tweet != null)
	        ret = tweet.getId();
	    else if(status != null)
	        ret = status.getId();
	    
	    return ret;
	}
	public String getText()
    {
        String ret = "";
        if(tweet != null)
            ret = tweet.getText();
        else if(status != null)
            ret = status.getText();
        
        return ret;
    }
	public Object GetContent()
	{
	    Object ret = null;
        if(tweet != null)
            ret = tweet;
        else if(status != null)
            ret = status;
        
        return ret;
	}
	
	private void init() 
	{
		Log.d(TAG,  "call init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.twitter_tweets_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);
		publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);
		username     = (TextView)v.findViewById(R.id.tweet_user_name);
		
		if(type == 0 && status != null)
		{
			username.setText(status.getUser().getName());
			imageURL = status.getUser().getProfileImageURL().toString(); 
			
			ImageRun imagerun = new ImageRun(handler, imageURL, 0);
			imagerun.use_avatar = true;
			imagerun.addHostAndPath = true;
			imagerun.setImageView(imageView);
			imagerun.post(null);
			
			
			publishDate.setText(status.getCreatedAt().toLocaleString());			
			publishTxt.setText(status.getText());
		}
		else if(type ==1 && tweet != null)
		{
			username.setText(tweet.getFromUser());
			imageURL = tweet.getProfileImageUrl();
			
			ImageRun imagerun = new ImageRun(handler, imageURL, 0);
			imagerun.use_avatar = true;
			imagerun.setImageView(imageView);
			imagerun.post(null);
			
			
			publishDate.setText(tweet.getCreatedAt().toLocaleString());			
			publishTxt.setText(tweet.getText());
		}		
	}	
	
	public void setImage(String url)
	{
		
	}
	public void setContent(String publishContent, String publishDate, String username)
	{
		
	}

	public void chooseTweetsListener()
	{
		setOnClickListener(tweetOnClik);
	}
	
	View.OnClickListener tweetOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "tweetOnClik you click first one=");			 
			 //view details
			 Intent intent = new Intent(mContext, TwitterTweetsDetailActivity.class);
			 			 
			 if(type ==0 && status != null)
			 {
				 TwitterStatus twitem = new TwitterStatus();
		         twitem.createdAt = status.getCreatedAt().getTime();
		         twitem.id        = status.getId();
		         twitem.inReplyToStatusId = status.getInReplyToStatusId();
		         twitem.inReplyToUserId   = status.getInReplyToUserId();
		         twitem.isFavorited       = status.isFavorited();
		         twitem.isTruncated       = status.isTruncated();
		         twitem.source  = status.getSource();
		         twitem.text    = status.getText();
		         twitem.user = new TwitterUser();
		         twitem.user.id = status.getUser().getId();
		         twitem.user.description = status.getUser().getDescription();
		         twitem.user.followersCount = status.getUser().getFollowersCount();
		         twitem.user.isProtected    = status.getUser().isProtected();
		         twitem.user.location       = status.getUser().getLocation();
		         twitem.user.name           = status.getUser().getName();
		         twitem.user.profileImageUrl = status.getUser().getProfileImageURL().toString();
		         twitem.user.screenName      = status.getUser().getScreenName();
		         twitem.user.url             = "";
		         if(status.getUser().getURL() != null)
		         {
		             twitem.user.url = status.getUser().getURL().toString();
		         }
		         
				 intent.putExtra("currentstatus", twitem);
				 intent.putExtra("fromstatus", true);
				 ((TwitterTweetsActivity)mContext).startActivityForResult(intent, TwitterTweetsActivity.TWEET_DETAIL);
			 }
			 else if(type ==1 && tweet != null)
			 {
				 TwitterStatus twitem = new TwitterStatus();
		         twitem.createdAt = tweet.getCreatedAt().getTime();
		         twitem.id        = tweet.getId();
		         twitem.inReplyToStatusId = -1;
		         twitem.inReplyToUserId   = tweet.getToUserId();
		         twitem.isFavorited       = false;
		         twitem.isTruncated       = false;
		         twitem.source  = tweet.getSource();
		         twitem.text    = tweet.getText();
		         twitem.user = new TwitterUser();
		         twitem.user.id = tweet.getFromUserId();
		         twitem.user.description = "";
		         twitem.user.followersCount = -1;
		         twitem.user.isProtected    = false;		         
		         twitem.user.name           = tweet.getFromUser();
		         twitem.user.profileImageUrl = tweet.getProfileImageUrl();
		         twitem.user.screenName      = tweet.getFromUser();
		         twitem.user.url             = "";		        
				 intent.putExtra("currenttweet", twitem);
				 intent.putExtra("fromtweet", true);
				 
				 ((TwitterSearchActivity)mContext).startActivityForResult(intent, TwitterSearchActivity.TWEET_DETAIL);
			 }
			 
		}
	};
	

	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public void setTweetItem(Tweet di) 
	{
		tweet = di;
		username.setText(tweet.getFromUser());
		String url = tweet.getProfileImageUrl();
		
		ImageRun imagerun = new ImageRun(handler, url, 0);
		imagerun.use_avatar = true;
		imagerun.addHostAndPath = true;
		imagerun.setImageView(imageView);
		imagerun.post(null);
		
		publishDate.setText(tweet.getCreatedAt().toLocaleString());
		
		publishTxt.setText(tweet.getText());
	}

	public void setStatusItem(Status di) 
	{
		status = di;
		username.setText(status.getUser().getScreenName());
		
		String url = status.getUser().getProfileImageURL().toString();
		ImageRun imagerun = new ImageRun(handler, url, 0);
		imagerun.use_avatar = true;
		imagerun.addHostAndPath = true;
		imagerun.setImageView(imageView);
		imagerun.post(null);
		
		publishDate.setText(status.getCreatedAt().toLocaleString());
		
		publishTxt.setText(status.getText());
	}
}
