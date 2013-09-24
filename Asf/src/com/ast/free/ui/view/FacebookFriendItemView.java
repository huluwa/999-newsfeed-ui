package com.ast.free.ui.view;

import java.util.ArrayList;
import java.util.List;

import com.ast.free.R;
import com.ast.free.providers.SocialORM;
import com.ast.free.ui.FacebookAccountActivity;
import com.ast.free.ui.FacebookBaseActivity;
import com.ast.free.ui.FacebookMailDetailActivity;
import com.ast.free.ui.FacebookNewFriendsActivity;
import oms.sns.service.facebook.client.FacebookMethod.Phonebook;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class FacebookFriendItemView extends SNSItemView {
	private final String TAG="FacebookFriendItemView";
	
	private ImageView imgView;
	private TextView  userName;
	private TextView  friendBD;
	private ImageView requestFriendView;
	private TextView  friendBD_Month;
	
	private ImageView email;
	private ImageView call;
	private View      phonebook;
	private SocialORM orm;
	
	public boolean   hasPhonebook;
	public String    emialStr;
	public String    cellStr;
	public String    phoneStr;
			
	private FacebookUser.SimpleFBUser  user;
    public long getUID()
    {
    	if(user != null)
    	{
    		return user.uid;
    	}
    	return -1;
    }
    Handler handler;
    
	boolean fromGroupMember = false;
	
	boolean forcursor = false;
	public void setForCusor(boolean cursor)
	{
	    forcursor = cursor;
	}
	static private String[] month_array;
	
    public FacebookUser.SimpleFBUser getUser()
    {
    	return user;
    }
    
	public FacebookFriendItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx.getApplicationContext(), attrs);		
		mContext = ctx;
		fromGroupMember = false;
				 
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);
        handler = new Handler();
        orm = SocialORM.instance(ctx);
    }

	
	public FacebookFriendItemView(Context context, FacebookUser.SimpleFBUser di,boolean isfriendbd) 
	{		
		super(context.getApplicationContext());
		mContext = context;
		if(month_array == null)
            month_array = mContext.getResources().getStringArray(R.array.entries_month);
        
		fromGroupMember = false;
				
		user = di;
		
		Log.d(TAG, "call  FacebookFriendItemView");
		handler = new Handler();
		orm = SocialORM.instance(context);
		
		init(isfriendbd);
	}
		
	private void init(boolean isfriendbd) 
	{
		Log.d(TAG,  "call FacebookStatusView init");
		LayoutInflater factory = LayoutInflater.from(mContext.getApplicationContext());
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = null;
		if(isfriendbd)
		{
		   v = factory.inflate(R.layout.facebook_friend_item, null);
		}
		else
		{
		   v = factory.inflate(R.layout.facebook_new_friend_item, null);
		} 		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		int width = 0;
		int otherwidth = 10+60+60;
		int leftwidth = 0;
		
		if(FacebookNewFriendsActivity.class.isInstance(mContext) && width ==0)
		{
		    width = ((FacebookNewFriendsActivity)mContext).getWindowManager().getDefaultDisplay().getWidth();
		    leftwidth = width - otherwidth;
		}
		
		imgView  = (ImageView)v.findViewById(R.id.facebook_friends_img_ui);
		userName = (TextView)v.findViewById(R.id.facebook_friend_user_name);	
		if(leftwidth > 0)
		{
		    userName.setWidth(leftwidth);
		    userName.setMaxWidth(leftwidth);
		}
		requestFriendView = (ImageView)v.findViewById(R.id.facebook_friend_groupmember_request);
		
		
		phonebook = this.findViewById(R.id.facebook_friend_phonebook_region);
		call      = (ImageView)this.findViewById(R.id.facebook_friend_call);
		email     = (ImageView)this.findViewById(R.id.facebook_friend_email);
		
		if(isfriendbd)
		{
			friendBD = (TextView)v.findViewById(R.id.facebook_friend_bd);
			friendBD.setVisibility(View.VISIBLE);
			friendBD_Month = (TextView)v.findViewById(R.id.facebook_friend_bd_month);
			friendBD_Month.setVisibility(View.VISIBLE);
			
			phonebook.setVisibility(View.GONE);
		}
		setUI(isfriendbd);			
	}	
	
	private void setUI(boolean isfriendbd)
	{
		if(isEmpty(user.pic_square) == false)
		{
			ImageRun imagerun = new ImageRun(handler, user.pic_square, 0);
			imagerun.use_avatar = true;
			imagerun.setImageView(imgView);
			imagerun.post(imagerun);
		}
		else
		{
			imgView.setImageResource(R.drawable.no_avatar);
		}
		
		userName.setText(user.name);
		if(isfriendbd && friendBD!=null){
			friendBD.setText(user.birthday);			
			if(user.b_month>=0)
			{
			    friendBD_Month.setText(month_array[user.b_month]);
			    Log.d(TAG,"friend bd is "+ user.birthday +" bd_month is "+month_array[user.b_month]);
			}
			else
			{
			    Log.d(TAG,"friend bd is "+ user.birthday+" bd_month is "+ user.b_month);
			    friendBD_Month.setText("");
			}
		}
		
		if(isfriendbd == false)
        {
    		hasPhonebook = false;
    		emialStr = null;
    		cellStr  = null;
    		phoneStr = null;
    		
    		phonebook.setVisibility(View.GONE);
    		phonebook.setOnClickListener(null);
    		email.setVisibility(View.GONE);
    		email.setOnClickListener(null);
    		call.setVisibility(View.GONE);
    		call.setOnClickListener(null);
    		
    		PhoneBook pb = orm.getPhonebook(user.uid);
    		if(pb != null && orm.isUnLimitVersion())
    		{
    			if(isEmpty(pb.email) == false)
    			{
    				phonebook.setVisibility(View.VISIBLE);
    				email.setVisibility(View.VISIBLE);
    		
    				hasPhonebook = true;
    				emialStr = pb.email;
    				email.setOnClickListener( new View.OnClickListener() 
    				{	
    					public void onClick(View v) 
    					{
    						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+ emialStr));
    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				        mContext.startActivity(intent);
    					}
    				});
    			}
    			int phonecount = 0;
    			if(isEmpty(pb.cell) == false || isEmpty(pb.phone) == false)
    			{
    				phonebook.setVisibility(View.VISIBLE);
    				call.setVisibility(View.VISIBLE);
    				
    				hasPhonebook = true;
    				
    				if(isEmpty(pb.cell) == false)	
    				{
    					phonecount++;
    				    cellStr  = pb.cell;
    				}
    				
    				if(isEmpty(pb.phone) == false)
    				{
    					phonecount++;
    				    phoneStr = pb.phone;
    				}
    				
    				call.setOnClickListener( new View.OnClickListener() 
    				{	
    					public void onClick(View v) 
    					{
    										
    						List<String> listaction = getActionList();
    						String items[] = new String[listaction.size()];
    						for(int i=0;i<listaction.size();i++)
    						{
    							items[i] = listaction.get(i);
    						}
    						AlertDialog dialog = new AlertDialog.Builder((FacebookBaseActivity)mContext)					
    						.setTitle(R.string.phone_action)
    		                .setItems(items, new DialogInterface.OnClickListener() 
    		                {
    		                    public void onClick(DialogInterface dialog, int which) 
    		                    {		                    
    		                    	List<String> listaction = getActionList();
    		                    	if(listaction.size() == 1)//just for phone
    		                    	{
    		                    		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneStr));
    		    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		    				        mContext.startActivity(intent);
    		                    	}
    		                    	else if(listaction.size() == 2)
    		                    	{
    		                    		if(which == 0)
    		                    		{
    			                    		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cellStr));
    			    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			    				        mContext.startActivity(intent);
    		                    		}
    		                    		else
    		                    		{
    		                    			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + cellStr));
    			    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			    				        mContext.startActivity(intent);
    		                    		}
    		                    	}
    		                    	else if(listaction.size() == 3)
    		                    	{
    		                    		if(which == 0)
    		                    		{
    			                    		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneStr));
    			    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			    				        mContext.startActivity(intent);
    		                    		}
    		                    		else if(which == 1)
    		                    		{
    			                    		Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cellStr));
    			    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			    				        mContext.startActivity(intent);
    		                    		}
    		                    		else if(which == 2)
    		                    		{
    		                    			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:" + cellStr));
    			    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			    				        mContext.startActivity(intent);
    		                    		}
    		                    	}
    		                    }
    		                }).create();						
    						dialog.show();						
    						/*
    						Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + (isEmpty(cellStr)==true?phoneStr:cellStr)));
    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    				        mContext.startActivity(intent);
    				        */
    					}
    				});
    			}
    			
    			pb.dispose();
    			pb = null;
    		}
        }
	}
	
	private List<String> getActionList()
	{
		List<String> listaction= new ArrayList<String>();
		if(isEmpty(phoneStr) == false)
		{
			listaction.add(mContext.getString(R.string.phonebook_call) + " " + phoneStr);
		}						
		if(isEmpty(cellStr) == false)
		{
			listaction.add(mContext.getString(R.string.phonebook_call) + " " + cellStr);
			listaction.add(mContext.getString(R.string.phonebook_message) + " " + cellStr);
		}		
		return listaction;
	}
	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init(false);
	}
	
	
	public void chooseFriendListener()
	{
		setOnClickListener(friendOnClik);
	}
	
	View.OnClickListener friendOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "friendOnClik you click first one=");			 
			 //view details
			Intent intent = new Intent(mContext, FacebookAccountActivity.class);
			intent.putExtra("uid", user.uid);
			intent.putExtra("username", user.name);
			intent.putExtra("imageurl", user.pic_square);				
			((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
		}
	};
	
	public void setUserItem(FacebookUser.SimpleFBUser di,boolean isfriendbd) 
	{
	    if(forcursor)
	    {
    	    user.despose();
    	    user = null;
	    }
		user = di;
		setUI(isfriendbd);
	}
	@Override
	public String getText() 
	{		
		return "";
	}
}

