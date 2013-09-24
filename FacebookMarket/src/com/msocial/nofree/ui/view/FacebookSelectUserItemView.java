package com.msocial.nofree.ui.view;

import java.util.List;

import com.msocial.nofree.R;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.FacebookBaseActivity;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class FacebookSelectUserItemView extends SNSItemView 
{
	private final String TAG="FacebookSelectUserItemView";
	
	private TextView email;	
	private TextView username;
	private CheckBox chekbox;
	
	FacebookUser user;
	
	SocialORM orm;
	Handler handler;
	public FacebookSelectUserItemView(Context context, FacebookUser di) {
		super(context);
		mContext = context;
		user = di;
		
		Log.d(TAG, "call FacebookSelectUserItemView");
		
		orm = SocialORM.instance(context);
		
		handler = new Handler();
		init();
	} 
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	public String getName()
	{
		return user.name;
	}	
	
	public long getUserID()
	{
		return user.uid;
	}	
	
	public boolean isSelected()
	{
		return user.selected;
	}
	
	private void init() 
	{
		Log.d(TAG,  "call FacebookSelectUserItemView init");
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
		email      = (TextView)v.findViewById(R.id.sns_user_email_item);
		chekbox.setOnClickListener(stOnClik);
		setUI();	
	}
	
	private void setUI()
	{
		if(user.name == null || user.name.length() ==0)
		{
			//get info from web
			username.setText(R.string.sns_retrieving);
			if(FacebookBaseActivity.class.isInstance(mContext))
			{
				AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
				if(af != null)
				{
					long[] uids = new long[1];
					uids[0] = getUserID();
					af.getBasicUsersAsync(uids, new FacebookAdapter()
			    	{
			    		@Override public void getUsers(List<FacebookUser> users)
			            {
			    			if(users != null && users.size()>0)
			    			{
			    				Log.d(TAG, "after get user info="+user);
			    				user = users.get(0);				    			
				    			
								//update database
								orm.addFacebookUser(user);
								updateUIFromUser();
			    			}
			            }
			    		
			            @Override public void onException(FacebookException e, int method) 
			            {
			            	Log.d(TAG, "fail to get the image");			            	         	
			            }
			    	});
				}
			}
		}
		else
		{
		    username.setText(user.name);
		}
		//don't show this
		email.setVisibility(View.GONE);		
		chekbox.setChecked(user.selected);
	}
	
	protected void updateUIFromUser() 
	{	
		handler.post( new Runnable()
		{
		   public void run()
		   {
			   username.setText(user.name);
		   }
		});
	}
	
	public void setUserItem(FacebookUser di) 
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
			user.selected = !user.selected;
			chekbox.setChecked(user.selected);			
			Log.d(TAG, "onClick select ="+user.selected);
		}
	};
	@Override
	public String getText() 
	{		
		return user !=null?user.name:"";
	}
}
