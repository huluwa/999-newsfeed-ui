package com.msocial.nofree.ui.view;

import java.util.List;

import com.msocial.nofree.*;
import com.msocial.nofree.providers.SocialORM;
import com.msocial.nofree.ui.FacebookAccountActivity;
import com.msocial.nofree.ui.FacebookBaseActivity;
import com.msocial.nofree.ui.FacebookMailDetailActivity;
import com.msocial.nofree.ui.FacebookMainActivity;
import com.msocial.nofree.ui.TwitterHelper;
import com.msocial.nofree.ui.adapter.MailboxThreadParcel;
import com.msocial.nofree.ui.view.ImageRun;
import com.msocial.nofree.util.DateUtil;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxThread;
import oms.sns.service.facebook.model.Wall;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class MessageItemView extends SNSItemView {
	private final String TAG="MessageItemView";
	
	private ImageView imageView;
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private Wall  wall;	
	private MailboxThread mthread;
	
	static float mWidth = (float) 100.0;
	String  imageURL;
	Handler handler;
	int type;//0, wall, 1 mailbox
	java.util.Random random = new java.util.Random();
	private boolean showImage   =true;
	private boolean showUsername=true;
	SocialORM orm;
	FacebookUser user;
	
	public Wall getWall()
	{
		return wall;
	}
	public MailboxThread getMailboxThread()
	{
		return mthread;
	}
	public String getImagePath()
	{
		return imageURL;
	}
	
	public MessageItemView(Context ctx, AttributeSet attrs) 
	{
		super(ctx, attrs);
		mContext = ctx;
		orm = SocialORM.instance(mContext);
		setOrientation(LinearLayout.VERTICAL);
        this.setVisibility(View.VISIBLE);   
        
        handler = new MessageHandler();
    }
	private void updateUIFromUser()
	{
		handler.obtainMessage(UPDATE_UI).sendToTarget();
	}
	
	final int UPDATE_UI=0;
	final int UPDATE_PAT_UI=1;
	final int UPDATE_IMAGE_UI=2;
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
            		if(user != null)
            		{
            			if(type == 1)
            			{
            				if(mthread.unreadcount > 0)
            					username.setText( user.name+String.format("(%1$s)", mthread.unreadcount));
            				else
            					username.setText(user.name);
            			}
            			else
            			{
            				username.setText(user.name);
            			}
            			
            		}
            		break;
            	}
            	case UPDATE_PAT_UI:
            	{
            		String names = msg.getData().getString("usernames");            		
            		username.setText(names);
            		break;
            	}
            	case UPDATE_IMAGE_UI:
            	{
            		String url = msg.getData().getString("imageurl");
            		if(url == null)
            		{
            			imageView.setImageResource(R.drawable.noimage);
            		}            		
            		else
            		{            			
	            		ImageRun imagerun = new ImageRun(handler, url, 0);		
	            		imagerun.setImageView(imageView);
	            		imagerun.use_avatar = true;
	            		imagerun.post(imagerun);
            		}
            		break;
            	}
            }
        }	        
	}
	
	public MessageItemView(Context context, MailboxThread di) 
	{		
		super(context);
		mContext = context;
		mthread = di;
		
		orm = SocialORM.instance(mContext);
		Log.d(TAG, "call mail box MessageItemView");
		type = 1;
		handler = new MessageHandler();
		init();
	}
	
	public MessageItemView(Context context, Wall di) 
	{		
		super(context);
		mContext = context;
		wall = di;
		
		orm = SocialORM.instance(mContext);
		Log.d(TAG, "call wall MessageItemView");
		type = 0;
		handler = new MessageHandler();
		init();
	}
	
	public void disableImage()
	{
		showImage = false;
	}
	public void disableUserName()
	{
		showUsername = false;
	}
	
	public long getFromUID()
	{
		if(type ==0)
			return wall.getFromid();
		else
			return mthread.originator;
	}
	//get the image from database, 
	//if the user is not exist, will load the user data, and save them into database
	//
	private void setImage()
	{	
		if(imageURL == null)
		{
			long id = getFromUID();
			user = orm.getFacebookUser(id);
			boolean getFromDB=false;
			if(user == null)
			{
				getFromDB = true;
			}
			else
			{
				Log.d(TAG, "who am I="+user);
				imageURL = user.pic_square;
				//no user data, maybe the user has image
				if((imageURL == null || imageURL.length() ==0 ) && (user.name == null || user.name.length() == 0))
				{
					getFromDB = true;
				}
				
			}			    
		    //the person might have no pic, so no need to call this fun
		    if(getFromDB == true)
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
				    				user = users.get(0);
				    				Log.d(TAG, "after get user info="+user);
				    				
					    			imageURL = user.pic_square;					    			
					    			getImageBMP(imageURL, true);
									
									//update database
									orm.addFacebookUser(user);
									
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
    		imagerun.setImageView(imageView);
    		imagerun.use_avatar = true;
    		imagerun.post(imagerun);
		}
	}
	private String getDate()
	{
		if(type ==0)
		    return DateUtil.converToRelativeTime(mContext,wall.getTime());
			//return wall.getTime().toLocaleString();
		else
		    return DateUtil.converToRelativeTime(mContext,mthread.getLastupdate());
			//return mthread.getLastupdate().toLocaleString();
	}
	public String getText()
	{
		if(type==0)
		{
		    return wall.getBody();
		}
		else
		{
			if(mthread.getSubject() == null || mthread.getSubject().length()==0)
			{
				return "<no subject>";
			}
			
			return mthread.getSubject();
		}
	}
	public String getViewUserName() 
	{
		if(type ==0)
		{
		    return wall.getFromusername();
		}
		else
		{
			String usernames="";
			long []uids = new long[mthread.participants.size()];
			for(int i=0;i<mthread.participants.size();i++)
			{
				uids[i] = mthread.participants.get(i);
			}
			List<FacebookUser>parts = orm.getFacebookUsers(uids);
			if(parts.size() > 0)
			{
				for(int i=0;i<parts.size();i++)
				{
					if(parts.get(i).name != null)
					{
						if(i>0)
							usernames +=", ";
						
						usernames +=parts.get(i).name;
					}
				}
				if(usernames.length()>0)
				{
					if(mthread.unreadcount > 0)
					    return usernames+String.format("(%1$s)", mthread.unreadcount);
					else
						return usernames;
				}
			}
			else
			{
				//begin to get all user information			
				if(FacebookBaseActivity.class.isInstance(mContext))
				{
					AsyncFacebook af = ((FacebookBaseActivity)mContext).getAsyncFacebook();
					if(af != null)
					{
						af.getBasicUsersAsync(uids, new FacebookAdapter()
				    	{
				    		@Override public void getUsers(List<FacebookUser> users)
				            {
				    			if(users != null && users.size()>0)
				    			{
				    				Log.d(TAG, "after get participants info="+users.size());
									//update database
					    			for(int i=0;i<users.size();i++)
					    			{
					    				user = users.get(i);
									    orm.addFacebookUser(user);
					    			}
									
									updateUIFromUser(users);
				    			}
				            }
				    		
				            @Override public void onException(FacebookException e, int method) 
				            {
				            	Log.d(TAG, "fail to get the participants");				            	        	
				            }
				    	});
					}
				}
			}
			return "";			
		}
	}
	
	protected void updateUIFromUser(List<FacebookUser> users) 
	{
		String uname="";
		for(int i=0;i<users.size();i++)
		{
			FacebookUser user = users.get(i);
			if(user.name != null )
			{
			    if(i>0)
				    uname +=", ";
			    
			    uname += user.name;
			}
		}
		
		if(uname.length() > 0)
		{
			Message msg = handler.obtainMessage(UPDATE_PAT_UI);
			msg.getData().putString("usernames", uname);
			handler.sendMessage(msg);
		}
	}
	public String getUserName() 
	{
		if(type ==0)
		    return wall.getFromusername();
		else
		{
			FacebookUser user = orm.getFacebookUser(getFromUID());
			if(user != null)
			    return user.name;
			else
				return String.valueOf(mthread.originator);
		}
	}
		
	private void init() 
	{
		Log.d(TAG,  "call MessageItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		int resID = R.layout.facebook_message_item;
		if(type == 1)
		{
			resID = R.layout.facebook_mail_item;
		}
		View v  = factory.inflate(resID, null);
		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);
		if(showImage==false)
			imageView.setVisibility(View.GONE);
		
		publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);		
		username     = (TextView)v.findViewById(R.id.tweet_user_name);
		
		username.setSingleLine(true);
		setUI();		
	}

	public void chooseMessageListener()
	{
		//setOnClickListener(msgOnClik);
	}
	
	View.OnClickListener msgOnClik = new View.OnClickListener()
	{
		public void onClick(View v) 
		{
			 Log.d(TAG, "msgOnClik you click first one=");			 
			 //view details			 
			 if(type ==0 && wall != null)
			 {
				Intent intent = new Intent(mContext, FacebookAccountActivity.class);
				intent.putExtra("uid", wall.fromid);
				intent.putExtra("username", wall.getFromusername());
				intent.putExtra("imageurl", imageURL);				
				((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
			 }
			 if(type ==1 && mthread != null)
			 {
								 
				 Intent intent = new Intent(mContext, FacebookMailDetailActivity.class);				
				 //set mail conversation detail information
				 MailboxThreadParcel mail = new MailboxThreadParcel(mthread);
				 
				 intent.putExtra("mailboxthread", mail);				 
				 intent.putExtra("tid",      mthread.threadid);
				 intent.putExtra("ouid",     mthread.originator);
				 intent.putExtra("username", getUserName());
				 intent.putExtra("imageurl", imageURL);	
				 
				 ((FacebookBaseActivity)mContext).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_MAIL_DETAIL);
			 }
			 
		}
	};
	

	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	static boolean noneedcut=true;
	private String getViewString(String users)
	{
		if(noneedcut)
		{
		    return users;
		}
		
		if(type == 0)
		{
			return users;
		}
		
		float tmpWidth=mWidth;
	    if(Activity.class.isInstance(mContext))
	    {
	        Activity ac = (Activity)mContext;
	        Display display = ac.getWindowManager().getDefaultDisplay();	        
	        tmpWidth = (float) (0.4 * display.getWidth());
	        if(display.getWidth() > display.getHeight())
	        {
	            tmpWidth = (float) (0.65 * display.getWidth());
	        }
	    }	    

	    if(users != null && users.length() > 7)
        {
            android.graphics.Paint pn = new android.graphics.Paint();
            pn.setTextSize(username.getTextSize());
            float[] widths = new float[users.length()];
            int count = pn.getTextWidths(users, widths);
            float totle=0.0f;
            int number=0;
            for(int i=0;i<count;i++)
            {
                if(totle < tmpWidth)
                {
                    number++;
                    totle+= widths[i];
                }
                else
                {
                    break;
                }                  
            }
            if(number < count)
            {
                return users.subSequence(0, number)+"...";
            }            
        }	    
	    return users;
	}
	
	private void setUI()
	{
	    //set no text and image firstly
	    username.setText("");
	    imageView.setImageBitmap(null);
	    
		if(showUsername)
		{
		    username.setText(getViewUserName());      
		}
		
		if(showImage)
		{
			if(type ==0)
			{
			    setImage();
			}
			else if(type == 1)
			{
				if(mthread != null)
				{
					if(mthread.unreadcount > 0)
					{
						this.imageView.setImageResource(R.drawable.cmcc_list_message_new);
					}
					else
					{
						this.imageView.setImageResource(R.drawable.cmcc_list_message_readsms);
					}
				}
			}
		}
		publishDate.setText(this.getDate());		
		publishTxt.setText(this.getText());
	}
	
	public void setWallItem(Wall di) 
	{
		wall = di;		
		//need reget the image
		imageURL = null;
		setUI();
	}

	public void setMessageItem(MailboxThread di) 
	{
		mthread = di;
		//need reget the image
		imageURL = null;
		
		setUI();
	}
	public MailboxThread getThread() 
	{
		if(mthread != null)
		{
			return mthread;
		}
		return null;
	}	
}

