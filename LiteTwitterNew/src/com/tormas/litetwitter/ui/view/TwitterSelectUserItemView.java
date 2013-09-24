package com.tormas.litetwitter.ui.view;

import com.tormas.litetwitter.R;
import com.tormas.litetwitter.providers.SocialORM;
import com.tormas.litetwitter.providers.SocialORM.Follow;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TwitterSelectUserItemView extends SNSItemView{
    private final String TAG="TwitterSelectUserItemView";	
	private TextView username;
	private CheckBox chekbox;
	
	Follow user;
	SocialORM orm;
	
	public TwitterSelectUserItemView(Context context, Follow di) 
	{
		super(context);
		mContext = context;
		user = di;		
		Log.d(TAG, "call TwitterSelectUserItemView");		
		orm = SocialORM.instance(context);
		init();
	} 
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public String getSName()
	{
		return user.Name;
	}	
	
	
	private void init() 
	{
		Log.d(TAG,  "call TwitterSelectUserItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.sns_user_select_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		chekbox    = (CheckBox)v.findViewById(R.id.sns_user_select_checkbox);
		username   = (TextView)v.findViewById(R.id.sns_user_name_item);
		chekbox.setOnClickListener(stOnClik);
		setUI();	
	}	
	
	private void setUI()
	{
		if(user.Name != null && user.Name.length()>0)		
		{
		    username.setText(user.Name);
		}
		else
		{
			if(user.SName != null && user.SName.length()>0)		
			{
			    username.setText(user.SName);
			}
		}
		chekbox.setChecked(user.selected);
	}
	
	public void setUserItem(Follow di) 
	{
	    user = di;
	    setUI();
	}

	
	public void setCheckBoxSelected(boolean sel)
	{
		user.selected = sel;
		chekbox.setChecked(user.selected);
		chekbox.invalidate();
		
		Log.d(TAG, "setSelected select ="+user.selected);
	}
	
	public void chooseSelectListener()
	{
		setOnClickListener(stOnClik);
	}
	
	View.OnClickListener stOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			user.selected  = !user.selected ;
			chekbox.setChecked(user.selected );			
			Log.d(TAG, "onClick select ="+user.selected );
		}
	};

	@Override
	public String getText() 
	{		
		return user.SName;
	}

}
