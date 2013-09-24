package com.msocial.facebook.ui.view;

import com.msocial.facebook.*;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.providers.SocialORM.PeopleMapFacebook;
import com.msocial.facebook.ui.FacebookFindFriendsActivity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.Contacts;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FacebookFindFriendItemView extends SNSItemView {
	private final String TAG="FacebookFindFriendItemView";
	
	private TextView     userName;
	private ImageView    request;
			
	private PeopleMapFacebook  user;	
    Handler handler;
    SocialORM orm;
    
    public PeopleMapFacebook getUser()
    {
    	return user;
    }
	public FacebookFindFriendItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);		
		mContext = ctx;		
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
        orm = SocialORM.instance(mContext);
    }

	
	public FacebookFindFriendItemView(Context context, PeopleMapFacebook di) 
	{		
		super(context);
		mContext = context;
		user = di;
		orm = SocialORM.instance(mContext);
		
		Log.d(TAG, "call  FacebookFindFriendItemView");
		handler = new Handler();
		init();
	}
	public long getFUID()
	{
		return user.uid;
	}
	private void init() 
	{
		Log.d(TAG,  "call FacebookFindFriendItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_find_friend_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		userName = (TextView)v.findViewById(R.id.facebook_find_friend_user_name);		
		request  = (ImageView)v.findViewById(R.id.facebook_friend_request);		
		setUI();			
	}	
	
	private void setUI()
	{
		//get contact name from peopleid		
		userName.setText(getPersonName());
		if(user.isFriend)
		{
			request.setVisibility(View.GONE);			
		}
		else
		{
			request.setVisibility(View.VISIBLE);
			request.setImageResource(R.drawable.select);	
			request.setOnClickListener(requestFriendsClick);			
		}
	}
	
	public boolean isFriend()
	{
		return user.isFriend;
	}
	 public String getPersonName()
	 {
		 //already have name
		 if(user.Name != null && user.Name.length()> 0)
		 {
			 return user.Name;
		 }
		
		 Uri person = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, user.peopleid);
		 Cursor cursor = mContext.getContentResolver().query(person, new String[]{ Contacts.People.NAME}, null, null, null);
         try 
         {
            if (!cursor.moveToNext()) {
                return null;
            }
            String data = cursor.getString(0);
            if (data == null || data.length()== 0 ) 
            {
                return null;
            }
            if(FacebookFindFriendsActivity.class.isInstance(mContext))
			{
				 FacebookFindFriendsActivity findA = (FacebookFindFriendsActivity)mContext;
				 findA.setUserName(user.peopleid, data);
			}
            
            return data;
         } 
         finally 
         {
            if ( cursor!= null)          	
                cursor.close();
         }
	}

	
	 private Uri getPhotoUri(Uri person)
	 {
        Cursor cursor = mContext.getContentResolver().query(person, new String[]{"photo_data"}, null, null, null);
        try 
        {
            if (!cursor.moveToNext()) {
                return null;
            }
            String data = cursor.getString(0);
            if (data == null || data.length()== 0 ) {
                return null;
            }
            Uri uri = Uri.parse(data);
            return uri;
        } 
        finally 
        {
            if ( cursor!= null)
                cursor.close();
        }
	}
		
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	
	public void chooseFriendListener()
	{
		//setOnClickListener(friendOnClik);
	}
	
	View.OnClickListener friendOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "findfriendOnClik you click first one=");			 
			 //view contact details
			 Uri peopleuri = ContentUris.withAppendedId(Contacts.People.CONTENT_URI, user.peopleid);
			 Intent intent = new Intent(Intent.ACTION_VIEW);
			 intent.setData(peopleuri);
			 mContext.startActivity(intent);
		}
	};
		
	
	View.OnClickListener requestFriendsClick = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "requestFriendsClick you click first one=");			 
			 //view contact details
			 if(FacebookFindFriendsActivity.class.isInstance(mContext))
			 {
				 FacebookFindFriendsActivity findA = (FacebookFindFriendsActivity)mContext;
				 findA.requestFrinds(user.uid);
			 }
		}
	};
	
	public void setUserItem(PeopleMapFacebook di) 
	{
		user = di;
		
		setUI();
	}
	public int getPeopleID() {
		
		return user.peopleid;
	}
	@Override
	public String getText() 
	{		
		return "";
	}	
}

