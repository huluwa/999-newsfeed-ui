package com.tormas.litesina.ui.view;

import oms.sns.TwitterUser;
import com.tormas.litesina.R;
import com.tormas.litesina.ui.TwitterBaseActivity;
import com.tormas.litesina.ui.TwitterFollowActivity;
import com.tormas.litesina.ui.TwitterUserDetailsActivity;
import twitter4j.SimplyUser;
import android.content.Context;
import android.content.Intent;
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

public class FollowItemView extends SNSItemView {
	private final String TAG="FollowItemView";
	private Handler handler;
	private SimplyUser user;
	private TextView username;
	private ImageView imageView;
	
	java.util.Random random = new java.util.Random();
	private ImageView imgIndicator;
	
	public FollowItemView(Context context) 
	{
		super(context);		
	}
	
	public FollowItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		
		mContext = ctx;
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);   
        
        handler = new Handler();
    }
	
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public FollowItemView(Context context, SimplyUser di) 
	{		
		super(context);
		mContext = context;
		user = di;
		
		Log.d(TAG, "call FollowItemView");		
		handler = new Handler();
		init();
	}	
	public String getScreenName() 
	{		
		return user.screenName;
	}

	public CharSequence getName() 
	{		
		return user.name;
	}
	public SimplyUser getUser()
	{
		return user;
	}

	public void setUserItem(SimplyUser di) 
	{
		user = di;		
		username.setText(user.name);
		ImageRun imagerun = new ImageRun(handler, user.profileImageUrl, 0);
		imagerun.use_avatar = true;
		imagerun.addHostAndPath = true;
		imagerun.setImageView(imageView);
		imagerun.post(imagerun);
		
		if(user.following)
        {
            imgIndicator.setVisibility(View.VISIBLE);
            imgIndicator.setImageResource(R.drawable.tiny_check);
        }
        else
        {
            imgIndicator.setVisibility(View.GONE);
        }
                    
	}	
	
	
	private void init() 
	{
		Log.d(TAG,  "call SimplyStatusItemView init");
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
		username     = (TextView)v.findViewById(R.id.follow_user_name);
		imgIndicator  = (ImageView)v.findViewById(R.id.follow_img_indicator);
		
		
		if(user!= null)
		{
			username.setText(user.name);
			if(user.following)
			    imgIndicator.setImageResource(R.drawable.tiny_check);
			
			ImageRun imagerun = new ImageRun(handler, user.profileImageUrl, 0);
			imagerun.setImageView(imageView);
			imagerun.use_avatar = true;
			imagerun.addHostAndPath = true;
			imagerun.post(imagerun);
		}
		
	}	

	public void chooseFollowListener() 
	{	
		//setOnClickListener(userDetailOnClik);	    
	}
	
	View.OnClickListener userDetailOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "userDetailOnClik you click first one=");			 
			 //view details
			 Intent intent = new Intent(mContext, TwitterUserDetailsActivity.class);
			 TwitterUser tuser = new TwitterUser();
			 tuser.following = true;
			 tuser.id        = user.id;
			 tuser.name      = user.name;
			 tuser.notifications   = user.notifications;
			 tuser.profileImageUrl = user.profileImageUrl;
			 tuser.screenName      = user.screenName;			 
			  
			 intent.putExtra("currentuser", tuser);
			 ((TwitterFollowActivity)mContext).startActivityForResult(intent, TwitterBaseActivity.TWITTER_FOLLOWING);
		}
	};

	public String getText() 
	{		
		return user.screenName;
	}			 			 

}
