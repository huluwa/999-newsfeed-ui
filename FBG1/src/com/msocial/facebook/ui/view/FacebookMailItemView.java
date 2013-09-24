package com.msocial.facebook.ui.view;

import com.msocial.facebook.*;
import java.util.List;
import com.msocial.facebook.providers.SocialORM;
import com.msocial.facebook.ui.FacebookAccountActivity;
import com.msocial.facebook.ui.FacebookBaseActivity;
import oms.sns.service.facebook.client.AsyncFacebook;
import oms.sns.service.facebook.client.FacebookAdapter;
import oms.sns.service.facebook.client.FacebookException;
import oms.sns.service.facebook.model.FacebookUser;
import oms.sns.service.facebook.model.MailboxMessage;
import oms.sns.service.facebook.model.MessageThreadInfo;
import oms.sns.service.facebook.model.Page;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FacebookMailItemView extends SNSItemView {
	private final String TAG="MailItemView";
	
	private ImageView imageView;
	private TextView publishDate;
	private TextView publishTxt;
	private TextView username;	
	
	private MailboxMessage    msg;
	private MessageThreadInfo thread;
	
	String  imageURL;
	Handler handler;
	
	private boolean showImage   =true;
	private boolean showUsername=true;
	private boolean isupdate = false;
	SocialORM orm;
	FacebookUser user;
	Page page;
	public MailboxMessage getMessage()
	{
		return msg;
	}
	public FacebookMailItemView(Context ctx, AttributeSet attrs) 
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
	
	final int UPDATE_UI      =0;
	final int UPDATE_IMAGE_UI=1;
	final int UPDATE_PAGE_UI=2;
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
            			username.setText(user.name);
            		}
            		break;
            	}
            	 case UPDATE_PAGE_UI:
                 {          
                     if(page != null)
                     {
                         username.setText(page.name); 
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
            }
        }	        
	}
	
	public FacebookMailItemView(Context context, MailboxMessage di) 
	{		
		super(context);
		mContext = context;
		msg = di;
		
		orm = SocialORM.instance(mContext);
		Log.d(TAG, "call mail box MailItemView");
		
		handler = new Handler();
		init();
	}
	
	public FacebookMailItemView(Context context, MailboxMessage di,boolean isupdate) 
    {       
        super(context);
        mContext = context;
        msg = di;
        this.isupdate = isupdate;
        orm = SocialORM.instance(mContext);
        Log.d(TAG, "call mail box MailItemView");
        
        handler = new Handler();
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
	
	public long getFromPID()
	{
	    long pid = 0;
	    if(msg != null)
        {
	        MessageThreadInfo mtinfo = orm.getMailThread(msg.threadid);
	        if(mtinfo != null && mtinfo.object_id>0)
	        {
	            pid = mtinfo.object_id;
	        }
        }       
	    return pid;
	}
	public long getFromUID()
	{
		return msg.author;		
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
				    				Log.d(TAG, "after get user info="+user);
				    				user = users.get(0);
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
            imagerun.use_avatar = true;
            imagerun.setImageView(imageView);
            imagerun.post(imagerun);
		}
	}
	private String getDate()
	{
		return msg.timesent.toLocaleString();
	}
	public String getText()
	{
		return msg.body;
	}
	public String getViewUserName() 
	{
	
		FacebookUser user = orm.getFacebookUser(getFromUID());
		if(user != null)
		{
			return user.name;
		}		
		return String.valueOf(getFromUID());		
	}
	
	public String getUserName() 
	{
		return getViewUserName();
	}
		
	private void init() 
	{
		Log.d(TAG,  "call FacebookMailItemView init");
		LayoutInflater factory = LayoutInflater.from(mContext);
		removeAllViews();
		
		//container
		FrameLayout view = (FrameLayout) FrameLayout.inflate(getContext(), R.layout.sns_container_normal, null);		
		FrameLayout.LayoutParams paras = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,	FrameLayout.LayoutParams.WRAP_CONTENT);		
		view.setLayoutParams(paras);
		view.setVerticalScrollBarEnabled(true);
		addView(view);
		
		//child 1
		View v  = factory.inflate(R.layout.facebook_mail_detail_item, null);		
		v.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,	LayoutParams.WRAP_CONTENT));
		view.addView(v);
		
		imageView  = (ImageView)v.findViewById(R.id.tweet_img_ui);
		if(showImage==false)
			imageView.setVisibility(View.GONE);
		
		publishDate  = (TextView)v.findViewById(R.id.tweet_publish_time);
		publishTxt   = (TextView)v.findViewById(R.id.tweet_publish_text);	
		//publishTxt.setMovementMethod(LinkMovementMethod.getInstance());
       // publishTxt.setLinksClickable(true);
		username     = (TextView)v.findViewById(R.id.tweet_user_name);
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
		}
	};
	

	@Override
	protected void onFinishInflate() 
	{	
		super.onFinishInflate();		
		init();
	}
	
	private void setUI()
	{
	    if(msg != null)
	    {
    		if(showUsername)
    		{
    		    if(isupdate == false)
    			    username.setText(getViewUserName());
    		    else
    		        username.setText(getViewPageName());
    			//username.setOnClickListener(viewUserDetailsClick);
    		}
    		
    		if(showImage)
    		{
    		    if(isupdate == false)
    			    setImage();
    		    else
    		        setPageImage();
    			//imageView.setOnClickListener(viewUserDetailsClick);
    		}
    		publishDate.setText(this.getDate());		
    		publishTxt.setText(this.getText());
	    }
		
	}
	
	private void setPageImage() {
	    if(imageURL == null)
        {
            long id = getFromPID();         
            page = orm.getPageBypid(id)  ;       
            
            boolean getFromDB=false;
            if(page == null)
            {
                getFromDB = true;
            }
            else
            {
                Log.d(TAG, "who am I="+user);
                imageURL = page.pic_square;
                //no user data, maybe the user has image
                if((imageURL == null || imageURL.length() ==0 ) && (page.name == null || page.name.length() == 0))
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
                        af.getPageInfoAsync(id,new FacebookAdapter()
                        {
                            @Override public void getPageInfo(Page tmpPage)
                            {
                                if(tmpPage != null )
                                {
                                    page = tmpPage;
                                    Log.d(TAG, "after get user info="+user);                               
                                    imageURL = page.pic_square;                                 
                                    getImageBMP(imageURL, true);                               
                                    //update database
                                    orm.insertPage(tmpPage);                           
                                    updateUIFromPage();
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
	
	protected void updateUIFromPage() {
        handler.obtainMessage(UPDATE_PAGE_UI).sendToTarget();   
    }
	
    private String getViewPageName() {
        long pid = getFromPID();   
        Page page = orm.getPageBypid(pid);
        if(page == null)
        {
            return "";
        }
        else
        {
            return page.name;
        }
    }

    View.OnClickListener viewUserDetailsClick = new View.OnClickListener()
    {
        public void onClick(View v) 
        {
            Log.d(TAG, "viewUserDetailsClick you click first one=");   
            Intent intent = new Intent(mContext, FacebookAccountActivity.class);
            if(user != null)
            {
                intent.putExtra("uid",      user.uid);
                intent.putExtra("username", user.name);
                intent.putExtra("imageurl", user.pic_square);               
            }
            ((FacebookBaseActivity)(mContext)).startActivityForResult(intent, FacebookBaseActivity.FACEBOOK_USER_DETAIL);
        }
    };

	public void setMessageItem(MailboxMessage di) 
	{
		msg = di;		
		//need reget the image
		imageURL = null;
		setUI();
	}
	public void setMessageItem(MailboxMessage di,boolean isupdate) 
    {
        msg = di;   
        this.isupdate = isupdate;
        //need reget the image
        imageURL = null;
        setUI();
    }
}

