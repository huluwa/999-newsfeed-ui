package com.msocial.freefb.ui.view;

import java.util.List;

import com.msocial.freefb.R;
import oms.sns.service.facebook.model.UserStatus;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FacebookStatusView   extends SNSItemView {
	private final String TAG="FacebookStatusView";
	private TextView username;
	private TextView publishDate;
	private TextView publishTxt;
		
	private UserStatus  status;	

	private boolean showUserName = false;
	public FacebookStatusView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);		
		mContext = ctx;		
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
    }
	
	
	public FacebookStatusView(Context context, UserStatus di) 
	{		
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call mail box FacebookStatusView");
		
		init();
	}
	public FacebookStatusView(Context context, UserStatus di, boolean showname) 
	{		
		
		super(context);
		mContext = context;
		status = di;
		
		Log.d(TAG, "call mail box FacebookStatusView");
		showUserName = showname;
		
		init();
	}
	public void showUserName(boolean showornot)
	{
		showUserName = showornot;
	}
	private String getDate()
	{
		return status.time==null?"":status.time.toLocaleString();
	}
	public String getText()
	{
		return status.message;
	}	
		
	private void init() 
	{
		Log.d(TAG,  "call FacebookStatusView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		int resID = R.layout.facebook_status_item;
		if(showUserName == false)
		{
			resID = R.layout.facebook_status_item_left;
		}
		
		View v  = factory.inflate(resID, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		publishDate  = (TextView)v.findViewById(R.id.facebook_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.facebook_publish_text);	
		
		if(showUserName)
		{
			//View username_span = v.findViewById(R.id.facebook_username_span);
			//username_span.setVisibility(View.VISIBLE);			
		    username     = (TextView)v.findViewById(R.id.facebook_username);	
		}
		
		setUI();
	}
	
	private void setUI()
	{
		if(username != null)
		{
			username.setText(status.username);
		}
		
		if(status.time != null)
		{
		    publishDate.setText(status.time.toLocaleString());
		}
		
		if(isEmpty(status.message) == false)
		{
		    publishTxt.setText(status.message);
		}
	}
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public void setStatusItem(UserStatus di) 
	{
		status = di;
		setUI();
	}


	public CharSequence getUserName() 
	{
		if(showUserName)
		{
			return status.username;
		}
		
		return null;
	}


	public long getUserID() 
	{		
		return status.uid;
	}	
}

