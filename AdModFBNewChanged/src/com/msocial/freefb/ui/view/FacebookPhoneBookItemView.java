package com.msocial.freefb.ui.view;

import java.util.List;

import com.msocial.freefb.R;
import com.msocial.freefb.providers.SocialORM;
import com.msocial.freefb.ui.FacebookAccountActivity;
import com.msocial.freefb.ui.FacebookBaseActivity;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.PhoneBook;
import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FacebookPhoneBookItemView extends SNSItemView 
{	
		private final String TAG="FacebookPhoneBookItemView";
		
		private boolean fromsearch;
		private ImageView imageView;
		private TextView cell;
		private TextView phone;
		private TextView email;
		private TextView username;	
		private Button   addAsFriend;
		
		String  imageURL;
		Handler handler;
		
		java.util.Random random = new java.util.Random();		
		SocialORM    orm;
		PhoneBook    phonebook;
		
		public String getImageUrl()
		{
			return imageURL;
		}
		
		public FacebookPhoneBookItemView(Context ctx, AttributeSet attrs) 
		{
			super(ctx.getApplicationContext(), attrs);
			
			mContext = ctx;
			orm = SocialORM.instance(mContext);
			setOrientation(LinearLayout.VERTICAL);
	        this.setVisibility(View.VISIBLE);   
	        
	        handler = new MessageHandler();
	    }
		
		public PhoneBook getPhoneBook()
		{
			return phonebook;
		}
		private void updateUIFromUser()
		{
			handler.obtainMessage(UPDATE_UI).sendToTarget();
		}
		
		private void updateUIButton(boolean ismyfriend){
			
			Log.d(TAG,"entering update Button "+ismyfriend);
			
			Message msd = handler.obtainMessage(UPDATE_BUTTON_UI);
			msd.getData().putBoolean("ismyfriend", ismyfriend);
			handler.sendMessage(msd);
		}
		
		final int UPDATE_UI       =0;
		final int UPDATE_IMAGE_UI =1;
		final int UPDATE_BUTTON_UI   = 2;
		public class MessageHandler extends Handler
		{
		    public MessageHandler()
	        {
	            super();            
	            Log.d(TAG, "new MessageHandler");
	        }
	        
	        @Override
	        public void handleMessage(Message msg) 
	        {
	            switch(msg.what)
	            {
	            	case UPDATE_UI:
	            	{	       
	            		if(phonebook != null)
	            		{	
	            			username.setText(phonebook.username);
	            		}
	            		break;
	            	}
	            	case UPDATE_IMAGE_UI:
	            	{
	            		String url = msg.getData().getString("imageurl");
	            		ImageRun imagerun = new ImageRun(handler, url, 0);
	            		imagerun.use_avatar = true;
	            		imagerun.setImageView(imageView);
	            		imagerun.post(imagerun);
	            		
	            		break;
	            	}
	            	case UPDATE_BUTTON_UI:
	            	{
	            		boolean ismyfriend = msg.getData().getBoolean("ismyfriend");
	            		Log.d(TAG,"entering update button "+ismyfriend);
	            		if(ismyfriend){
	            		  addAsFriend.setVisibility(View.GONE);
	            		}else{
	            		  addAsFriend.setVisibility(View.VISIBLE);
	            		}
	            		break;
	            	}
	            }
	        }	        
		}
		
		public FacebookPhoneBookItemView(Context context,PhoneBook di){
			super(context.getApplicationContext());
			mContext = context;
			phonebook = di;
			
			orm = new SocialORM(mContext);
			Log.d(TAG, "call mail box FacebookPhoneBookItemView");
			
			handler = new MessageHandler();
			init(false);
		}
		
		public FacebookPhoneBookItemView(Context context,PhoneBook di,boolean fromsearch){
			super(context.getApplicationContext());
			mContext = context;
			phonebook = di;
			
			orm = SocialORM.instance(mContext);
			Log.d(TAG, "call mail box FacebookPhoneBookItemView");
			
			handler = new MessageHandler();
			init(fromsearch);
		}
			
		
		public long getFromUID()
		{
			 return phonebook.uid;
		}
		
		private void setButton()
		{
			if(FacebookBaseActivity.class.isInstance(mContext))
			{
				AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
				if(af != null)
				{
					af.isMyFriendAsync(phonebook.uid, new FacebookAdapter()
			    	{
			    		@Override public void isMyFriend(boolean ismyfriend)
			            {
			    		    Log.d(TAG, " we are friends "+ismyfriend);
			    		    updateUIButton(ismyfriend);

			            }
			    		
			            @Override public void onException(FacebookException e, int method) 
			            {
			            	Log.d(TAG, "fail to get the image");
			            	getImageBMP(null, true);         	
			            }
			    	});
				}
			}
			
		}
		//get the image from database, 
		//if the user is not exist, will load the user data, and save them into database
		//
		private void setImage()
		{	
			if(imageURL == null)
			{
				long id = getFromUID();
				FacebookUser user = orm.getFacebookUser(id);
				boolean getFromWeb=false;
				if(user == null)
				{
					getFromWeb = true;
				}
				else
				{
					//Log.d(TAG, "who am I="+user);
					imageURL = user.pic_square;
					phonebook.username = user.name;
					//no user data, maybe the user has image
					if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
					{
						getFromWeb = true;
					}					
				}			    
			    //the person might have no pic, so no need to call this fun
			    if(getFromWeb == true)
				{	
					if(FacebookBaseActivity.class.isInstance(mContext))
					{
						AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
						if(af != null)
						{
							long[] uids = new long[1];
							uids[0] = id;
							af.getBasicUsersAsync(uids, new FacebookAdapter()
					    	{
					    		@Override public void getUsers(List<FacebookUser> users)
					            {
					    			if(users != null && users.size()>0)
					    			{
					    				FacebookUser tmp = users.get(0);
					    				Log.d(TAG, "after get user info="+tmp);
					    				
						    			imageURL = tmp.pic_square;
						    			getImageBMP(imageURL, true);
										
										//update database
										orm.addFacebookUser(tmp);
										phonebook.username = tmp.name;
										
										updateUIFromUser();
					    			}
					            }
					    		
					            @Override public void onException(FacebookException e, int method) 
					            {
					            	Log.d(TAG, "fail to get the image");
					            	getImageBMP(null, true);         	
					            }
					    	});
						}
					}
					
				}
				else
				{	
					getImageBMP(imageURL, false);
				}
				
			}
			else//I have get the image
			{				
				getImageBMP(imageURL, false);
			}
		}
		
		private void getImageBMP(String url, boolean fromAnotherThread)
		{
			if(fromAnotherThread == true)
			{
				Message msg = handler.obtainMessage(UPDATE_IMAGE_UI);
				msg.getData().putString("imageurl", url);
				handler.sendMessage(msg);
			}
			else//from the same thread
			{
				ImageRun imagerun = new ImageRun(handler, url, 0);		
                imagerun.use_avatar = true;
                imagerun.setImageView(imageView);
                imagerun.post(imagerun);
			}
		}
		
		private void addAsFriend()
		{
			if(FacebookBaseActivity.class.isInstance(mContext))
			{ 
			    addAsFriend.setEnabled(false);
				AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
				if(af != null)
				{
					af.addAsFriendAsync(phonebook.uid, new FacebookAdapter()
			    	{
			    		@Override public void addAsFriend(long uid,boolean returnret)
			            {
			    			Log.d(TAG, "add success ");
			    			handler.post(new Runnable(){
			    			   public void run()
			    			   {
			    			       Toast.makeText(mContext, R.string.facebook_add_as_friend_successfully, Toast.LENGTH_LONG).show();
                                   addAsFriend.setEnabled(true);
			    			       addAsFriend.setVisibility(View.GONE);
			    			   }
			    			});	
			            }
			    		
			            @Override public void onException(FacebookException e, int method) 
			            {
			            	Log.d(TAG, "fail to add friend");
			            	handler.post(new Runnable(){
                               public void run()
                               {
                                   Toast.makeText(mContext, R.string.facebook_add_as_friend_failed, Toast.LENGTH_LONG).show();
                                   addAsFriend.setEnabled(true);
                               }
	                        });
			            	
			            }
			    	});
				}
			}
		}
		
		private void init(boolean fromsearch){
			Log.d(TAG,  "call FacebookPhoneBookItemView init");
			LayoutInflater factory = LayoutInflater.from(mContext.getApplicationContext());
			removeAllViews();
			
			//container
			//FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
			//FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
			//view.setLayoutParams(paras);
			//view.setVerticalScrollBarEnabled(true);
			//addView(view);
			
			//child 1
			View v  = factory.inflate(R.layout.facebook_phonebook_ui, null);		
			v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
			//view.addView(v);
			addView(v);
			
			imageView  = (ImageView)v.findViewById(R.id.phonebook_img_ui);
			
			cell     = (TextView)v.findViewById(R.id.phonebook_cell);
			phone    = (TextView)v.findViewById(R.id.phonebook_phone);		
			email    = (TextView)v.findViewById(R.id.phonebook_email);
			username = (TextView)v.findViewById(R.id.phonebook_user_name);
			
            addAsFriend = (Button)v.findViewById(R.id.facebook_add_as_friend);
            addAsFriend.setOnClickListener(addfriendListener);
            addAsFriend.setText(R.string.menu_facebook_title_add_as_friend);
            addAsFriend.setVisibility(View.GONE);
           
         
        	cell.setVisibility(View.GONE);
        	phone.setVisibility(View.GONE);
        	email.setVisibility(View.GONE);      
        	
			setUI(fromsearch);		
		}
	
		private void init() 
		{
			init(false);
		}

		@Override
		protected void onFinishInflate() 
		{	
			super.onFinishInflate();		
			init();
		}
		
		private void setUI(boolean fromsearch){
			setImage();
			
			if(fromsearch){
				//TODO
				setButton();
				imageView.setOnClickListener(viewUserDetailsClick);
				username.setOnClickListener(viewUserDetailsClick);
				
			}else{
			
				if(phonebook.cell == null || phonebook.cell.length()==0)
				{
					cell.setVisibility(View.GONE);
					cell.setText(R.string.sns_no_cell);
				}
				else
				{
				    cell.setVisibility(View.VISIBLE);
				    cell.setText(phonebook.cell);
				}
				
				if(phonebook.phone == null || phonebook.phone.length()==0)
				{
					phone.setVisibility(View.GONE);
					phone.setText(R.string.sns_no_phone);
				}
				else
				{
				    phone.setVisibility(View.VISIBLE);
				    phone.setText(phonebook.phone);
				}
				
				if(phonebook.email == null || phonebook.email.length()==0)
				{
					email.setVisibility(View.GONE);
					email.setText(R.string.sns_no_email);
				}
				else
				{
				    email.setVisibility(View.VISIBLE);
				    email.setText(phonebook.email);
				}
			}
			//Log.d(TAG, "entering set username ");
			username.setText(phonebook.username);
		}
		
	    View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
	    {
	        public void onClick(View v) 
	        {
	            Log.d(TAG, "viewUserDetailsClick you click first one=");   
	            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
	            if(phonebook != null)
	            {
	                intent.putExtra("uid",      phonebook.uid);
	                if(isEmpty(phonebook.username)== false)intent.putExtra("username", phonebook.username);
	                if(isEmpty(imageURL)== false) intent.putExtra("imageurl", imageURL);               
	            }
	            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
	        }
	    };
		
		private void setUI()
		{
			setUI(false);
		}
		
		public void setPhoneBookItem(PhoneBook di) 
		{
		    phonebook = di;
		    //need re-get the image
		    imageURL = null;
		    setUI();	
		}
		
		public void setPhoneBookItem(PhoneBook di,boolean useCursor) 
		{    
		    phonebook.recycle();
		    phonebook = null;
		    phonebook = di;
		    //need re-get the image
		    imageURL = null;
		    setUI();
		}

		public void choosePhoneBookListener() 
		{
			//this.setOnClickListener(phonebookdetailOnClik);
		}
		
		View.OnClickListener phoneOnClik = new View.OnClickListener()
		{
			public void onClick(View v) 
			{
				 Log.d(TAG, "phoneOnClik you click first one=");
			}
		};
		
		View.OnClickListener emailOnClik = new View.OnClickListener()
		{
			public void onClick(View v) 
			{
				 Log.d(TAG, "emailOnClik you click first one=");
			}
		};
		
		View.OnClickListener addfriendListener = new View.OnClickListener(){

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				addAsFriend();
			}
			
		};

		@Override
		public String getText() 
		{			
			return "";//String.format("Cell: %1$s, Phone:&2$s, email:%3$s", phonebook.cell, phonebook.phone, phonebook.email);
		}
	
}


