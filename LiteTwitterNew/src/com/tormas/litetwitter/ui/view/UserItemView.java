package com.tormas.litetwitter.ui.view;

import com.tormas.litetwitter.*;
import twitter4j.SimplyUser;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.util.Log;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;

import com.tormas.litetwitter.ui.TwitterHelper;

public class UserItemView  extends SNSItemView {
	private final String TAG="UserItemView";
	private ImageView imageView;	
	private TextView  username;	
	
	private SimplyUser user;	
	String  imageURL;
	Handler handler;
	java.util.Random random = new java.util.Random();
	
	public UserItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		
		mContext = ctx;
		setOrientation(LinearLayout.VERTICAL);
        setVisibility(View.VISIBLE);   
        
        handler = new Handler();
    }
	
	public UserItemView(Context context, SimplyUser di) 
	{		
		super(context);	
		mContext = context;
		user = di;
		
		Log.d(TAG, "call UserItemView");
		
		handler = new Handler();
		init();		
	}
	
	//create the view
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
		View v  = factory.inflate(R.layout.twitter_follow_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imageView  = (ImageView)v.findViewById(R.id.follow_img_ui);		
		username   = (TextView)v.findViewById(R.id.follow_user_name);
	}	
	
	public void setUserItem(SimplyUser di) {
		user = di;
		username.setText(di.name);
				
		ImageRun imagerun = new ImageRun(handler, di.profileImageUrl, 0);
		imagerun.use_avatar = true;
		imagerun.addHostAndPath = true;
		imagerun.setImageView(imageView);
		imagerun.post(null);
	}

	
	public void chooseFollowListener()
	{
		setOnClickListener(followOnClik);
	}
	
	View.OnClickListener followOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			//enter user details, re-user the TwitterTweetActivity
		}
	};

	@Override
	public String getText() 
	{		
		return user.name;
	}

}
